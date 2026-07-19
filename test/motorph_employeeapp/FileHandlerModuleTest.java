package motorph_employeeapp;

final class FileHandlerModuleTest {

    private FileHandlerModuleTest() {
    }

    static void runAll() {
        testSmartSplitSimple();
        testSmartSplitQuotedComma();
        testJoinCsvLineQuotesFields();
    }

    private static void testSmartSplitSimple() {
        String[] cols = FileHandlerModule.smartSplit("10001,Garcia,Manuel");
        TestSupport.assertArrayEquals(new String[] { "10001", "Garcia", "Manuel" }, cols,
                "Simple CSV split");
    }

    private static void testSmartSplitQuotedComma() {
        String[] cols = FileHandlerModule.smartSplit(
                "10001,Garcia,\"Valero Carpark Building, Valero Street\"");
        TestSupport.assertEquals("Valero Carpark Building, Valero Street", cols[2],
                "Quoted comma preserved in field");
    }

    private static void testJoinCsvLineQuotesFields() {
        String line = FileHandlerModule.joinCsvLine(new String[] { "10001", "A,B", "Test" });
        TestSupport.assertEquals("10001,\"A,B\",Test", line, "joinCsvLine quotes comma fields");
    }
}
