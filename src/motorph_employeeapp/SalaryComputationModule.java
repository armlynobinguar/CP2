
package MotorPH_EmployeeApp;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalaryComputationModule {

    /**
     * Payroll output engine.
     * Handles mathematical logic for shift calculation and government deductions.
     */
    public static void calculatePayroll(String[] emp, String month, String year) {
        // Validation: ensures the array has enough data
        if (emp == null || emp.length < 19 || emp[0].isEmpty()) return;

        String id = emp[EmployeeModule.ID];
        double hourlyRate = EmployeeModule.getHourlyRate(emp);
        String mName = monthName(month);
        
        double hoursFirstCutoff = 0;
        double hoursSecondCutoff = 0;
        
        // Retrieve attendance records for the specific ID
        List<String> records = FileHandlerModule.findAttendanceData(id);

        for (String line : records) {
            String[] row = FileHandlerModule.smartSplit(line);
            String[] dateParts = row[3].split("/"); // Expected Format: MM/DD/YYYY
            
            try {
                // Parse strings to integers to handle leading zeros (e.g., "08" == 8)
                int inputMonth = Integer.parseInt(month);
                int inputYear = Integer.parseInt(year);
                int csvMonth = Integer.parseInt(dateParts[0]);
                int csvYear = Integer.parseInt(dateParts[2]);

                // Comparison based on numeric value rather than string literal
                if (csvMonth == inputMonth && csvYear == inputYear) {
                    int day = Integer.parseInt(dateParts[1]);
                    double shift = calculateShift(row[4], row[5]);
                    
                    if (day <= 15) {
                        hoursFirstCutoff += shift;
                    } else {
                        hoursSecondCutoff += shift;
                    }
                }
            } catch (Exception e) {
                // Skips any row with a malformed date or empty column
                continue;
            }
        }
        
        // Calculate gross salaries
        double grossFirstCutoff = hoursFirstCutoff * hourlyRate;
        double grossSecondCutoff = hoursSecondCutoff * hourlyRate;
        double totalMonthlyGross = grossFirstCutoff + grossSecondCutoff;
        
        // Deduction logic (Based on total monthly gross)
        double sss = computeSSS(totalMonthlyGross);
        double ph = computePhilHealth(totalMonthlyGross);
        double pi = computePagIBIG(totalMonthlyGross);
        double taxableIncome = totalMonthlyGross - (sss + ph + pi);
        double tax = calculateWithholdingTax(taxableIncome);
        double totalDeduc = sss + ph + pi + tax;
        
        // Split net salary distribution
        double netSalary1 = grossFirstCutoff;
        double netSalary2 = grossSecondCutoff - totalDeduc;

        // Visual Output (Sent to GUI via TextAreaOutputStream redirect)
        System.out.println("\n---------------------------------------------");
        System.out.println(" Employee #: " + id);
        System.out.println(" Employee Name: " + EmployeeModule.fullName(emp));
        System.out.println(" Birthday: " + emp[EmployeeModule.BIRTHDAY]);
        System.out.println(" Cutoff Date: " + mName + " 1 to " + mName + " 15");
        System.out.println(" Total Hours Worked: " + hoursFirstCutoff);
        System.out.println(" Gross Salary: " + grossFirstCutoff);
        System.out.println(" Net Salary: " + netSalary1);
        System.out.println(" \nCutoff Date: " + mName + " 16 to " + mName + " 31 (Deductions Applied)");
        System.out.println(" Total Hours Worked: " + hoursSecondCutoff);
        System.out.println(" Gross Salary: " + grossSecondCutoff);
        System.out.println(" Each Deduction:");
        System.out.println("    SSS: " + sss);
        System.out.println("    PhilHealth: " + ph);
        System.out.println("    Pag-IBIG: " + pi);
        System.out.println("    Tax: " + tax);
        System.out.println(" Total Deductions: " + totalDeduc);
        System.out.println(" Net Salary: " + netSalary2);
        System.out.println("---------------------------------------------");
    }

    public static double calculateShift(String logIn, String logOut) {
        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("H:mm");
            LocalTime timeIn = LocalTime.parse(logIn, format);
            LocalTime timeOut = LocalTime.parse(logOut, format);
            LocalTime graceLimit = LocalTime.of(8, 10);
            LocalTime startLimit = LocalTime.of(8, 0);
            LocalTime endLimit = LocalTime.of(17, 0);
            
            LocalTime actualStart = timeIn.isAfter(graceLimit) ? timeIn : startLimit;
            LocalTime actualEnd = timeOut.isAfter(endLimit) ? endLimit : timeOut;

            if (actualStart.isAfter(actualEnd)) return 0;

            int startMins = actualStart.getHour() * 60 + actualStart.getMinute();
            int endMins = actualEnd.getHour() * 60 + actualEnd.getMinute();
            
            return Math.max(0, (endMins - startMins - 60) / 60.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static double computeSSS(double salary) {
        if (salary < 3250) return 135.00;
        if (salary >= 24750) return 1125.00;
        double threshold = 3250;
        double contribution = 157.50;
        while (threshold < 24750) {
            if (salary >= threshold && salary <= threshold + 499.99) return contribution;
            threshold += 500;
            contribution += 22.50;
        }
        return 1125.00;
    }

    public static double computePhilHealth(double salary) {
        double totalPremium;
        if (salary <= 10000) totalPremium = 300.0;
        else if (salary >= 60000) totalPremium = 1800.0;
        else totalPremium = salary * 0.03;
        return totalPremium / 2;
    }

    public static double computePagIBIG(double salary) {
        double employeeRate = (salary > 1500) ? 0.02 : 0.01;
        double total = (salary * employeeRate) + (salary * 0.02);
        return Math.min(total, 100.0);
    }

    public static double calculateWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) return 0;
        else if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        else if (taxableIncome < 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome < 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        else return 200833.33 + (taxableIncome - 666667) * 0.35;
    }

    public static String monthName(String monthStr) {
        try {
            int month = Integer.parseInt(monthStr);
            return switch (month) {
                case 1 -> "January"; case 2 -> "February"; case 3 -> "March";
                case 4 -> "April"; case 5 -> "May"; case 6 -> "June";
                case 7 -> "July"; case 8 -> "August"; case 9 -> "September";
                case 10 -> "October"; case 11 -> "November"; case 12 -> "December";
                default -> "Month " + month;
            };
        } catch (Exception e) { return "Invalid Month"; }
    }
    
    public static List<String> findWorkingPeriods(String id) {
        List<String> workingPeriods = new java.util.ArrayList<>();
        List<String> records = FileHandlerModule.findAttendanceData(id);
        
        for (String record : records) {
            String[] columns = FileHandlerModule.smartSplit(record);
            String[] dateParts = columns[3].split("/"); 
            
            if (dateParts.length >= 3) {
                try {
                    // Normalize month/year during discovery as well
                    int m = Integer.parseInt(dateParts[0]);
                    int y = Integer.parseInt(dateParts[2]);
                    String monthYear = m + "/" + y;
                    
                    if (!workingPeriods.contains(monthYear)) {
                        workingPeriods.add(monthYear);
                    }
                } catch (Exception e) { continue; }
            }
        }
        return workingPeriods;
    }
}