package motorph_employeeapp;

/**
 * Minimal assertion helpers for dependency-free unit tests run via {@link AllTests}.
 */
final class TestSupport {

    private TestSupport() {
    }

    static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) <= delta) {
            return;
        }
        throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    static void assertArrayEquals(String[] expected, String[] actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || actual == null || expected.length != actual.length) {
            throw new AssertionError(message);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!java.util.Objects.equals(expected[i], actual[i])) {
                throw new AssertionError(message + " at index " + i
                        + " (expected=" + expected[i] + ", actual=" + actual[i] + ")");
            }
        }
    }
}
