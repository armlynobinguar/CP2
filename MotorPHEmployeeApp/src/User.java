/**
 * Manages login credentials and system access roles
 * for employees using the MotorPH application.
 */
public class User {

    // ---------- Attributes ----------
    private int      userId;
    private String   username;
    private String   password;
    private String   role;
    private Employee employee;
    private boolean  isLoggedIn;

    // ---------- Constructor ----------
    public User(int userId, String username, String password,
                String role, Employee employee) {
        this.userId    = userId;
        this.username  = username;
        this.password  = password;
        this.role      = role;
        this.employee  = employee;
        this.isLoggedIn= false;
        System.out.println("[User] Account created: " + username + " | Role: " + role);
    }

    // ---------- Methods ----------
    public boolean login(String inputUsername, String inputPassword) {
        if (username.equals(inputUsername) && password.equals(inputPassword)) {
            this.isLoggedIn = true;
            System.out.println("[User] login() -> SUCCESS. Welcome, " + employee.getFullName() + "!");
            return true;
        } else {
            System.out.println("[User] login() -> FAILED. Invalid username or password.");
            return false;
        }
    }

    public void logout() {
        if (isLoggedIn) {
            this.isLoggedIn = false;
            System.out.println("[User] logout() -> " + employee.getFullName() + " has logged out.");
        } else {
            System.out.println("[User] logout() -> No active session to end.");
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        if (password.equals(currentPassword)) {
            this.password = newPassword;
            System.out.println("[User] changePassword() -> Password updated successfully for " + username);
        } else {
            System.out.println("[User] changePassword() -> Failed. Current password is incorrect.");
        }
    }

    public String getRole() {
        System.out.println("[User] getRole() -> " + role);
        return role;
    }

    // ---------- Getters ----------
    public int      getUserId()    { return userId; }
    public String   getUsername()  { return username; }
    public boolean  isLoggedIn()   { return isLoggedIn; }
    public Employee getEmployee()  { return employee; }
}
