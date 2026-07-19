package motorph_employeeapp;

/**
 * Dependency-free test runner invoked by {@code ant test} and CI.
 */
public final class AllTests {

    private AllTests() {
    }

    public static void main(String[] args) throws Exception {
        int passed = 0;
        passed += run("SalaryComputationModuleTest", SalaryComputationModuleTest::runAll);
        passed += run("EmployeeRecordsModuleTest", EmployeeRecordsModuleTest::runAll);
        passed += run("FileHandlerModuleTest", FileHandlerModuleTest::runAll);
        passed += run("EmployeeRevisionModuleTest", EmployeeRevisionModuleTest::runAll);
        System.out.println("AllTests passed: " + passed + " suite(s)");
    }

    @FunctionalInterface
    private interface TestRunnable {
        void run() throws Exception;
    }

    private static int run(String name, TestRunnable runnable) throws Exception {
        runnable.run();
        System.out.println("[PASS] " + name);
        return 1;
    }
}
