
package motorph_employeeapp;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SalaryComputationModule
 * -----------------------
 * Core payroll engine for MotorPH semi-monthly pay processing.
 *
 * Business rules implemented here:
 *   - Attendance hours are split into two cutoffs per month (days 1–15 and 16–31).
 *   - Shift hours use an 8:00 AM start with 8:10 AM grace period, 5:00 PM cap, and 1-hour lunch.
 *   - Government deductions (SSS, PhilHealth, Pag-IBIG, withholding tax) apply to the
 *     second cutoff net pay only; first cutoff is gross-only in this model.
 *   - Deduction amounts follow simplified Philippine contribution/tax tables.
 *
 * Output is written directly to a Swing {@link javax.swing.JTextArea} for on-screen payslip display.
 */
public class SalaryComputationModule {

    /** Last computed total gross pay (both cutoffs), for GUI summary display. */
    public static double summaryGross = 0;

    /** Last computed total deductions (2nd cutoff), for GUI summary display. */
    public static double summaryDeductions = 0;

    /** Last computed total net pay (both cutoffs), for GUI summary display. */
    public static double summaryNet = 0;

    /** True when the last calculatePayroll call found actual attendance data. */
    public static boolean lastCalculationSucceeded = false;

    // ── Per-calculation snapshot used by PDF payslip export and employee document viewer ──

    /** Employee ID from the most recent {@link #calculatePayroll} run. */
    public static String lastEmpId = "";
    /** Full display name from the most recent payroll run. */
    public static String lastEmpName = "";
    /** Birthday string from employee CSV for payslip header. */
    public static String lastEmpBirthday = "";
    /** English month name (e.g. "June") for payslip labels. */
    public static String lastMonthName = "";
    /** Pay coverage year string (e.g. "2024"). */
    public static String lastYear = "";
    /** Billable hours in 1st cutoff (days 1–15). */
    public static double lastHoursFirst = 0;
    /** Billable hours in 2nd cutoff (days 16–31). */
    public static double lastHoursSecond = 0;
    /** Gross pay for 1st cutoff (no deductions in this model). */
    public static double lastGrossFirst = 0;
    /** Gross pay for 2nd cutoff before deductions. */
    public static double lastGrossSecond = 0;
    /** Net pay for 1st cutoff (= gross first in this model). */
    public static double lastNetFirst = 0;
    /** Net pay for 2nd cutoff after all deductions. */
    public static double lastNetSecond = 0;
    /** SSS contribution from last calculation. */
    public static double lastSss = 0;
    /** PhilHealth employee share from last calculation. */
    public static double lastPhilHealth = 0;
    /** Pag-IBIG contribution from last calculation. */
    public static double lastPagIbig = 0;
    /** Withholding tax from last calculation. */
    public static double lastTax = 0;
    /** Sum of all deductions applied on 2nd cutoff. */
    public static double lastTotalDeductions = 0;

    /** Outcome of validating employee payroll inputs before bulk computation. */
    public static class PayrollValidationResult {
        /** Blocking errors that prevent payroll from running. */
        public final List<String> errors;
        /** Non-blocking issues (e.g. employee with zero attendance hours). */
        public final List<String> warnings;

        public PayrollValidationResult(List<String> errors, List<String> warnings) {
            this.errors = errors;
            this.warnings = warnings == null ? new ArrayList<>() : warnings;
        }

        /** @return {@code true} when {@link #errors} is empty */
        public boolean isValid() {
            return errors == null || errors.isEmpty();
        }
    }

    /** Summary row for one employee after bulk salary computation. */
    public static class EmployeePayrollSummary {
        /** Employee number from CSV. */
        public final String employeeId;
        /** Display name for batch results table. */
        public final String employeeName;
        /** Total attendance hours in the selected month. */
        public final double hoursWorked;
        /** Gross pay (hours × hourly rate). */
        public final double grossPay;
        /** SSS + PhilHealth + Pag-IBIG + tax. */
        public final double totalDeductions;
        /** Gross minus deductions. */
        public final double netPay;
        /** {@code false} when employee had no attendance in the period. */
        public final boolean computed;

        public EmployeePayrollSummary(String employeeId, String employeeName, double hoursWorked,
                double grossPay, double totalDeductions, double netPay, boolean computed) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.hoursWorked = hoursWorked;
            this.grossPay = grossPay;
            this.totalDeductions = totalDeductions;
            this.netPay = netPay;
            this.computed = computed;
        }
    }

    /** Result of computing salaries for every employee in the CSV. */
    public static class BulkPayrollResult {
        public final boolean savedToFile;
        public final List<EmployeePayrollSummary> summaries;
        public final double totalGross;
        public final double totalDeductions;
        public final double totalNet;
        public final int computedCount;
        public final int skippedCount;

        public BulkPayrollResult(boolean savedToFile, List<EmployeePayrollSummary> summaries,
                double totalGross, double totalDeductions, double totalNet,
                int computedCount, int skippedCount) {
            this.savedToFile = savedToFile;
            this.summaries = summaries;
            this.totalGross = totalGross;
            this.totalDeductions = totalDeductions;
            this.totalNet = totalNet;
            this.computedCount = computedCount;
            this.skippedCount = skippedCount;
        }
    }

    /**
     * Main payroll routine: aggregates attendance, computes gross per cutoff, applies deductions,
     * and renders a formatted payslip into the GUI text area.
     *
     * @param emp    split employee row from Employee Details CSV (minimum 19 columns)
     * @param month  numeric month as string (e.g. "6" for June)
     * @param year   pay coverage year (currently validated as "2024" in the GUI)
     * @param output JTextArea on the payroll screen; cleared then filled with results
     */
    public static void calculatePayroll(String[] emp, String month, String year, javax.swing.JTextArea output) {
        // Guard: employee array must include ID and hourly rate column
        if (emp == null || emp.length < 19 || emp[0].isEmpty())
            return;

        output.setText("");
        summaryGross = 0;
        summaryDeductions = 0;
        summaryNet = 0;
        lastCalculationSucceeded = false;

        String id = emp[EmployeeModule.ID];
        double hourlyRate = EmployeeModule.getHourlyRate(emp);
        String mName = monthName(month);

        // Accumulators for semi-monthly cutoffs
        double hoursFirstCutoff = 0;
        double hoursSecondCutoff = 0;
        int matchedCount = 0;

        // Load every attendance row for this employee; filter by month/year below
        List<String> records = FileHandlerModule.findAttendanceData(id);

        for (String line : records) {
            String[] row = FileHandlerModule.smartSplit(line);
            if (row.length < 6)
                continue; // Row must have ID, names, date, login, logout

            // Date is stored as MM/DD/YYYY in column index 3
            String[] dateParts = row[3].split("/");
            if (dateParts.length < 3)
                continue;

            try {
                int inputMonth = Integer.parseInt(month.trim());
                int inputYear = Integer.parseInt(year.trim());

                int csvMonth = Integer.parseInt(dateParts[0].trim());
                int csvYear = Integer.parseInt(dateParts[2].trim());

                // Only count attendance within the selected pay period
                if (csvMonth == inputMonth && csvYear == inputYear) {
                    matchedCount++;
                    int day = Integer.parseInt(dateParts[1].trim());

                    // Columns 4 and 5 hold login and logout times (H:mm format)
                    double shift = calculateShift(row[4].trim(), row[5].trim());

                    // First cutoff: 1st through 15th; second cutoff: 16th through end of month
                    if (day <= 15) {
                        hoursFirstCutoff += shift;
                    } else {
                        hoursSecondCutoff += shift;
                    }
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                // Log bad rows to console for debugging without stopping payroll
                System.out.println("Skipped unparseable row: " + line + " due to: " + e.getMessage());
            }
        }

        // No attendance records found for the selected period — abort with clear message
        if (matchedCount == 0) {
            output.setText(
                "\n ⚠  No attendance data available for " + mName + " " + year + ".\n\n" +
                " Payslip data is not available or retrievable for this period.\n\n" +
                " Please verify that:\n" +
                "   - The selected month and year are correct.\n" +
                "   - Attendance records exist for Employee #" + id + "\n" +
                "     in the selected pay period.\n\n" +
                " Only periods with recorded attendance can generate a payslip.");
            return;
        }

        // Gross pay = total hours in cutoff × hourly rate from employee master file
        double grossFirstCutoff = computeGrossPay(new double[] { hourlyRate }, new double[] { hoursFirstCutoff })[0];
        double grossSecondCutoff = computeGrossPay(new double[] { hourlyRate }, new double[] { hoursSecondCutoff })[0];
        double totalMonthlyGross = grossFirstCutoff + grossSecondCutoff;

        // Statutory deductions computed on full monthly gross, applied on 2nd cutoff net
        double sss = computeSSS(new double[] { totalMonthlyGross })[0];
        double ph = computePhilHealth(new double[] { totalMonthlyGross })[0];
        double pi = computePagIBIG(new double[] { totalMonthlyGross })[0];
        double taxableIncome = totalMonthlyGross - (sss + ph + pi);
        double tax = computeWithholdingTax(new double[] { taxableIncome })[0];
        double totalDeduc = computeDeductions(new double[] { sss }, new double[] { ph },
                new double[] { pi }, new double[] { tax })[0];

        // 1st cutoff: no deductions in this payslip model; 2nd cutoff bears all deductions
        double netSalary1 = grossFirstCutoff;
        double netSalary2 = computeNetPay(new double[] { grossSecondCutoff }, new double[] { totalDeduc })[0];

        // --- Build payslip text in the GUI result panel ---
        output.append("\n Employee #: " + id);
        output.append("\n Employee Name: " + EmployeeModule.fullName(emp));
        output.append("\n Birthday: " + emp[EmployeeModule.BIRTHDAY]);
        output.append("\n Cutoff Date: " + mName + " 1 to " + mName + " 15, " + year);
        output.append("\n Total Hours Worked: " + String.format("%.2f", hoursFirstCutoff));
        output.append("\n Gross Salary: " + "PHP " + String.format("%,.2f", grossFirstCutoff));
        output.append("\n Net Salary: " + "PHP " + String.format("%,.2f", netSalary1));
        output.append("\n ");
        output.append("\n Cutoff Date: " + mName + " 16 to " + mName + " 31, " + year);
        output.append("\n Total Hours Worked: " + String.format("%.2f", hoursSecondCutoff));
        output.append("\n Gross Salary: " + "PHP " + String.format("%,.2f", grossSecondCutoff));
        output.append("\n Each Deduction:");
        output.append("\n    - SSS: " + "PHP " + String.format("%,.2f", sss));
        output.append("\n    - PhilHealth: " + "PHP " + String.format("%,.2f", ph));
        output.append("\n    - Pag-IBIG: " + "PHP " + String.format("%,.2f", pi));
        output.append("\n    - Withholding Tax: " + "PHP " + String.format("%,.2f", tax));
        output.append("\n Total Deductions: " + "PHP " + String.format("%,.2f", totalDeduc));
        output.append("\n Net Salary: " + "PHP " + String.format("%,.2f", netSalary2));

        summaryGross = grossFirstCutoff + grossSecondCutoff;
        summaryDeductions = totalDeduc;
        summaryNet = netSalary1 + netSalary2;
        lastCalculationSucceeded = true;

        lastEmpId          = id;
        lastEmpName        = EmployeeModule.fullName(emp);
        lastEmpBirthday    = emp[EmployeeModule.BIRTHDAY];
        lastMonthName      = mName;
        lastYear           = year;
        lastHoursFirst     = hoursFirstCutoff;
        lastHoursSecond    = hoursSecondCutoff;
        lastGrossFirst     = grossFirstCutoff;
        lastGrossSecond    = grossSecondCutoff;
        lastNetFirst       = netSalary1;
        lastNetSecond      = netSalary2;
        lastSss            = sss;
        lastPhilHealth     = ph;
        lastPagIbig        = pi;
        lastTax            = tax;
        lastTotalDeductions = totalDeduc;
    }

    /**
     * Computes billable work hours for one attendance day from login/logout timestamps.
     *
     * Rules:
     *   - Official shift window: 8:00 AM – 5:00 PM.
     *   - Arrival before or at 8:10 AM counts as 8:00 AM start (grace period).
     *   - Logout after 5:00 PM is capped at 5:00 PM.
     *   - One hour is subtracted for lunch break.
     *
     * @param logIn  time string in H:mm format (e.g. "8:04")
     * @param logOut time string in H:mm format (e.g. "17:12")
     * @return hours worked as double; 0.0 if invalid or negative duration
     */
    public static double calculateShift(String logIn, String logOut) {
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("H:mm");
            LocalTime timeIn = LocalTime.parse(logIn, format);
            LocalTime timeOut = LocalTime.parse(logOut, format);
            LocalTime graceLimit = LocalTime.of(8, 10);
            LocalTime startLimit = LocalTime.of(8, 0);
            LocalTime endLimit = LocalTime.of(17, 0);

            // Apply grace: late within 10 minutes still starts at 8:00
            LocalTime actualStart = timeIn.isAfter(graceLimit) ? timeIn : startLimit;
            // Cap end time at official shift end
            LocalTime actualEnd = timeOut.isAfter(endLimit) ? endLimit : timeOut;

            if (actualStart.isAfter(actualEnd))
                return 0;

            int startMins = actualStart.getHour() * 60 + actualStart.getMinute();
            int endMins = actualEnd.getHour() * 60 + actualEnd.getMinute();

            // Subtract 60 minutes for lunch; convert total minutes to hours
            return Math.max(0, (endMins - startMins - 60) / 60.0);
        } catch (java.time.format.DateTimeParseException e) {
            return 0.0;
        }
    }

    /**
     * Computes gross pay for each employee using rate × days/hours worked arrays.
     *
     * @param ratesPerDay hourly or daily rate values (same length as daysWorked)
     * @param daysWorked  hours or days worked per employee
     * @return gross pay per index; 0 when inputs are missing or length mismatch
     */
    public static double[] computeGrossPay(double[] ratesPerDay, double[] daysWorked) {
        if (ratesPerDay == null || daysWorked == null) {
            return new double[0];
        }
        int n = Math.min(ratesPerDay.length, daysWorked.length);
        double[] gross = new double[n];
        for (int i = 0; i < n; i++) {
            if (!Double.isFinite(ratesPerDay[i]) || !Double.isFinite(daysWorked[i])) {
                gross[i] = 0.0;
            } else {
                gross[i] = ratesPerDay[i] * daysWorked[i];
            }
        }
        return gross;
    }

    /**
     * Computes SSS contributions for an array of monthly gross salaries.
     */
    public static double[] computeSSS(double[] grossPays) {
        return mapDoubles(grossPays, SalaryComputationModule::computeSSS);
    }

    /**
     * Computes PhilHealth contributions for an array of monthly gross salaries.
     */
    public static double[] computePhilHealth(double[] grossPays) {
        return mapDoubles(grossPays, SalaryComputationModule::computePhilHealth);
    }

    /**
     * Computes Pag-IBIG contributions for an array of monthly gross salaries.
     */
    public static double[] computePagIBIG(double[] grossPays) {
        return mapDoubles(grossPays, SalaryComputationModule::computePagIBIG);
    }

    /**
     * Computes withholding tax for an array of taxable income values.
     */
    public static double[] computeWithholdingTax(double[] taxableIncomes) {
        return mapDoubles(taxableIncomes, SalaryComputationModule::computeWithholdingTax);
    }

    /**
     * Sums SSS, PhilHealth, Pag-IBIG, and withholding tax per employee index.
     */
    public static double[] computeDeductions(double[] sss, double[] philHealth,
            double[] pagIbig, double[] withholdingTax) {
        if (sss == null || philHealth == null || pagIbig == null || withholdingTax == null) {
            return new double[0];
        }
        int n = Math.min(Math.min(sss.length, philHealth.length),
                Math.min(pagIbig.length, withholdingTax.length));
        double[] totals = new double[n];
        for (int i = 0; i < n; i++) {
            totals[i] = sss[i] + philHealth[i] + pagIbig[i] + withholdingTax[i];
        }
        return totals;
    }

    /**
     * Computes net pay by subtracting total deductions from gross pay per index.
     */
    public static double[] computeNetPay(double[] grossPays, double[] totalDeductions) {
        if (grossPays == null || totalDeductions == null) {
            return new double[0];
        }
        int n = Math.min(grossPays.length, totalDeductions.length);
        double[] net = new double[n];
        for (int i = 0; i < n; i++) {
            net[i] = grossPays[i] - totalDeductions[i];
        }
        return net;
    }

    /**
     * Validates numeric payroll inputs for every employee before bulk computation.
     *
     * @param employees all employee rows from CSV
     * @param month     pay coverage month (1–12)
     * @param year      pay coverage year
     */
    public static PayrollValidationResult validatePayrollInputs(List<String[]> employees,
            String month, String year) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (month == null || month.trim().isEmpty()) {
            errors.add("Pay coverage month is required.");
        } else if (!isValidInteger(month)) {
            errors.add("Pay coverage month must be a valid number.");
        } else {
            int m = Integer.parseInt(month.trim());
            if (m < 1 || m > 12) {
                errors.add("Pay coverage month must be between 1 and 12.");
            }
        }

        if (year == null || year.trim().isEmpty()) {
            errors.add("Pay coverage year is required.");
        } else if (!isValidInteger(year)) {
            errors.add("Pay coverage year must be a valid number.");
        }

        if (employees == null || employees.isEmpty()) {
            errors.add("No employee records were loaded from the CSV file.");
            return new PayrollValidationResult(errors, warnings);
        }

        for (String[] emp : employees) {
            if (emp == null || emp.length == 0 || emp[EmployeeModule.ID] == null
                    || emp[EmployeeModule.ID].trim().isEmpty()) {
                errors.add("One or more employee records are missing an Employee Number.");
                continue;
            }
            String id = emp[EmployeeModule.ID].trim();
            String rateText = emp.length > EmployeeModule.HOURLY_RATE
                    ? emp[EmployeeModule.HOURLY_RATE] : "";
            if (rateText == null || rateText.trim().isEmpty()) {
                errors.add("Employee #" + id + ": Hourly Rate is required.");
            } else if (!isValidNumber(rateText)) {
                errors.add("Employee #" + id + ": Hourly Rate must be a valid number.");
            } else if (EmployeeModule.getHourlyRate(emp) <= 0) {
                errors.add("Employee #" + id + ": Hourly Rate must be greater than zero.");
            } else {
                double hours = sumAttendanceHours(id, month, year);
                if (hours <= 0) {
                    warnings.add("Employee #" + id + ": no attendance hours found for the selected period.");
                }
            }
        }

        return new PayrollValidationResult(errors, warnings);
    }

    /**
     * Computes salaries for every employee in the CSV and persists results to disk.
     */
    public static BulkPayrollResult computeAllEmployeeSalaries(String month, String year,
            javax.swing.JTextArea output) {
        FileHandlerModule.ensureEmployeeFileSchema();
        return computeSelectedEmployeeSalaries(month, year,
                FileHandlerModule.getAllEmployees(), output, true);
    }

    /**
     * Computes monthly payroll for a chosen subset using array-based modular methods,
     * optionally merging computed columns back into the full Employee Details CSV.
     *
     * @param month             pay coverage month (1–12)
     * @param year              pay coverage year
     * @param selectedEmployees employee rows to process (typically checked rows in HR batch payroll)
     * @param output            optional text area for plain-text summary; may be {@code null}
     * @param saveToFile        when {@code true}, updates matching rows in the CSV via
     *                          {@link FileHandlerModule#rewriteEmployeeFile}
     */
    public static BulkPayrollResult computeSelectedEmployeeSalaries(String month, String year,
            List<String[]> selectedEmployees, javax.swing.JTextArea output, boolean saveToFile) {
        FileHandlerModule.ensureEmployeeFileSchema();

        if (selectedEmployees == null || selectedEmployees.isEmpty()) {
            return new BulkPayrollResult(false, new ArrayList<>(), 0, 0, 0, 0, 0);
        }

        List<String[]> selected = new ArrayList<>();
        for (String[] row : selectedEmployees) {
            selected.add(FileHandlerModule.normalizeEmployeeRow(row));
        }

        PayrollValidationResult validation = validatePayrollInputs(selected, month, year);
        if (!validation.isValid()) {
            return new BulkPayrollResult(false, new ArrayList<>(), 0, 0, 0, 0, selected.size());
        }

        int count = selected.size();
        double[] rates = new double[count];
        double[] hoursWorked = new double[count];
        List<EmployeePayrollSummary> summaries = new ArrayList<>();
        Map<String, String[]> computedById = new HashMap<>();
        double runningGross = 0;
        double runningDeductions = 0;
        double runningNet = 0;
        int computedCount = 0;
        int skippedCount = 0;

        for (int i = 0; i < count; i++) {
            String[] emp = selected.get(i);
            String id = emp[EmployeeModule.ID].trim();
            rates[i] = EmployeeModule.getHourlyRate(emp);
            hoursWorked[i] = sumAttendanceHours(id, month, year);
        }

        double[] grossPays = computeGrossPay(rates, hoursWorked);
        double[] sss = computeSSS(grossPays);
        double[] philHealth = computePhilHealth(grossPays);
        double[] pagIbig = computePagIBIG(grossPays);
        double[] taxable = new double[count];
        for (int i = 0; i < count; i++) {
            taxable[i] = grossPays[i] - (sss[i] + philHealth[i] + pagIbig[i]);
        }
        double[] tax = computeWithholdingTax(taxable);
        double[] totalDeductions = computeDeductions(sss, philHealth, pagIbig, tax);
        double[] netPays = computeNetPay(grossPays, totalDeductions);

        if (output != null) {
            output.setText("");
            output.append("  PAYROLL SUMMARY — " + monthName(month) + " " + year.trim() + "\n");
            output.append("  Selected employee salaries computed from attendance hours × hourly rate.\n");
            output.append("  ────────────────────────────────────────────────────────────────\n\n");
        }

        for (int i = 0; i < count; i++) {
            String[] emp = selected.get(i);
            String id = emp[EmployeeModule.ID].trim();
            String name = EmployeeModule.fullName(emp);
            boolean hasHours = hoursWorked[i] > 0;
            if (hasHours) {
                emp[EmployeeModule.HOURS_WORKED] = formatAmount(hoursWorked[i]);
                emp[EmployeeModule.GROSS_PAY] = formatAmount(grossPays[i]);
                emp[EmployeeModule.TOTAL_DEDUCTIONS] = formatAmount(totalDeductions[i]);
                emp[EmployeeModule.NET_PAY] = formatAmount(netPays[i]);
                computedCount++;
                runningGross += grossPays[i];
                runningDeductions += totalDeductions[i];
                runningNet += netPays[i];
            } else {
                emp[EmployeeModule.HOURS_WORKED] = "0.00";
                emp[EmployeeModule.GROSS_PAY] = "0.00";
                emp[EmployeeModule.TOTAL_DEDUCTIONS] = "0.00";
                emp[EmployeeModule.NET_PAY] = "0.00";
                skippedCount++;
            }
            computedById.put(id, emp);
            summaries.add(new EmployeePayrollSummary(id, name, hoursWorked[i], grossPays[i],
                    totalDeductions[i], netPays[i], hasHours));

            if (output != null) {
                output.append(String.format("  #%s  %-28s  Hours %8.2f%n", id, truncate(name, 28), hoursWorked[i]));
                output.append(String.format("       Gross PHP %,12.2f   Deductions PHP %,12.2f   Net PHP %,12.2f%n%n",
                        grossPays[i], totalDeductions[i], netPays[i]));
            }
        }

        boolean saved = false;
        if (saveToFile) {
            List<String[]> allEmployees = new ArrayList<>(FileHandlerModule.getAllEmployees());
            for (int i = 0; i < allEmployees.size(); i++) {
                String[] row = FileHandlerModule.normalizeEmployeeRow(allEmployees.get(i));
                String id = row[EmployeeModule.ID].trim();
                if (computedById.containsKey(id)) {
                    allEmployees.set(i, computedById.get(id));
                }
            }
            saved = FileHandlerModule.rewriteEmployeeFile(allEmployees);
        }

        summaryGross = runningGross;
        summaryDeductions = runningDeductions;
        summaryNet = runningNet;
        lastCalculationSucceeded = computedCount > 0;

        if (output != null) {
            output.append("  ────────────────────────────────────────────────────────────────\n");
            output.append(String.format("  Processed: %d employee(s) with attendance  •  Skipped: %d%n",
                    computedCount, skippedCount));
            output.append(String.format("  Totals — Gross PHP %,.2f  •  Deductions PHP %,.2f  •  Net PHP %,.2f%n",
                    runningGross, runningDeductions, runningNet));
            if (saveToFile && saved) {
                output.append("\n  Employee Details CSV updated with Hours Worked, Gross Pay, "
                        + "Total Deductions, and Net Pay.");
            }
        }

        return new BulkPayrollResult(saved, summaries, runningGross, runningDeductions, runningNet,
                computedCount, skippedCount);
    }

    /**
     * Aggregates billable attendance hours for one employee in a calendar month.
     */
    public static double sumAttendanceHours(String employeeId, String month, String year) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return 0;
        }
        double totalHours = 0;
        List<String> records = FileHandlerModule.findAttendanceData(employeeId.trim());
        for (String line : records) {
            String[] row = FileHandlerModule.smartSplit(line);
            if (row.length < 6) {
                continue;
            }
            String[] dateParts = row[3].split("/");
            if (dateParts.length < 3) {
                continue;
            }
            try {
                int inputMonth = Integer.parseInt(month.trim());
                int inputYear = Integer.parseInt(year.trim());
                int csvMonth = Integer.parseInt(dateParts[0].trim());
                int csvYear = Integer.parseInt(dateParts[2].trim());
                if (csvMonth == inputMonth && csvYear == inputYear) {
                    totalHours += calculateShift(row[4].trim(), row[5].trim());
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            }
        }
        return totalHours;
    }

    /** Applies a scalar function to each element of {@code values}. */
    private static double[] mapDoubles(double[] values, java.util.function.DoubleUnaryOperator fn) {
        if (values == null) {
            return new double[0];
        }
        double[] mapped = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            mapped[i] = fn.applyAsDouble(values[i]);
        }
        return mapped;
    }

    /** True when {@code value} parses as a finite double (comma-safe). */
    private static boolean isValidNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value.replace(",", "").trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** True when {@code value} parses as a base-10 integer. */
    private static boolean isValidInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Formats monetary amounts as two decimal places for CSV columns. */
    private static String formatAmount(double value) {
        return String.format("%.2f", value);
    }

    /** Truncates long names for fixed-width batch payroll output. */
    private static String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 1) + "…";
    }

    /**
     * SSS employee contribution based on monthly salary credit brackets.
     *
     * Uses stepped 500-peso brackets from 3,250 to 24,750 salary credits.
     * Below minimum: flat 135.00; at/above maximum: capped at 1,125.00.
     *
     * @param salary total monthly gross income
     * @return SSS contribution amount in PHP
     */
    public static double computeSSS(double salary) {
        if (salary < 3250)
            return 135.00;
        if (salary >= 24750)
            return 1125.00;
        double threshold = 3250;
        double contribution = 157.50;
        while (threshold < 24750) {
            if (salary >= threshold && salary <= threshold + 499.99)
                return contribution;
            threshold += 500;
            contribution += 22.50;
        }
        return 1125.00;
    }

    /**
     * PhilHealth employee share (50% of total premium).
     *
     * Premium is 3% of salary between 10,000 and 60,000, with floor 300 and ceiling 1,800.
     *
     * @param salary total monthly gross income
     * @return employee PhilHealth contribution in PHP
     */
    public static double computePhilHealth(double salary) {
        double totalPremium;
        if (salary <= 10000)
            totalPremium = 300.0;
        else if (salary >= 60000)
            totalPremium = 1800.0;
        else
            totalPremium = salary * 0.03;
        return totalPremium / 2;
    }

    /**
     * Pag-IBIG employee contribution with combined employee/employer cap logic.
     *
     * Employee rate: 1% if salary ≤ 1,500, else 2%. Employer share modeled at 2%.
     * Total contribution capped at 100.00 PHP.
     *
     * @param salary total monthly gross income
     * @return Pag-IBIG contribution amount in PHP
     */
    public static double computePagIBIG(double salary) {
        double employeeRate = (salary > 1500) ? 0.02 : 0.01;
        double total = (salary * employeeRate) + (salary * 0.02);
        return Math.min(total, 100.0);
    }

    /**
     * BIR withholding tax on taxable income (gross minus SSS, PhilHealth, Pag-IBIG).
     */
    public static double computeWithholdingTax(double taxableIncome) {
        return calculateWithholdingTax(taxableIncome);
    }

    /**
     * BIR withholding tax on taxable income (gross minus SSS, PhilHealth, Pag-IBIG).
     *
     * Progressive brackets: 0% up to 20,832, then 20%, 25%, 30%, 32%, 35% tiers.
     *
     * @param taxableIncome gross salary minus mandatory non-tax deductions
     * @return withholding tax amount in PHP
     */
    public static double calculateWithholdingTax(double taxableIncome) {
        // 2023 TRAIN law (RA 10963 2nd tranche) monthly brackets
        if (taxableIncome <= 20833)
            return 0;
        else if (taxableIncome <= 33333)
            return (taxableIncome - 20833) * 0.15;
        else if (taxableIncome <= 66667)
            return 1875 + (taxableIncome - 33333) * 0.20;
        else if (taxableIncome <= 166667)
            return 8541.80 + (taxableIncome - 66667) * 0.25;
        else if (taxableIncome <= 666667)
            return 33541.80 + (taxableIncome - 166667) * 0.30;
        else
            return 183541.80 + (taxableIncome - 666667) * 0.35;
    }

    /**
     * Converts numeric month string to full English month name for payslip labels.
     *
     * @param monthStr month number as string (1–12)
     * @return month name or fallback label on parse error
     */
    public static String monthName(String monthStr) {
        try {
            int month = Integer.parseInt(monthStr.trim());
            switch (month) {
                case 1:  return "January";
                case 2:  return "February";
                case 3:  return "March";
                case 4:  return "April";
                case 5:  return "May";
                case 6:  return "June";
                case 7:  return "July";
                case 8:  return "August";
                case 9:  return "September";
                case 10: return "October";
                case 11: return "November";
                case 12: return "December";
                default: return "Month " + month;
            }
        } catch (NumberFormatException e) {
            return "Invalid Month";
        }
    }

    /**
     * Discovers distinct month/year pairs present in an employee's attendance history.
     *
     * Used to populate pay-period selectors or validate available coverage dates.
     * Format returned: "M/YYYY" (e.g. "6/2024").
     *
     * @param id employee number
     * @return unique list of month/year strings; may be empty
     */
    public static List<String> findWorkingPeriods(String id) {
        List<String> workingPeriods = new java.util.ArrayList<>();
        List<String> records = FileHandlerModule.findAttendanceData(id);

        for (String record : records) {
            String[] columns = FileHandlerModule.smartSplit(record);
            String[] dateParts = columns[3].split("/");

            if (dateParts.length >= 3) {
                try {
                    int m = Integer.parseInt(dateParts[0]);
                    int y = Integer.parseInt(dateParts[2]);
                    String monthYear = m + "/" + y;

                    // Avoid duplicate entries for the same month/year
                    if (!workingPeriods.contains(monthYear)) {
                        workingPeriods.add(monthYear);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return workingPeriods;
    }
}
