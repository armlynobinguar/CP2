
package motorph_employeeapp;

import java.io.*;
import java.util.*;

/**
 * FileHandlerModule
 * -----------------
 * Centralizes all CSV file I/O for the MotorPH Employee App.
 *
 * Responsibilities:
 * - Resolve data file paths regardless of working directory (IDE, project root,
 * or bin/).
 * - Look up employee master data and attendance time logs by employee ID.
 * - Parse CSV lines safely when fields contain commas inside quoted text.
 * - Append new employee records to the master CSV when needed.
 *
 * Data sources (under resources/):
 * - Employee Details CSV — master employee profile and hourly rate.
 * - Attendance Record CSV — daily login/logout entries per employee.
 */
public class FileHandlerModule {

    /** Relative path to the attendance time-log CSV file. */
    public static final String ATTENDANCE_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";

    /** Relative path to the employee master data CSV file. */
    public static final String EMPLOYEE_FILE = "resources/MotorPH_Employee Data - Employee Details.csv";

    /**
     * Resolves CSV paths whether the app is run from the project root, bin/, or
     * IDE.
     *
     * Search order:
     * 1. Path as given (relative to current working directory).
     * 2. user.dir + relativePath.
     * 3. user.dir/../relativePath (parent folder, e.g. when cwd is bin/).
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
     * @return the raw CSV line for that employee, or null if not found or on read
     *         error
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
     * Each line contains: Employee ID, name fields, date (MM/DD/YYYY), login time,
     * logout time.
     * Payroll logic filters these rows by month/year in
     * {@link SalaryComputationModule}.
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
     * Parses a CSV line into columns, respecting double-quoted fields.
     *
     * Standard String.split(",") breaks when address or name fields contain commas
     * inside quotes. This scanner toggles {@code inQuotes} on each {@code "}
     * character
     * and only splits on commas outside quotes.
     *
     * @param line one line from a CSV file (no trailing newline)
     * @return array of trimmed column values; empty array for null/empty input
     */
    public static String[] smartSplit(String line) {
        if (line == null || line.isEmpty())
            return new String[0];
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
        return results.toArray(new String[0]);
    }

    /**
     * Locates an employee by ID and updates their Address and Phone Number,
     * then rewrites the updated records list back onto the source CSV file.
     *
     * @param empId          The target employee ID to match
     * @param newAddress     The modified home address string
     * @param newPhoneNumber The modified contact phone string
     * @return true if the record was found and successfully written back to disk
     */
    public static boolean updateEmployeeContactInfo(String empId, String newAddress, String newPhoneNumber) {
        List<String[]> allRecords = getAllEmployees();
        boolean foundAndUpdated = false;

        for (int i = 0; i < allRecords.size(); i++) {
            String[] row = allRecords.get(i);
            // Match the target employee ID in the first column
            if (row.length > 0 && row[0].trim().equals(empId.trim())) {

                // Re-allocate array sizing if row lengths are shorter than expected
                if (row.length < 6) {
                    String[] largerRow = new String[19];
                    Arrays.fill(largerRow, "");
                    System.arraycopy(row, 0, largerRow, 0, row.length);
                    row = largerRow;
                }

                // Assign the newly edited table cell values to their designated CSV indexes
                row[4] = newAddress; // Address field
                row[5] = newPhoneNumber; // Phone Number field

                allRecords.set(i, row);
                foundAndUpdated = true;
                break;
            }
        }

        // Save the entire record array collection back onto the local file track if
        // updated
        if (foundAndUpdated) {
            return saveEntireDatabaseCollectionHelper(allRecords);
        }
        return false;
    }

    /**
     * Helper utility to securely rewrite the master collection database onto disk.
     */
    private static boolean saveEntireDatabaseCollectionHelper(List<String[]> recordsList) {
        String fileHeaderStr = "Employee ID,Last Name,First Name,Birthday,Address,Phone Number,SSS #,PhilHealth #,TIN,Pag-IBIG #,Status,Position,Immediate Supervisor,Basic Salary,Gross Semi-monthly Rate,Hourly Rate,Rice Subsidy,Phone Allowance,Clothing Allowance";
        String resolvedPath = resolveDataFile(EMPLOYEE_FILE);

        try (PrintWriter writerStream = new PrintWriter(new FileWriter(new java.io.File(resolvedPath), false))) {
            writerStream.println(fileHeaderStr);

            for (String[] lineRow : recordsList) {
                StringBuilder lineBuilder = new StringBuilder();
                for (int m = 0; m < lineRow.length; m++) {
                    String singleCellStr = lineRow[m];
                    if (singleCellStr == null)
                        singleCellStr = "";

                    // Wrap strings with commas in quotes to preserve structured columns
                    if (singleCellStr.contains(",")) {
                        singleCellStr = "\"" + singleCellStr + "\"";
                    }
                    lineBuilder.append(singleCellStr);
                    if (m < lineRow.length - 1) {
                        lineBuilder.append(",");
                    }
                }
                writerStream.println(lineBuilder.toString());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Critical failure during database file rewrite streams: " + e.getMessage());
            return false;
        }
    }
}