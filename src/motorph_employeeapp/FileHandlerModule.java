
package motorph_employeeapp;

import java.io.*;
import java.nio.file.*;
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

    /** Optional override used by unit tests to avoid touching bundled project data. */
    private static String testEmployeeFileOverride = null;

    static void setTestEmployeeFileOverrideForTests(String absolutePath) {
        testEmployeeFileOverride = absolutePath;
    }

    static void clearTestEmployeeFileOverrideForTests() {
        testEmployeeFileOverride = null;
    }

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
        if (testEmployeeFileOverride != null && EMPLOYEE_FILE.equals(relativePath)) {
            return testEmployeeFileOverride;
        }
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
                    employees.add(normalizeEmployeeRow(smartSplit(line)));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading entire Employee Database: " + e.getMessage());
        }
        return employees;
    }

    /**
     * Returns the next available numeric employee ID (max existing ID + 1).
     */
    public static String getNextEmployeeNumber() {
        int max = 10000;
        for (String[] emp : getAllEmployees()) {
            if (emp == null || emp.length == 0) {
                continue;
            }
            try {
                int id = Integer.parseInt(emp[EmployeeModule.ID].trim());
                if (id > max) {
                    max = id;
                }
            } catch (NumberFormatException ignored) {
                // skip malformed IDs
            }
        }
        return String.valueOf(max + 1);
    }

    /** Canonical header row for the Employee Details CSV (24 columns). */
    public static final String EMPLOYEE_FILE_HEADER =
            "Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,"
            + "Status,Position,Department,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,"
            + "Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate,Hours Worked,Gross Pay,Total Deductions,Net Pay";

    /**
     * Upgrades legacy CSV rows to the current 24-column schema and rewrites the file when needed.
     * Called at startup and before bulk payroll to ensure {@link EmployeeModule#COLUMN_COUNT} columns exist.
     */
    public static void ensureEmployeeFileSchema() {
        List<String[]> rawRows = new ArrayList<>();
        boolean needsMigration = false;
        try (BufferedReader br = new BufferedReader(new FileReader(resolveDataFile(EMPLOYEE_FILE)))) {
            String header = br.readLine();
            if (header == null || !header.toLowerCase().contains("department")
                    || !header.toLowerCase().contains("gross pay")) {
                needsMigration = true;
            }
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] split = smartSplit(line);
                    if (split.length < EmployeeModule.COLUMN_COUNT) {
                        needsMigration = true;
                    }
                    rawRows.add(split);
                }
            }
        } catch (IOException e) {
            System.out.println("Error checking employee schema: " + e.getMessage());
            return;
        }
        if (!needsMigration) {
            return;
        }
        List<String[]> migrated = new ArrayList<>();
        for (String[] row : rawRows) {
            migrated.add(normalizeEmployeeRow(row));
        }
        rewriteEmployeeFile(migrated);
    }

    /**
     * Pads or migrates one employee row to {@link EmployeeModule#COLUMN_COUNT} columns.
     */
    public static String[] normalizeEmployeeRow(String[] row) {
        String[] normalized = new String[EmployeeModule.COLUMN_COUNT];
        if (row == null) {
            return normalized;
        }
        if (row.length >= EmployeeModule.COLUMN_COUNT) {
            for (int i = 0; i < EmployeeModule.COLUMN_COUNT; i++) {
                normalized[i] = row[i] == null ? "" : row[i].trim();
            }
            if (normalized[EmployeeModule.DEPARTMENT].isEmpty()) {
                normalized[EmployeeModule.DEPARTMENT] =
                        DepartmentModule.inferDepartmentFromPosition(normalized[EmployeeModule.POSITION]);
            }
            return normalized;
        }
        if (row.length > EmployeeModule.HOURLY_RATE) {
            for (int i = 0; i <= EmployeeModule.HOURLY_RATE; i++) {
                normalized[i] = row[i] == null ? "" : row[i].trim();
            }
            if (normalized[EmployeeModule.DEPARTMENT].isEmpty()) {
                normalized[EmployeeModule.DEPARTMENT] =
                        DepartmentModule.inferDepartmentFromPosition(normalized[EmployeeModule.POSITION]);
            }
            for (int i = EmployeeModule.HOURS_WORKED; i < EmployeeModule.COLUMN_COUNT; i++) {
                normalized[i] = "";
            }
            return normalized;
        }
        int copyThrough = Math.min(row.length, EmployeeModule.POSITION + 1);
        for (int i = 0; i < copyThrough; i++) {
            normalized[i] = row[i] == null ? "" : row[i].trim();
        }
        normalized[EmployeeModule.DEPARTMENT] =
                DepartmentModule.inferDepartmentFromPosition(normalized[EmployeeModule.POSITION]);
        int legacySupervisorIndex = 12;
        for (int legacy = legacySupervisorIndex; legacy < row.length; legacy++) {
            int target = legacy + 1;
            if (target < EmployeeModule.COLUMN_COUNT) {
                normalized[target] = row[legacy] == null ? "" : row[legacy].trim();
            }
        }
        for (int i = 0; i < EmployeeModule.COLUMN_COUNT; i++) {
            if (normalized[i] == null) {
                normalized[i] = "";
            }
        }
        return normalized;
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
     * Reads the current file, appends the row, and rewrites atomically so partial
     * writes cannot corrupt the master CSV.
     *
     * @param rawCsvLine complete comma-separated row (no header)
     * @return true on success, false if IOException occurs
     */
    public static boolean appendEmployeeRecord(String rawCsvLine) {
        if (rawCsvLine == null || rawCsvLine.trim().isEmpty()) {
            return false;
        }
        List<String[]> all = getAllEmployees();
        String[] newRow = normalizeEmployeeRow(smartSplit(rawCsvLine));
        EmployeeRecordsModule.clearPayrollOutputColumns(newRow);
        all.add(newRow);
        return rewriteEmployeeFile(all);
    }

    /**
     * Reads the header line from the Employee Details CSV.
     *
     * @return header row text, or a default header if the file cannot be read
     */
    public static String getEmployeeFileHeader() {
        String filePath = resolveDataFile(EMPLOYEE_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            if (header != null && !header.trim().isEmpty()) {
                return header;
            }
        } catch (IOException e) {
            System.out.println("Error reading employee header: " + e.getMessage());
        }
        return EMPLOYEE_FILE_HEADER;
    }

    /**
     * Rewrites the entire Employee Details CSV from an in-memory list of rows.
     *
     * @param employees ordered employee rows (each row must match CSV column count)
     * @return true when the file is written successfully
     */
    public static boolean rewriteEmployeeFile(List<String[]> employees) {
        List<String> lines = new ArrayList<>();
        lines.add(EMPLOYEE_FILE_HEADER);
        if (employees != null) {
            for (String[] row : employees) {
                String[] normalized = normalizeEmployeeRow(row);
                EmployeeRecordsModule.clearPayrollOutputColumns(normalized);
                lines.add(joinCsvLine(normalized));
            }
        }
        return writeTextFileAtomically(resolveDataFile(EMPLOYEE_FILE), lines, true);
    }

    /**
     * Writes text lines to a temp file, optionally backs up the target, then replaces atomically.
     */
    static boolean writeTextFileAtomically(String filePath, List<String> lines, boolean createBackup) {
        if (filePath == null || lines == null) {
            return false;
        }
        File target = new File(filePath);
        File parent = target.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        File temp = new File(target.getAbsolutePath() + ".tmp");
        File backup = new File(target.getAbsolutePath() + ".bak");
        try {
            if (createBackup && target.isFile()) {
                Files.copy(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(temp, false)))) {
                for (String line : lines) {
                    out.println(line == null ? "" : line);
                }
            }
            if (target.isFile() && !target.delete()) {
                temp.delete();
                return false;
            }
            if (!temp.renameTo(target)) {
                Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return target.isFile();
        } catch (IOException e) {
            System.out.println("Error writing file atomically (" + filePath + "): " + e.getMessage());
            temp.delete();
            return false;
        }
    }

    /**
     * Replaces one employee row in the CSV by employee ID.
     *
     * @param id         employee number to update
     * @param updatedRow complete replacement row
     * @return true when the record exists and the file is saved
     */
    public static boolean updateEmployeeRecord(String id, String[] updatedRow) {
        if (id == null || updatedRow == null || updatedRow.length == 0) {
            return false;
        }
        List<String[]> all = getAllEmployees();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).length > 0 && all.get(i)[0].trim().equals(id.trim())) {
                all.set(i, updatedRow);
                found = true;
                break;
            }
        }
        return found && rewriteEmployeeFile(all);
    }

    /**
     * Removes one employee row from the CSV by employee ID.
     *
     * @param id employee number to delete
     * @return true when the record exists and the file is saved
     */
    public static boolean deleteEmployeeRecord(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        List<String[]> all = getAllEmployees();
        boolean removed = all.removeIf(row -> row.length > 0 && row[0].trim().equals(id.trim()));
        return removed && rewriteEmployeeFile(all);
    }

    /**
     * Serializes one CSV row, quoting fields that contain commas or quotes.
     *
     * @param columns split employee row
     * @return comma-separated line suitable for writing to the CSV file
     */
    public static String joinCsvLine(String[] columns) {
        if (columns == null || columns.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            String value = columns[i] == null ? "" : columns[i];
            if (value.contains(",") || value.contains("\"")) {
                sb.append('"').append(value.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(value);
            }
        }
        return sb.toString();
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
        List<String> lines = new ArrayList<>();
        if (items != null) {
            for (String s : items) {
                lines.add(s == null ? "" : s);
            }
        }
        return writeTextFileAtomically(resolveDataFile("resources/notifications.txt"), lines, true);
    }

    /**
     * Loads notifications from {@code resources/notifications.txt} as structured objects.
     *
     * @return parsed list; empty when file is missing or every line is malformed
     */
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

    /**
     * Overwrites {@code resources/notifications.txt} with serialized notification lines.
     *
     * @param items current in-memory notification list from the GUI
     * @return {@code true} on successful write
     */
    public static boolean saveStructuredNotifications(java.util.List<NotificationModule.Notification> items) {
        List<String> lines = new ArrayList<>();
        if (items != null) {
            for (NotificationModule.Notification n : items) {
                lines.add(n == null ? "" : n.serializeLine());
            }
        }
        return writeTextFileAtomically(resolveDataFile("resources/notifications.txt"), lines, true);
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

    /** Relative path for employee-submitted payslip issue reports. */
    public static final String PAYSLIP_ISSUES_FILE = "resources/payslip_issues.txt";

    private static File resolvePayslipIssuesFile() {
        File direct = new File(PAYSLIP_ISSUES_FILE);
        if (direct.getParentFile() != null && !direct.getParentFile().exists()) {
            direct.getParentFile().mkdirs();
        }
        if (direct.getParentFile() != null && direct.getParentFile().exists()) {
            return direct;
        }
        String userDir = System.getProperty("user.dir");
        File fromCwd = new File(userDir, PAYSLIP_ISSUES_FILE);
        if (fromCwd.getParentFile() != null && !fromCwd.getParentFile().exists()) {
            fromCwd.getParentFile().mkdirs();
        }
        return fromCwd;
    }

    /** Loads all payslip issue reports from disk (file order, oldest first). */
    public static java.util.List<PayslipIssueModule.PayslipIssue> loadPayslipIssues() {
        java.util.List<PayslipIssueModule.PayslipIssue> list = new ArrayList<>();
        File target = resolvePayslipIssuesFile();
        if (!target.isFile()) {
            return list;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(target))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                PayslipIssueModule.PayslipIssue issue = PayslipIssueModule.PayslipIssue.parseLine(line);
                if (issue != null) {
                    list.add(issue);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading payslip issues: " + e.getMessage());
        }
        return list;
    }

    /** Rewrites the payslip issues file after HR updates status or resolution notes. */
    public static boolean savePayslipIssues(java.util.List<PayslipIssueModule.PayslipIssue> issues) {
        List<String> lines = new ArrayList<>();
        if (issues != null) {
            for (PayslipIssueModule.PayslipIssue issue : issues) {
                if (issue != null) {
                    lines.add(issue.serializeLine());
                }
            }
        }
        return writeTextFileAtomically(resolvePayslipIssuesFile().getPath(), lines, true);
    }

    /** Open or in-progress payslip reports — used for HR sidebar badge counts. */
    public static int countPayslipIssuesNeedingAction() {
        int count = 0;
        for (PayslipIssueModule.PayslipIssue issue : loadPayslipIssues()) {
            if (issue.needsHrAction()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Appends one employee payslip issue report (status defaults to Open).
     */
    public static boolean appendPayslipIssueReport(String employeeId, String employeeName,
            String payPeriod, String issueType, String description) {
        PayslipIssueModule.PayslipIssue issue = new PayslipIssueModule.PayslipIssue(
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                employeeId, employeeName, payPeriod, issueType, description);

        File target = resolvePayslipIssuesFile();
        try (java.io.FileWriter fw = new java.io.FileWriter(target, true);
                java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
            bw.write(issue.serializeLine());
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Error saving payslip issue report: " + e.getMessage());
            return false;
        }
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
