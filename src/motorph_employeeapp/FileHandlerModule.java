package motorph_employeeapp;

import java.io.*;
import java.util.*;

/**
 * FileHandlerModule
 * -----------------
 * Centralizes all CSV file I/O for the MotorPH Employee App.
 *
 * Responsibilities:
 *   - Resolve data file paths regardless of working directory (IDE, project root, or bin/).
 *   - Look up employee master data and attendance time logs by employee ID.
 *   - Parse CSV lines safely when fields contain commas inside quoted text.
 *   - Append new employee records to the master CSV when needed.
 *
 * Data sources (under resources/):
 *   - Employee Details CSV  — master employee profile and hourly rate.
 *   - Attendance Record CSV — daily login/logout entries per employee.
 */
public class FileHandlerModule {

    /** Relative path to the attendance time-log CSV file. */
    public static final String ATTENDANCE_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";

    /** Relative path to the employee master data CSV file. */
    public static final String EMPLOYEE_FILE = "resources/MotorPH_Employee Data - Employee Details.csv";

    /**
     * Resolves CSV paths whether the app is run from the project root, bin/, or IDE.
     *
     * Search order:
     *   1. Path as given (relative to current working directory).
     *   2. user.dir + relativePath.
     *   3. user.dir/../relativePath (parent folder, e.g. when cwd is bin/).
     *
     * @param relativePath path such as "resources/...csv"
     * @return absolute or relative + relative path string for FileReader
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
        // Fall back to original path; caller may hit IOException on open
        return relativePath;
    }

    /**
     * Returns true when the given ID exists in the Employee Details CSV (column 0).
     *
     * @param id employee number to check (e.g. "10001")
     * @return true if a matching row exists
     */
    public static boolean employeeExists(String id) {
        return findEmployeeData(id) != null;
    }

    /**
     * Searches the Employee Details CSV for a single row matching the employee ID.
     *
     * @param id employee number; null or blank returns null immediately
     * @return the raw CSV line for that employee, or null if not found or on read error
     */
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
                // First column is always Employee ID
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
     * Retrieves all attendance rows for one employee from the Attendance CSV.
     *
     * Each line contains: Employee ID, name fields, date (MM/DD/YYYY), login time, logout time.
     * Payroll logic filters these rows by month/year in {@link SalaryComputationModule}.
     *
     * @param id employee number to match against column 0
     * @return list of raw CSV lines (empty list if none found or on error)
     */
    public static List<String> findAttendanceData(String id) {
        List<String> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(resolveDataFile(ATTENDANCE_FILE)))) {
            br.readLine(); // Skip header row
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
     * Reads all raw employee lines from the CSV file (skipping the header).
     * Useful for filling tabular views or bulk processing.
     *
     * @return list of split employee rows; empty on read failure
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
     * Reads all attendance rows from the attendance CSV (skipping header) and
     * returns them split into columns using {@link #smartSplit}.
     *
     * @return list of split attendance rows; empty on read failure
     */
    public static List<String[]> getAllAttendanceRecords() {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(resolveDataFile(ATTENDANCE_FILE)))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    records.add(smartSplit(line));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Attendance file: " + e.getMessage());
        }
        return records;
    }

    /**
     * Appends a newly validated employee record line to the CSV file.
     *
     * Opens the file in append mode, writes a newline, then the raw CSV line.
     *
     * @param rawCsvLine complete comma-separated row (no header)
     * @return true on success, false if IOException occurs
     */
    public static boolean appendEmployeeRecord(String rawCsvLine) {
        try (FileWriter fw = new FileWriter(resolveDataFile(EMPLOYEE_FILE), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(); // Ensure record starts on a new line after existing data
            out.print(rawCsvLine);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving employee details to file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads application notifications from a simple text file (one notification per line).
     * The file is stored under {@code resources/notifications.txt} so it travels with the project.
     * If the file does not exist an empty list is returned.
     *
     * @return list of notification lines (may be empty)
     */
    public static List<String> loadNotifications() {
        List<String> items = new ArrayList<>();
        String path = resolveDataFile("resources/notifications.txt");
        File f = new File(path);
        if (!f.isFile()) return items;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) items.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading notifications: " + e.getMessage());
        }
        return items;
    }

    /**
     * Saves the current notifications list to disk overwriting the previous file.
     * This is a simple persistence mechanism used by the GUI to remember dismissed
     * or marked notifications between runs.
     *
     * @param items ordered list of notification lines
     * @return true on success
     */
    public static boolean saveNotifications(List<String> items) {
        String path = resolveDataFile("resources/notifications.txt");
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
            for (String s : items) {
                out.println(s == null ? "" : s);
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error saving notifications: " + e.getMessage());
            return false;
        }
    }

    /** Structured notification load/save using NotificationModule.Notification */
    public static java.util.List<NotificationModule.Notification> loadStructuredNotifications() {
        java.util.List<NotificationModule.Notification> list = new ArrayList<>();
        String path = resolveDataFile("resources/notifications.txt");
        File f = new File(path);
        if (!f.isFile()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                NotificationModule.Notification n = NotificationModule.Notification.parseLine(line);
                if (n != null) list.add(n);
            }
        } catch (IOException e) {
            System.out.println("Error reading structured notifications: " + e.getMessage());
        }
        return list;
    }

    public static boolean saveStructuredNotifications(java.util.List<NotificationModule.Notification> items) {
        String path = resolveDataFile("resources/notifications.txt");
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
            for (NotificationModule.Notification n : items) {
                out.println(n == null ? "" : n.serializeLine());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error saving structured notifications: " + e.getMessage());
            return false;
        }
    }

    /** Export structured notifications to an explicit relative path. */
    public static boolean exportStructuredNotifications(java.util.List<NotificationModule.Notification> items, String relativePath) {
        String path = resolveDataFile(relativePath);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
            for (NotificationModule.Notification n : items) out.println(n == null ? "" : n.serializeLine());
            return true;
        } catch (IOException e) {
            System.out.println("Error exporting notifications: " + e.getMessage());
            return false;
        }
    }

    /** Load structured notifications from an explicit file path. */
    public static java.util.List<NotificationModule.Notification> loadStructuredNotificationsFromPath(String filePath) {
        java.util.List<NotificationModule.Notification> list = new ArrayList<>();
        File f = new File(filePath);
        if (!f.isFile()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                NotificationModule.Notification n = NotificationModule.Notification.parseLine(line);
                if (n != null) list.add(n);
            }
        } catch (IOException e) {
            System.out.println("Error loading notifications from path: " + e.getMessage());
        }
        return list;
    }

    /**
     * Joins a String[] row back into a single CSV line.
     * Wraps fields containing commas in double quotes.
     *
     * @param columns array of field values
     * @return comma-separated string suitable for writing to CSV
     */
    public static String joinCsvLine(String[] columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            String val = columns[i] == null ? "" : columns[i];
            // Wrap in quotes if the value contains a comma
            if (val.contains(",")) {
                sb.append("\"").append(val).append("\"");
            } else {
                sb.append(val);
            }
            if (i < columns.length - 1) sb.append(",");
        }
        return sb.toString();
    }

    /**
     * Rewrites the entire Employee Details CSV with the provided list of rows.
     * Preserves the original header row and writes all data rows below it.
     *
     * @param rows list of split employee rows to write back
     * @return true on success, false if IOException occurs
     */
    public static boolean rewriteEmployeeFile(List<String[]> rows) {
        String filePath = resolveDataFile(EMPLOYEE_FILE);
        // Read the original header first
        String header = "";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            header = br.readLine();
        } catch (IOException e) {
            System.out.println("Error reading header: " + e.getMessage());
            return false;
        }
        // Rewrite file with header + all updated rows
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))) {
            out.println(header);
            for (String[] row : rows) {
                out.println(joinCsvLine(row));
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error rewriting Employee file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates a single employee record in the CSV by matching on employee ID.
     * Rewrites the entire file with the updated row in place.
     *
     * @param originalId the employee ID to find and replace
     * @param updatedRow the new column values for that employee
     * @return true if found and updated, false otherwise
     */
    public static boolean updateEmployeeRecord(String originalId, String[] updatedRow) {
        List<String[]> all = getAllEmployees();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            String[] row = all.get(i);
            if (row.length > 0 && row[0].trim().equals(originalId.trim())) {
                all.set(i, updatedRow); // Replace the matching row
                found = true;
                break;
            }
        }
        if (!found) return false;
        return rewriteEmployeeFile(all);
    }

    /**
     * Deletes a single employee record from the CSV by employee ID.
     * Rewrites the entire file without the deleted row.
     *
     * @param id the employee ID to remove
     * @return true if found and deleted, false otherwise
     */
    public static boolean deleteEmployeeRecord(String id) {
        List<String[]> all = getAllEmployees();
        boolean found = false;
        List<String[]> remaining = new ArrayList<>();
        for (String[] row : all) {
            if (row.length > 0 && row[0].trim().equals(id.trim())) {
                found = true; // Skip this row — it's being deleted
            } else {
                remaining.add(row);
            }
        }
        if (!found) return false;
        return rewriteEmployeeFile(remaining);
    }

    /**
     * Parses a CSV line into columns, respecting double-quoted fields.
     *
     * Standard String.split(",") breaks when address or name fields contain commas
     * inside quotes. This scanner toggles {@code inQuotes} on each {@code "} character
     * and only splits on commas outside quotes.
     *
     * @param line one line from a CSV file (no trailing newline)
     * @return array of trimmed column values; empty array for null/empty input
     */
    public static String[] smartSplit(String line) {
        if (line == null || line.isEmpty()) return new String[0];
        List<String> results = new ArrayList<>();
        StringBuilder tempText = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                results.add(tempText.toString().trim());
                tempText.setLength(0);
            } else {
                tempText.append(c);
            }
        }
        // Add the final column after the last comma (or entire line if no commas)
        results.add(tempText.toString().trim());
        String[] out = new String[results.size()];
        return results.toArray(out);
    }
}