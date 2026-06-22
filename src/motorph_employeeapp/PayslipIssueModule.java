package motorph_employeeapp;

/**
 * PayslipIssueModule
 * ------------------
 * Model for employee-submitted payslip concerns stored in {@code resources/payslip_issues.txt}.
 *
 * <p>Line format (pipe-delimited, {@code |||} separator):</p>
 * <pre>
 *   timestamp|||employeeId|||employeeName|||payPeriod|||issueType|||description|||status|||hrNote|||resolvedAt
 * </pre>
 * Older six-field lines are parsed as {@link #STATUS_OPEN} with empty HR notes.
 */
public class PayslipIssueModule {

    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED = "Resolved";

    /** One employee payslip issue report and HR resolution state. */
    public static class PayslipIssue {

        public String timestamp;
        public String employeeId;
        public String employeeName;
        public String payPeriod;
        public String issueType;
        public String description;
        public String status;
        public String hrNote;
        public String resolvedAt;

        public PayslipIssue(String timestamp, String employeeId, String employeeName, String payPeriod,
                String issueType, String description, String status, String hrNote, String resolvedAt) {
            this.timestamp = nz(timestamp);
            this.employeeId = nz(employeeId);
            this.employeeName = nz(employeeName);
            this.payPeriod = nz(payPeriod);
            this.issueType = nz(issueType);
            this.description = nz(description);
            this.status = isBlank(status) ? STATUS_OPEN : status.trim();
            this.hrNote = nz(hrNote);
            this.resolvedAt = nz(resolvedAt);
        }

        /** New employee report defaults to {@link #STATUS_OPEN}. */
        public PayslipIssue(String timestamp, String employeeId, String employeeName, String payPeriod,
                String issueType, String description) {
            this(timestamp, employeeId, employeeName, payPeriod, issueType, description,
                    STATUS_OPEN, "", "");
        }

        public boolean isOpen() {
            return STATUS_OPEN.equalsIgnoreCase(status);
        }

        public boolean isInProgress() {
            return STATUS_IN_PROGRESS.equalsIgnoreCase(status);
        }

        public boolean isResolved() {
            return STATUS_RESOLVED.equalsIgnoreCase(status);
        }

        public boolean needsHrAction() {
            return !isResolved();
        }

        public String displaySummary() {
            return "#" + employeeId + " · " + payPeriod + " · " + issueType;
        }

        public String serializeLine() {
            return sanitize(timestamp) + "|||" + sanitize(employeeId) + "|||" + sanitize(employeeName)
                    + "|||" + sanitize(payPeriod) + "|||" + sanitize(issueType) + "|||" + sanitize(description)
                    + "|||" + sanitize(status) + "|||" + sanitize(hrNote) + "|||" + sanitize(resolvedAt);
        }

        public static PayslipIssue parseLine(String line) {
            if (line == null || line.trim().isEmpty()) {
                return null;
            }
            String[] parts = line.split("\\Q|||\\E", 9);
            if (parts.length < 6) {
                return null;
            }
            String status = parts.length > 6 ? parts[6] : STATUS_OPEN;
            String hrNote = parts.length > 7 ? parts[7] : "";
            String resolvedAt = parts.length > 8 ? parts[8] : "";
            return new PayslipIssue(
                    unescape(parts[0]),
                    unescape(parts[1]),
                    unescape(parts[2]),
                    unescape(parts[3]),
                    unescape(parts[4]),
                    unescape(parts[5]),
                    unescape(status),
                    unescape(hrNote),
                    unescape(resolvedAt));
        }

        private static String sanitize(String value) {
            if (value == null) {
                return "";
            }
            return value.replace("|||", "[PIPE3]").replace('\n', ' ').replace('\r', ' ').trim();
        }

        private static String unescape(String value) {
            if (value == null) {
                return "";
            }
            return value.replace("[PIPE3]", "|||").trim();
        }

        private static String nz(String value) {
            return value == null ? "" : value.trim();
        }

        private static boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }
    }
}
