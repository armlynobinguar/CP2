/**
 * Represents a company department that groups employees
 * under a common organizational unit in MotorPH.
 */
public class Department {

    // ---------- Attributes ----------
    private int    departmentId;
    private String departmentName;
    private String departmentHead;

    // ---------- Constructor ----------
    public Department(int departmentId, String departmentName, String departmentHead) {
        this.departmentId   = departmentId;
        this.departmentName = departmentName;
        this.departmentHead = departmentHead;
    }

    // ---------- Methods ----------
    public String getDepartmentName() {
        return departmentName;
    }

    public String getDepartmentHead() {
        return departmentHead;
    }

    public void setDepartmentHead(String newHead) {
        this.departmentHead = newHead;
    }

    // ---------- Getter ----------
    public int getDepartmentId() { return departmentId; }
}
