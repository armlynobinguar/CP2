package motorph_employeeapp;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DepartmentModule
 * ----------------
 * Canonical department and position lists for HR Add/Edit employee forms, plus logic to
 * infer department from legacy job titles and resolve immediate supervisor display names.
 *
 * <p>Supervisor names are looked up from live CSV data when possible; hard-coded fallbacks
 * match the MotorPH sample dataset when a position holder is not found.</p>
 */
public class DepartmentModule {

    /** All selectable departments in HR employee record forms (insertion order preserved). */
    public static final String[] DEPARTMENTS = {
        "Executive",
        "IT",
        "Human Resources",
        "Accounting",
        "Sales & Accounts",
        "Marketing",
        "Operations",
        "Customer Service"
    };

    /**
     * Maps each department name to valid job titles in that department.
     * Initialized once in the static block below.
     */
    private static final Map<String, String[]> POSITIONS_BY_DEPARTMENT = new LinkedHashMap<>();

    static {
        // Executive leadership titles
        POSITIONS_BY_DEPARTMENT.put("Executive", new String[] {
            "Chief Executive Officer",
            "Chief Operating Officer",
            "Chief Finance Officer",
            "Chief Marketing Officer"
        });
        POSITIONS_BY_DEPARTMENT.put("IT", new String[] {
            "IT Operations and Systems"
        });
        POSITIONS_BY_DEPARTMENT.put("Human Resources", new String[] {
            "HR Manager",
            "HR Team Leader",
            "HR Rank and File"
        });
        POSITIONS_BY_DEPARTMENT.put("Accounting", new String[] {
            "Accounting Head",
            "Payroll Manager",
            "Payroll Team Leader",
            "Payroll Rank and File"
        });
        POSITIONS_BY_DEPARTMENT.put("Sales & Accounts", new String[] {
            "Account Manager",
            "Account Team Leader",
            "Account Rank and File"
        });
        POSITIONS_BY_DEPARTMENT.put("Marketing", new String[] {
            "Sales & Marketing"
        });
        POSITIONS_BY_DEPARTMENT.put("Operations", new String[] {
            "Supply Chain and Logistics"
        });
        POSITIONS_BY_DEPARTMENT.put("Customer Service", new String[] {
            "Customer Service and Relations"
        });
    }

    /**
     * @return immutable-style list of all department names for combo boxes
     */
    public static List<String> allDepartments() {
        return Arrays.asList(DEPARTMENTS);
    }

    /**
     * Returns job titles valid for the given department.
     *
     * @param department selected department name
     * @return cloned array of positions, or {@code {"N/A"}} when department is unknown
     */
    public static String[] positionsForDepartment(String department) {
        if (department == null) {
            return new String[0];
        }
        String[] positions = POSITIONS_BY_DEPARTMENT.get(department.trim());
        return positions == null ? new String[] { "N/A" } : positions.clone();
    }

    /**
     * Infers a department from a job title when older CSV rows have no department column filled.
     * Uses keyword matching first, then exact title lookup in {@link #POSITIONS_BY_DEPARTMENT}.
     *
     * @param position job title from form or CSV
     * @return inferred department name, or {@code "General"} when no rule matches
     */
    public static String inferDepartmentFromPosition(String position) {
        if (position == null || position.trim().isEmpty()) {
            return "General";
        }
        String p = position.trim().toLowerCase();
        if (p.contains("chief executive") || p.contains("chief operating")
                || p.contains("chief finance") || p.contains("chief marketing")) {
            return "Executive";
        }
        if (p.contains("it ") || p.contains("systems")) {
            return "IT";
        }
        if (p.contains("hr ")) {
            return "Human Resources";
        }
        if (p.contains("accounting") || p.contains("payroll")) {
            return "Accounting";
        }
        if (p.contains("account manager") || p.contains("account team")
                || p.contains("account rank")) {
            return "Sales & Accounts";
        }
        if (p.contains("sales") || p.contains("marketing")) {
            return "Marketing";
        }
        if (p.contains("supply chain") || p.contains("logistics")) {
            return "Operations";
        }
        if (p.contains("customer service")) {
            return "Customer Service";
        }
        // Exact title match against the position map
        for (Map.Entry<String, String[]> entry : POSITIONS_BY_DEPARTMENT.entrySet()) {
            for (String title : entry.getValue()) {
                if (title.equalsIgnoreCase(position.trim())) {
                    return entry.getKey();
                }
            }
        }
        return "General";
    }

    /**
     * Resolves the immediate supervisor display name ({@code "Last, First"}) from department
     * and position using org-chart rules and CSV lookup.
     *
     * @param department employee department
     * @param position   employee job title
     * @return supervisor name for the form, or {@code "N/A"} at top of hierarchy
     */
    public static String resolveSupervisor(String department, String position) {
        List<String[]> employees = FileHandlerModule.getAllEmployees();
        String dept = department == null ? "" : department.trim();
        String pos = position == null ? "" : position.trim();
        String posLower = pos.toLowerCase();

        if ("Executive".equals(dept)) {
            if (posLower.contains("chief executive")) {
                return "N/A";
            }
            return supervisorNameForPosition(employees, "Chief Executive Officer", "N/A");
        }
        if ("IT".equals(dept)) {
            return supervisorNameForPosition(employees, "Chief Operating Officer", "Lim, Antonio");
        }
        if ("Human Resources".equals(dept)) {
            if (posLower.contains("hr manager")) {
                return supervisorNameForPosition(employees, "Chief Operating Officer", "Lim, Antonio");
            }
            if (posLower.contains("team leader")) {
                return supervisorNameForPosition(employees, "HR Manager", "Villanueva, Andrea Mae");
            }
            if (posLower.contains("rank and file")) {
                return supervisorNameForPosition(employees, "HR Team Leader", "San, Jose Brad");
            }
        }
        if ("Accounting".equals(dept)) {
            if (posLower.contains("accounting head")) {
                return supervisorNameForPosition(employees, "Chief Finance Officer", "Aquino, Bianca Sofia");
            }
            if (posLower.contains("payroll manager")) {
                return supervisorNameForPosition(employees, "Accounting Head", "Alvaro, Roderick");
            }
            if (posLower.contains("payroll team leader") || posLower.contains("payroll rank")) {
                return supervisorNameForPosition(employees, "Payroll Manager", "Salcedo, Anthony");
            }
        }
        if ("Sales & Accounts".equals(dept)) {
            if (posLower.contains("account manager")) {
                return supervisorNameForPosition(employees, "Chief Operating Officer", "Lim, Antonio");
            }
            if (posLower.contains("team leader") || posLower.contains("rank and file")) {
                return supervisorNameForPosition(employees, "Account Manager", "Romualdez, Fredrick");
            }
        }
        if ("Marketing".equals(dept)) {
            return supervisorNameForPosition(employees, "Chief Marketing Officer", "Reyes, Isabella");
        }
        if ("Operations".equals(dept)) {
            return supervisorNameForPosition(employees, "Chief Marketing Officer", "Reyes, Isabella");
        }
        if ("Customer Service".equals(dept)) {
            return supervisorNameForPosition(employees, "Chief Marketing Officer", "Reyes, Isabella");
        }
        return findDepartmentHead(employees, dept);
    }

    /**
     * Finds any manager/head/chief in the same department when no explicit rule exists.
     */
    private static String findDepartmentHead(List<String[]> employees, String department) {
        for (String[] emp : employees) {
            if (emp == null || emp.length <= EmployeeModule.DEPARTMENT) {
                continue;
            }
            String dept = safe(emp, EmployeeModule.DEPARTMENT);
            String pos = safe(emp, EmployeeModule.POSITION).toLowerCase();
            if (department.equals(dept) && (pos.contains("manager") || pos.contains("head") || pos.contains("chief"))) {
                return formatSupervisorName(emp);
            }
        }
        return "N/A";
    }

    /**
     * Looks up an employee by exact position title; returns {@code fallback} when not found.
     */
    private static String supervisorNameForPosition(List<String[]> employees, String targetPosition,
            String fallback) {
        for (String[] emp : employees) {
            if (emp != null && emp.length > EmployeeModule.POSITION
                    && targetPosition.equalsIgnoreCase(safe(emp, EmployeeModule.POSITION))) {
                return formatSupervisorName(emp);
            }
        }
        return fallback;
    }

    /**
     * Formats CSV first/last name as supervisor label {@code "Last, First"}.
     *
     * @param emp employee row from CSV
     * @return formatted name or {@code "N/A"}
     */
    public static String formatSupervisorName(String[] emp) {
        if (emp == null || emp.length <= EmployeeModule.FIRST_NAME) {
            return "N/A";
        }
        return safe(emp, EmployeeModule.LAST_NAME) + ", " + safe(emp, EmployeeModule.FIRST_NAME);
    }

    /** Safe CSV cell read with trim; empty string when index out of range or null. */
    private static String safe(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) {
            return "";
        }
        return row[index].trim();
    }
}
