
package MotorPH_EmployeeApp;

import java.io.*;
import java.util.*;

public class FileHandlerModule {
    public static final String ATTENDANCE_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";
    public static final String EMPLOYEE_FILE = "resources/MotorPH_Employee Data - Employee Details.csv";

    public static String findEmployeeData(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = smartSplit(line);
                if (columns.length > 0 && columns[0].trim().equals(id.trim())) {
                    return line;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Employee file: " + e.getMessage());
        }
        return null;
    }

    public static List<String> findAttendanceData(String id) {
        List<String> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = smartSplit(line);
                if (columns.length > 0 && columns[0].trim().equals(id.trim())) {
                    records.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Attendance file.");
        }
        return records;
    }

    public static String[] smartSplit(String line) {
        if (line == null || line.isEmpty()) return new String[0];
        List<String> results = new ArrayList<>();
        StringBuilder tempText = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) {
                results.add(tempText.toString().trim());
                tempText.setLength(0);
            } else tempText.append(c);
        }
        results.add(tempText.toString().trim());
        return results.toArray(new String[0]);
    }
}