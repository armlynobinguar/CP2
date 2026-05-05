public class UserModule {
    private UserModule() {}

    static void runUserFlow(String username, String password, String employeeName) {
        System.out.println("User account created: " + username + " (Employee)");
        boolean ok1 = login(username, password, "mreyes", "wrongpass");
        System.out.println(ok1 ? "Login success: " + employeeName : "Login failed");
        boolean ok2 = login(username, password, "mreyes", "secure123");
        System.out.println(ok2 ? "Login success: " + employeeName : "Login failed");
        System.out.println(ok2 ? "Logout success" : "No active session");
    }

    static boolean login(String realUser, String realPass, String inputUser, String inputPass) {
        return realUser.equals(inputUser) && realPass.equals(inputPass);
    }
}
