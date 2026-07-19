package motorph_employeeapp;

final class SalaryComputationModuleTest {

    private SalaryComputationModuleTest() {
    }

    static void runAll() {
        testCalculateShiftOnTime();
        testCalculateShiftLateBeyondGrace();
        testCalculateShiftInvalidInput();
        testComputeSSSBrackets();
        testComputePhilHealth();
        testComputePagIBIG();
        testComputeWithholdingTax();
        testComputeGrossPay();
    }

    private static void testCalculateShiftOnTime() {
        double hours = SalaryComputationModule.calculateShift("8:04", "17:12");
        TestSupport.assertEquals(8.0, hours, 0.01, "On-time shift should be 8 hours");
    }

    private static void testCalculateShiftLateBeyondGrace() {
        double hours = SalaryComputationModule.calculateShift("8:15", "17:00");
        TestSupport.assertEquals(7.75, hours, 0.01, "Late start should reduce hours");
    }

    private static void testCalculateShiftInvalidInput() {
        TestSupport.assertEquals(0.0, SalaryComputationModule.calculateShift("bad", "17:00"), 0.001,
                "Invalid login time should return 0");
    }

    private static void testComputeSSSBrackets() {
        TestSupport.assertEquals(135.0, SalaryComputationModule.computeSSS(3000), 0.01, "Low SSS bracket");
        TestSupport.assertEquals(1125.0, SalaryComputationModule.computeSSS(30000), 0.01, "High SSS cap");
    }

    private static void testComputePhilHealth() {
        TestSupport.assertEquals(150.0, SalaryComputationModule.computePhilHealth(8000), 0.01,
                "PhilHealth floor premium split");
        TestSupport.assertEquals(900.0, SalaryComputationModule.computePhilHealth(60000), 0.01,
                "PhilHealth ceiling premium split");
    }

    private static void testComputePagIBIG() {
        TestSupport.assertEquals(30.0, SalaryComputationModule.computePagIBIG(1000), 0.01,
                "Pag-IBIG low salary rate");
        TestSupport.assertEquals(100.0, SalaryComputationModule.computePagIBIG(50000), 0.01,
                "Pag-IBIG contribution cap");
    }

    private static void testComputeWithholdingTax() {
        TestSupport.assertEquals(0.0, SalaryComputationModule.calculateWithholdingTax(20000), 0.01,
                "Tax-free bracket");
        TestSupport.assertEquals(1875.0, SalaryComputationModule.calculateWithholdingTax(33333), 0.01,
                "Second tax bracket boundary");
    }

    private static void testComputeGrossPay() {
        double[] gross = SalaryComputationModule.computeGrossPay(
                new double[] { 100.0, 200.0 }, new double[] { 8.0, 4.0 });
        TestSupport.assertEquals(800.0, gross[0], 0.01, "Gross pay index 0");
        TestSupport.assertEquals(800.0, gross[1], 0.01, "Gross pay index 1");
    }
}
