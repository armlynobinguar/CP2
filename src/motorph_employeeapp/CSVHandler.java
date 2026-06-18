package motorph_employeeapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV file I/O for payroll processing. Delegates to {@link FileHandlerModule}
 * to preserve the existing Employee Details CSV path and schema.
 */
public final class CSVHandler {

    public static final String CSV_PATH = FileHandlerModule.EMPLOYEE_FILE;

    private CSVHandler() {
    }

    public static List<Employee> loadEmployees() throws IOException {
        return loadEmployees(null, null);
    }

    /**
     * Loads employees and optionally refreshes days worked from attendance logs.
     */
    public static List<Employee> loadEmployees(String month, String year) throws IOException {
        FileHandlerModule.ensureEmployeeFileSchema();
        List<Employee> employees = new ArrayList<>();
        List<String[]> rows = FileHandlerModule.getAllEmployees();
        if (rows == null) {
            return employees;
        }

        int rowNum = 1;
        for (String[] row : rows) {
            rowNum++;
            if (row == null || row.length < 3) {
                continue;
            }
            try {
                String id = row[EmployeeModule.ID] == null ? "" : row[EmployeeModule.ID].trim();
                if (id.isEmpty()) {
                    continue;
                }
                String firstName = safe(row, EmployeeModule.FIRST_NAME);
                String lastName = safe(row, EmployeeModule.LAST_NAME);
                String dept = safe(row, EmployeeModule.DEPARTMENT);
                String status = safe(row, EmployeeModule.STATUS);
                double ratePerDay = EmployeeModule.getHourlyRate(row);
                double daysWorked = 0;
                if (month != null && year != null && !month.trim().isEmpty() && !year.trim().isEmpty()) {
                    daysWorked = SalaryComputationModule.sumAttendanceHours(id, month, year);
                } else if (row.length > EmployeeModule.HOURS_WORKED) {
                    daysWorked = parseDouble(safe(row, EmployeeModule.HOURS_WORKED));
                }
                employees.add(new Employee(id, firstName, lastName, dept, status, ratePerDay, daysWorked));
            } catch (NumberFormatException ex) {
                System.err.println("Skipped row " + rowNum + ": " + ex.getMessage());
            }
        }
        return employees;
    }

    public static void savePayrollResults(List<PayrollResult> results) throws IOException {
        if (results == null || results.isEmpty()) {
            return;
        }
        FileHandlerModule.ensureEmployeeFileSchema();
        List<String[]> employees = new ArrayList<>(FileHandlerModule.getAllEmployees());
        for (PayrollResult result : results) {
            for (int i = 0; i < employees.size(); i++) {
                String[] row = employees.get(i);
                if (row == null || row.length == 0) {
                    continue;
                }
                String id = safe(row, EmployeeModule.ID);
                if (!id.equals(result.getEmployeeId())) {
                    continue;
                }
                row = FileHandlerModule.normalizeEmployeeRow(row);
                row[EmployeeModule.GROSS_PAY] = format(result.getGrossPay());
                row[EmployeeModule.TOTAL_DEDUCTIONS] = format(result.getTotalDeductions());
                row[EmployeeModule.NET_PAY] = format(result.getNetPay());
                employees.set(i, row);
                break;
            }
        }
        if (!FileHandlerModule.rewriteEmployeeFile(employees)) {
            throw new IOException("Could not write payroll results to " + CSV_PATH);
        }
    }

    public static List<String> distinctDepartments(List<Employee> employees) {
        List<String> departments = new ArrayList<>();
        departments.add("All Departments");
        if (employees == null) {
            return departments;
        }
        for (Employee employee : employees) {
            String dept = employee.getDepartment();
            if (dept != null && !dept.trim().isEmpty() && !departments.contains(dept)) {
                departments.add(dept);
            }
        }
        return departments;
    }

    private static String safe(String[] row, int index) {
        return row.length > index && row[index] != null ? row[index].trim() : "";
    }

    private static double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        return Double.parseDouble(value.replace(",", "").trim());
    }

    private static String format(double value) {
        return String.format("%.2f", value);
    }
}
