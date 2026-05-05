public class ValidationModule {
    private ValidationModule() {}

    static OperationResult<Integer> validateEmployeeNumber(String rawEmployeeNumber) {
        if (rawEmployeeNumber == null || rawEmployeeNumber.trim().isEmpty()) {
            return OperationResult.fail("Employee Number is required.");
        }
        try {
            int employeeNumber = Integer.parseInt(rawEmployeeNumber.trim());
            if (employeeNumber <= 0) {
                return OperationResult.fail("Employee Number must be greater than zero.");
            }
            return OperationResult.ok(employeeNumber);
        } catch (NumberFormatException ex) {
            return OperationResult.fail("Employee Number must be numeric.");
        }
    }

    static OperationResult<String> validateEmployeeName(String employeeName) {
        if (employeeName == null || employeeName.trim().isEmpty()) {
            return OperationResult.fail("Employee Name is required.");
        }
        String normalized = employeeName.trim();
        if (!normalized.matches("[A-Za-z .'-]{2,}")) {
            return OperationResult.fail("Employee Name contains invalid characters.");
        }
        return OperationResult.ok(normalized);
    }

    static OperationResult<String> validatePayCoverage(String payCoverage) {
        if (payCoverage == null || payCoverage.trim().isEmpty()) {
            return OperationResult.fail("Pay Coverage is required.");
        }
        if (!payCoverage.matches("\\d{4}-\\d{2}-\\d{2}\\s+to\\s+\\d{4}-\\d{2}-\\d{2}")) {
            return OperationResult.fail("Pay Coverage must follow: YYYY-MM-DD to YYYY-MM-DD");
        }
        return OperationResult.ok(payCoverage.trim());
    }

    static OperationResult<String> validateTime(String rawTime, String label) {
        if (rawTime == null || rawTime.trim().isEmpty()) {
            return OperationResult.fail(label + " is required.");
        }
        String normalized = rawTime.trim();
        if (!normalized.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            return OperationResult.fail(label + " must be in HH:MM (24-hour) format.");
        }
        return OperationResult.ok(normalized);
    }

    static OperationResult<Double> validatePositiveAmount(double amount, String label) {
        if (amount < 0) {
            return OperationResult.fail(label + " must not be negative.");
        }
        return OperationResult.ok(amount);
    }
}
