
package MotorPH_EmployeeApp;

import java.io.*;
import java.util.*;

/**
 * FileHandlerModule
 * This module contains all the functions for 
 * interacting with the external CSV files. It handles file I/O and 
 * string parsing logic.
 */
public class FileHandlerModule {
    
    public static final String ATTENDANCE_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";
    public static final String EMPLOYEE_FILE = "resources/MotorPH_Employee Data - Employee Details.csv";

    /**
     * Searches for a specific employee ID within the Employee Details file.
     * Includes error handling for missing files or read errors.
     * Baeldung. Exception Handling in Java. 4.4 try-with-resources and 4.5. multiple catch (file not found/ file corrupted)
     */
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

     /**
     * Retrieves all attendance records for a specific employee.
     * Explains exactly what went wrong if the file cannot be accessed.
     * Baeldung. Exception Handling in Java. 4.4 try-with-resources and 4.5 multiple catch
     * GeeksforGeeks. ArrayList toArray() method in Java with Examples. Baeldung. Guide to the Java ArrayList.
     */
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

     /**
     * CSV Parser. Baeldung CSV File into Array 6.1
     * Iterates character by character to handle commas inside quotes.
     * Uses dynamic ArrayList. Baeldung. Guide to the Java ArrayList
     * @param line - single raw line of text from the CSV file.
     * @return the String array where each element represents a specific column.
     */
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