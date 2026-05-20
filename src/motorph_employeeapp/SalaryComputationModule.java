
package MotorPH_EmployeeApp;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * SalaryComputationModule
 * This module centralizes all calculation logic.
 */
public class SalaryComputationModule {

    /**
     * Payroll output engine.
     * Handles mathematical logic for shift calculation and government deductions.
     */
    public static void calculatePayroll(String[] emp, String month, String year, javax.swing.JTextArea output) {
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
            String[] dateParts = row[3].split("/"); 
            
            try {
                int inputMonth = Integer.parseInt(month);
                int inputYear = Integer.parseInt(year);
                int csvMonth = Integer.parseInt(dateParts[0]);
                int csvYear = Integer.parseInt(dateParts[2]);

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
                continue;
            }
        }
        
        // Calculate gross salaries
        double grossFirstCutoff = hoursFirstCutoff * hourlyRate;
        double grossSecondCutoff = hoursSecondCutoff * hourlyRate;
        double totalMonthlyGross = grossFirstCutoff + grossSecondCutoff;
        
        // Deduction logic
        double sss = computeSSS(totalMonthlyGross);
        double ph = computePhilHealth(totalMonthlyGross);
        double pi = computePagIBIG(totalMonthlyGross);
        double taxableIncome = totalMonthlyGross - (sss + ph + pi);
        double tax = calculateWithholdingTax(taxableIncome);
        double totalDeduc = sss + ph + pi + tax;
        
        double netSalary1 = grossFirstCutoff;
        double netSalary2 = grossSecondCutoff - totalDeduc;

        // --- OUTPUT REDIRECTED TO GUI BOX ---
        output.append("\n Employee #: " + id);
        output.append("\n Employee Name: " + EmployeeModule.fullName(emp));
        output.append("\n Birthday: " + emp[EmployeeModule.BIRTHDAY]);
        output.append("\n Cutoff Date: " + mName + " 1 to " + mName + " 15");
        output.append("\n Total Hours Worked: " + hoursFirstCutoff);
        output.append("\n Gross Salary: " + String.format("%.2f", grossFirstCutoff));
        output.append("\n Net Salary: " + String.format("%.2f", netSalary1));
        output.append("\n ");
        output.append("\n Cutoff Date: " + mName + " 16 to " + mName + " 31");
        output.append("\n Total Hours Worked: " + hoursSecondCutoff);
        output.append("\n Gross Salary: " + String.format("%.2f", grossSecondCutoff));
        output.append("\n Each Deduction:");
        output.append("\n    SSS: " + String.format("%.2f", sss));
        output.append("\n    PhilHealth: " + String.format("%.2f", ph));
        output.append("\n    Pag-IBIG: " + String.format("%.2f", pi));
        output.append("\n    Withholding Tax: " + String.format("%.2f", tax));
        output.append("\n Total Deductions: " + String.format("%.2f", totalDeduc));
        output.append("\n Net Salary: " + String.format("%.2f", netSalary2));
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
    
    /**
     * Simplified SSS Calculation using threshold loop for better readability.
     * @param salary - total monthly gross income.
     * @return the calculated SSS contribution amount.
     */
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

    /**
     * Computes PhilHealth contribution (Employee share).
     * Returns the 50% employee share of the PhilHealth premium.
     * @param salary - total monthly gross income.
     * @return the PhilHealth contribution amount.
     */
    public static double computePhilHealth(double salary) {
        double totalPremium;
        if (salary <= 10000) totalPremium = 300.0;
        else if (salary >= 60000) totalPremium = 1800.0;
        else totalPremium = salary * 0.03;
        return totalPremium / 2;
    }

    /**
     * Computes PagIBIG contribution with a max cap.
     * Applies PagIBIG rates with a contribution cap of 100.00
     * @param salary - total monthly gross income.
     * @return the PagIBIG contribution amount.
     */
    public static double computePagIBIG(double salary) {
        double employeeRate = (salary > 1500) ? 0.02 : 0.01;
        double total = (salary * employeeRate) + (salary * 0.02);
        return Math.min(total, 100.0);
    }

     /**
     * Computes Withholding Tax based on taxable income brackets.
     * Calculates tax after government deductions are subtracted from gross
     * @param taxableIncome - Gross salary minus SSS, PhilHealth, and PagIBIG.
     * @return the calculated withholding tax amount.
     */
    public static double calculateWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832) return 0;
        else if (taxableIncome < 33333) return (taxableIncome - 20833) * 0.20;
        else if (taxableIncome < 66667) return 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome < 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome < 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        else return 200833.33 + (taxableIncome - 666667) * 0.35;
    }
    
    /**
     * Helper to map month number to name.
     * switch expressions
     * @param monthStr - numeric month string 
     * @return the full name of the month 
     */
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
    
    /**
     * Helper to find MM/YYYY in attendance 
     * Uses simple ArrayList logic to avoid duplicates.
     * @param id Employee ID.
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
                    
                    if (!workingPeriods.contains(monthYear)) {
                        workingPeriods.add(monthYear);
                    }
                } catch (Exception e) { continue; }
            }
        }
        return workingPeriods;
    }
}