public class UserModule {
    private UserModule() {}

    static void runUserFlow(String username, String password, String employeeName) {
        System.out.println("User account created: " + username + " (Employee)");
        OperationResult<Boolean> ok1 = loginValidated(username, password, "mreyes", "wrongpass");
        System.out.println(ok1.isSuccess() && ok1.getData() ? "Login success: " + employeeName : "Login failed");
        OperationResult<Boolean> ok2 = loginValidated(username, password, "mreyes", "secure123");
        System.out.println(ok2.isSuccess() && ok2.getData() ? "Login success: " + employeeName : "Login failed");
        System.out.println(ok2.isSuccess() && ok2.getData() ? "Logout success" : "No active session");
    }

    static OperationResult<Boolean> loginValidated(
            String realUser, String realPass, String inputUser, String inputPass) {
        if (inputUser == null || inputPass == null) {
            return OperationResult.fail("Username and password are required.");
        }
        boolean loggedIn = login(realUser, realPass, inputUser, inputPass);
        return OperationResult.ok(loggedIn);
    }

    static boolean login(String realUser, String realPass, String inputUser, String inputPass) {
        return realUser.equals(inputUser) && realPass.equals(inputPass);
    }
}
