
package motorph_employeeapp;

import java.io.*;
import java.util.*;

/**
 * FileHandlerModule
 * This module handles file I/O operations including data reads, 
 * searching records, and safe appending.
 */
public class FileHandlerModule {
    
    public static final String ATTENDANCE_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";
    public static final String EMPLOYEE_FILE = "resources/MotorPH_Employee Data - Employee Details.csv";

    /**
     * Resolves CSV paths whether the app is run from the project root, bin/, or IDE.
     */
    private static String resolveDataFile(String relativePath) {
        File direct = new File(relativePath);
        if (direct.isFile()) {
            return direct.getPath();
        }
        String userDir = System.getProperty("user.dir");
        File fromCwd = new File(userDir, relativePath);
        if (fromCwd.isFile()) {
            return fromCwd.getPath();
        }
        File fromParent = new File(userDir, "../" + relativePath);
        if (fromParent.isFile()) {
            return fromParent.getAbsolutePath();
        }
        return relativePath;
    }

    /**
     * Returns true when the given ID exists in the Employee Details CSV (column 0).
     */
    public static boolean employeeExists(String id) {
        return findEmployeeData(id) != null;
    }

    public static String findEmployeeData(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        String filePath = resolveDataFile(EMPLOYEE_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip CSV header row
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
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
        try (BufferedReader br = new BufferedReader(new FileReader(resolveDataFile(ATTENDANCE_FILE)))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = smartSplit(line);
                if (columns.length > 0 && columns[0].trim().equals(id.trim())) {
                    // FIX: Add the line to the list instead of returning a single String
                    records.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Attendance file.");
        }
        return records;
    }

    /**
     * Reads all raw employee lines from the CSV file (skipping the header).
     * Useful for filling up tabular views.
     */
    public static List<String[]> getAllEmployees() {
        List<String[]> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(resolveDataFile(EMPLOYEE_FILE)))) {
            br.readLine(); // Skip CSV Header
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    employees.add(smartSplit(line));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading entire Employee Database: " + e.getMessage());
        }
        return employees;
    }

    /**
     * Appends a newly validated employee record line to the CSV file.
     */
    public static boolean appendEmployeeRecord(String rawCsvLine) {
        try (FileWriter fw = new FileWriter(resolveDataFile(EMPLOYEE_FILE), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(); // Ensure it writes to a brand new line
            out.print(rawCsvLine);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving employee details to file: " + e.getMessage());
            return false;
        }
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