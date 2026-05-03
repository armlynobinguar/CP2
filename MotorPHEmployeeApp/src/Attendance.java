/**
 * Records and manages an employee's daily attendance,
 * including time-in, time-out, and hours worked computation.
 */
public class Attendance {

    // ---------- Attributes ----------
    private int      attendanceId;
    private Employee employee;
    private String   date;
    private String   timeIn;
    private String   timeOut;
    private double   hoursWorked;
    private boolean  isLate;

    // Standard shift start time (8:00 AM = 480 minutes from midnight)
    private static final int STANDARD_START_MINUTES = 8 * 60;

    // ---------- Constructor ----------
    public Attendance(int attendanceId, Employee employee, String date) {
        this.attendanceId = attendanceId;
        this.employee     = employee;
        this.date         = date;
        this.hoursWorked  = 0;
        this.isLate       = false;
        System.out.println("Attendance Record created for " + employee.getFullName() + " on " + date);
    }

    // ---------- Methods ----------
    public void recordTimeIn(String time) {
        this.timeIn = time;
        this.isLate = checkIfLate();
        System.out.println("Attendance Record Time-IN -> " + time + (isLate ? " (LATE)" : " (ON TIME)"));
    }

    public void recordTimeOut(String time) {
        this.timeOut    = time;
        this.hoursWorked = computeHoursWorked();
        System.out.println("Attendance Record Time-OUT -> " + time);
    }

    public double computeHoursWorked() {
        if (timeIn == null || timeOut == null) {
            System.out.println("Attendance Hours Worked -> Incomplete time records.");
            return 0;
        }

        int inMinutes  = parseTimeToMinutes(timeIn);
        int outMinutes = parseTimeToMinutes(timeOut);
        double hours   = (outMinutes - inMinutes) / 60.0;

        // Deduct 1 hour for lunch break if shift is longer than 5 hours
        if (hours > 5) hours -= 1;
        if (hours < 0) hours  = 0;

        System.out.println("Attendance Hours Worked -> " + String.format("%.2f", hours) + " hrs");
        return hours;
    }

    public boolean checkIfLate() {
        if (timeIn == null) return false;
        int inMinutes = parseTimeToMinutes(timeIn);
        boolean late  = inMinutes > STANDARD_START_MINUTES;
        System.out.println("Attendance Is Late? -> " + late);
        return late;
    }

    public String getAttendanceSummary() {
        String summary = "[ " + date + " ] " + employee.getFullName()
                       + " | In: " + timeIn
                       + " | Out: " + timeOut
                       + " | Hours: " + String.format("%.2f", hoursWorked)
                       + " | Late: " + (isLate ? "Yes" : "No");
        System.out.println("Attendance Summary -> " + summary);
        return summary;
    }

    // ---------- Helper ----------
    /**
     * Parses a time string in "HH:MM" format to total minutes from midnight.
     */
    private int parseTimeToMinutes(String time) {
        String[] parts  = time.split(":");
        int hours       = Integer.parseInt(parts[0]);
        int minutes     = Integer.parseInt(parts[1]);
        return (hours * 60) + minutes;
    }

    // ---------- Getters ----------
    public int     getAttendanceId() { return attendanceId; }
    public String  getDate()         { return date; }
    public String  getTimeIn()       { return timeIn; }
    public String  getTimeOut()      { return timeOut; }
    public double  getHoursWorked()  { return hoursWorked; }
    public boolean isLate()          { return isLate; }
    public Employee getEmployee()    { return employee; }
}
