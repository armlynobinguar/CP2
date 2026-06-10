package motorph_employeeapp;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        String id = emp[EmployeeModule.ID];
        double hourlyRate = EmployeeModule.getHourlyRate(emp);
        String mName = monthName(month);

        // Accumulators for semi-monthly cutoffs
        double hoursFirstCutoff = 0;
        double hoursSecondCutoff = 0;

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

        // Gross pay = total hours in cutoff × hourly rate from employee master file
        double grossFirstCutoff = hoursFirstCutoff * hourlyRate;
        double grossSecondCutoff = hoursSecondCutoff * hourlyRate;
        double totalMonthlyGross = grossFirstCutoff + grossSecondCutoff;

        // Statutory deductions computed on full monthly gross, applied on 2nd cutoff net
        double sss = computeSSS(totalMonthlyGross);
        double ph = computePhilHealth(totalMonthlyGross);
        double pi = computePagIBIG(totalMonthlyGross);
        double taxableIncome = totalMonthlyGross - (sss + ph + pi);
        double tax = calculateWithholdingTax(taxableIncome);
        double totalDeduc = sss + ph + pi + tax;

        // 1st cutoff: no deductions in this payslip model; 2nd cutoff bears all deductions
        double netSalary1 = grossFirstCutoff;
        double netSalary2 = grossSecondCutoff - totalDeduc;

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
     *
     * Progressive brackets: 0% up to 20,832, then 20%, 25%, 30%, 32%, 35% tiers.
     *
     * @param taxableIncome gross salary minus mandatory non-tax deductions
     * @return withholding tax amount in PHP
     */
    public static double calculateWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832)
            return 0;
        else if (taxableIncome < 33333)
            return (taxableIncome - 20833) * 0.20;
        else if (taxableIncome < 66667)
            return 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome < 166667)
            return 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome < 666667)
            return 40833.33 + (taxableIncome - 166667) * 0.32;
        else
            return 200833.33 + (taxableIncome - 666667) * 0.35;
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

    // ─────────────────────────────────────────────────────────────────────────
    // FEATURE 3 — MODULAR SALARY COMPUTATION METHODS (array-based)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes gross pay for each employee given parallel arrays of days worked and rate per day.
     *
     * @param daysWorked  array of total days worked per employee
     * @param ratePerDay  array of daily pay rates per employee
     * @return array of gross pay values (daysWorked[i] × ratePerDay[i])
     */
    public static double[] computeGrossPay(double[] daysWorked, double[] ratePerDay) {
        double[] gross = new double[daysWorked.length];
        for (int i = 0; i < daysWorked.length; i++) {
            gross[i] = daysWorked[i] * ratePerDay[i];
        }
        return gross;
    }

    /**
     * Sums all statutory deductions (SSS + PhilHealth + Pag-IBIG + withholding tax)
     * for each employee into a single total deductions array.
     *
     * @param grossPay array of monthly gross pay per employee
     * @return array of total deductions per employee
     */
    public static double[] computeDeductions(double[] grossPay) {
        double[] deductions = new double[grossPay.length];
        for (int i = 0; i < grossPay.length; i++) {
            double sss      = computeSSS(grossPay[i]);
            double ph       = computePhilHealth(grossPay[i]);
            double pi       = computePagIBIG(grossPay[i]);
            double taxable  = grossPay[i] - (sss + ph + pi);
            double tax      = calculateWithholdingTax(taxable);
            deductions[i]   = sss + ph + pi + tax;
        }
        return deductions;
    }

    /**
     * Computes net pay for each employee by subtracting total deductions from gross pay.
     *
     * @param grossPay   array of gross pay per employee
     * @param deductions array of total deductions per employee
     * @return array of net pay values (grossPay[i] - deductions[i])
     */
    public static double[] computeNetPay(double[] grossPay, double[] deductions) {
        double[] net = new double[grossPay.length];
        for (int i = 0; i < grossPay.length; i++) {
            net[i] = grossPay[i] - deductions[i];
        }
        return net;
    }

    /**
     * Bulk payroll engine: processes all employees in the CSV for a given month/year.
     *
     * Steps:
     *   1. Loads all employee records from the CSV.
     *   2. Aggregates attendance hours per employee for the target period.
     *   3. Computes gross pay, deductions, and net pay using modular methods.
     *   4. Writes results to the output JTextArea.
     *   5. Returns a SalaryBatch summary for optional CSV export.
     *
     * @param month  numeric month string (e.g. "6")
     * @param year   pay year string (e.g. "2024")
     * @param output JTextArea to render the salary report into
     * @return SalaryBatch containing per-employee arrays and totals
     */
    public static SalaryBatch computeAllSalaries(String month, String year, javax.swing.JTextArea output) {
        output.setText("");

        java.util.List<String[]> employees = FileHandlerModule.getAllEmployees();
        int n = employees.size();

        if (n == 0) {
            output.append("No employee records found.");
            return null;
        }

        // Parallel arrays indexed by employee position in the list
        String[] ids          = new String[n];
        String[] names        = new String[n];
        double[] daysWorked   = new double[n];
        double[] ratePerDay   = new double[n];
        double[] grossPay     = new double[n];
        double[] deductions   = new double[n];
        double[] netPay       = new double[n];

        int inputMonth = 0;
        int inputYear  = 0;
        try {
            inputMonth = Integer.parseInt(month.trim());
            inputYear  = Integer.parseInt(year.trim());
        } catch (NumberFormatException e) {
            output.append("Invalid month or year format.");
            return null;
        }

        // Step 1 & 2: Load each employee and aggregate their hours for the period
        for (int i = 0; i < n; i++) {
            String[] emp = employees.get(i);
            ids[i]       = emp.length > EmployeeModule.ID ? emp[EmployeeModule.ID].trim() : "";
            names[i]     = EmployeeModule.fullName(emp);

            // Hourly rate from CSV column 18; convert to daily (8-hour day)
            double hourlyRate = EmployeeModule.getHourlyRate(emp);
            ratePerDay[i]    = hourlyRate * 8;

            // Sum attendance hours for this employee in the target month/year
            double totalHours = 0;
            java.util.List<String> records = FileHandlerModule.findAttendanceData(ids[i]);
            for (String line : records) {
                String[] row = FileHandlerModule.smartSplit(line);
                if (row.length < 6) continue;
                String[] dateParts = row[3].split("/");
                if (dateParts.length < 3) continue;
                try {
                    int csvMonth = Integer.parseInt(dateParts[0].trim());
                    int csvYear  = Integer.parseInt(dateParts[2].trim());
                    if (csvMonth == inputMonth && csvYear == inputYear) {
                        totalHours += calculateShift(row[4].trim(), row[5].trim());
                    }
                } catch (NumberFormatException ignored) {}
            }
            // Convert hours to days (8-hour workday)
            daysWorked[i] = totalHours / 8.0;
        }

        // Step 3: Compute gross, deductions, net using modular methods
        // grossPay = daysWorked × ratePerDay
        grossPay   = computeGrossPay(daysWorked, ratePerDay);
        deductions = computeDeductions(grossPay);
        netPay     = computeNetPay(grossPay, deductions);

        // Step 4: Render results into GUI text area
        String mName = monthName(month);
        output.append("============================================================\n");
        output.append("   MOTORPH PAYROLL REPORT — " + mName.toUpperCase() + " " + year + "\n");
        output.append("============================================================\n");
        output.append(String.format("%-8s %-25s %12s %12s %12s%n",
                "EMP #", "NAME", "GROSS PAY", "DEDUCTIONS", "NET PAY"));
        output.append("------------------------------------------------------------\n");

        double totalGross = 0, totalDeduc = 0, totalNet = 0;
        for (int i = 0; i < n; i++) {
            output.append(String.format("%-8s %-25s %12s %12s %12s%n",
                    ids[i],
                    names[i].length() > 24 ? names[i].substring(0, 24) : names[i],
                    String.format("PHP %,.2f", grossPay[i]),
                    String.format("PHP %,.2f", deductions[i]),
                    String.format("PHP %,.2f", netPay[i])));
            totalGross  += grossPay[i];
            totalDeduc  += deductions[i];
            totalNet    += netPay[i];
        }

        output.append("------------------------------------------------------------\n");
        output.append(String.format("%-34s %12s %12s %12s%n",
                "TOTALS (" + n + " employees)",
                String.format("PHP %,.2f", totalGross),
                String.format("PHP %,.2f", totalDeduc),
                String.format("PHP %,.2f", totalNet)));
        output.append("============================================================\n");
        output.append("Report generated for: " + mName + " " + year + "\n");

        // Update summary fields for the GUI summary bar
        summaryGross      = totalGross;
        summaryDeductions = totalDeduc;
        summaryNet        = totalNet;

        return new SalaryBatch(ids, names, daysWorked, ratePerDay, grossPay, deductions, netPay, month, year);
    }

    /**
     * Writes computed salary results back into the Employee Details CSV
     * by updating the Basic Salary column (index 13) with the computed gross pay.
     *
     * @param batch SalaryBatch produced by {@link #computeAllSalaries}
     * @return true if the file was successfully rewritten
     */
    public static boolean saveSalaryBatchToCSV(SalaryBatch batch) {
        if (batch == null) return false;
        java.util.List<String[]> all = FileHandlerModule.getAllEmployees();
        for (int i = 0; i < batch.ids.length; i++) {
            final String empId = batch.ids[i];
            final double gross = batch.grossPay[i];
            for (String[] row : all) {
                if (row.length > EmployeeModule.ID && row[EmployeeModule.ID].trim().equals(empId)) {
                    // Update Basic Salary column with computed gross pay
                    if (row.length > EmployeeModule.BASIC_SALARY) {
                        row[EmployeeModule.BASIC_SALARY] = String.format("%.2f", gross);
                    }
                    break;
                }
            }
        }
        return FileHandlerModule.rewriteEmployeeFile(all);
    }

    /**
     * Data transfer object holding the result arrays from a bulk salary computation run.
     */
    public static class SalaryBatch {
        public final String[] ids;
        public final String[] names;
        public final double[] daysWorked;
        public final double[] ratePerDay;
        public final double[] grossPay;
        public final double[] deductions;
        public final double[] netPay;
        public final String month;
        public final String year;

        public SalaryBatch(String[] ids, String[] names, double[] daysWorked,
                           double[] ratePerDay, double[] grossPay,
                           double[] deductions, double[] netPay,
                           String month, String year) {
            this.ids        = ids;
            this.names      = names;
            this.daysWorked = daysWorked;
            this.ratePerDay = ratePerDay;
            this.grossPay   = grossPay;
            this.deductions = deductions;
            this.netPay     = netPay;
            this.month      = month;
            this.year       = year;
        }
    }
}