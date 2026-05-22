# MotorPH Employee App

**MO-IT103 Computer Programming 2**
**Group 3 | Section H1101**

javac -d bin src/motorph_employeeapp/*.java
java -cp bin motorph_employeeapp.MotorPH_EmployeeApp

> **Demo Credentials**
> Username: `employee` &nbsp;|&nbsp; Password: `12345`

---

## Group Members

| Name |
|---|
| Arrogante, Mitzi Reese |
| Flor, Chantal Louise |
| Obinguar, Armielyn |
| Tavera, Ryan Keneth |

---

## Overview

The MotorPH Employee App is a Java-based payroll and employee management system developed for MO-IT103 Computer Programming 2. It processes employee payroll, government deductions (SSS, PhilHealth, Pag-IBIG, Withholding Tax), and attendance-based salary computations through a Java Swing graphical interface.

The system is built using a **procedural modular architecture** — all logic is separated into dedicated static module classes, with no domain object instantiation. This design serves as the stepping stone toward a full object-oriented implementation in later sprints.

---

## Project Structure

```
CP2/
├── resources/
│   ├── MotorPH_Employee Data - Employee Details.csv
│   └── MotorPH_Employee Data - Attendance Record.csv
└── src/motorph_employeeapp/
    ├── MotorPH_EmployeeApp.java     ← Main entry point and authentication
    ├── MotorPH_GUI.java             ← Java Swing GUI and event handling
    ├── EmployeeModule.java          ← CSV column constants and employee utilities
    ├── FileHandlerModule.java       ← File I/O and CSV parsing engine
    └── SalaryComputationModule.java ← Payroll math and government deduction logic
```

---

## How to Run

1. Open the project in VS Code or NetBeans
2. Ensure the `resources/` folder is in the project root with both CSV files
3. Compile all `.java` files inside `src/motorph_employeeapp/`
4. Run `MotorPH_EmployeeApp.java` as the main entry point
5. Log in using the demo credentials above
6. Select a menu option to proceed

javac -d bin src/motorph_employeeapp/*.java
java -cp bin motorph_employeeapp.MotorPH_EmployeeApp

---

## Component Architecture & Class Documentation

---

### 1. `MotorPH_EmployeeApp.java`
**Role:** Main entry point and authentication controller.

Manages the application's root execution pipeline by enforcing credential verification before bootstrapping the GUI runtime thread. It defines global authentication constants and controls the login flow gate.

#### Authentication Constants
| Constant | Value | Purpose |
|---|---|---|
| `AUTH_VAL_1` | `"employee"` | Valid username option 1 |
| `AUTH_VAL_2` | `"payroll_staff"` | Valid username option 2 |
| `AUTH_VAL_3` | `"12345"` | Valid password option 1 |
| `AUTH_VAL_4` | `"password123"` | Valid password option 2 |
| `loginSuccessful` | `boolean` | Global flag tracking login state |

#### Methods
| Method | Return Type | Description |
|---|---|---|
| `main(String[] args)` | `void` | Launches the login dialog, checks `loginSuccessful` flag, then either initializes the GUI or exits the application. |

#### Flow
```
main() → showCustomLoginDialog() → [login check] → initialize() OR System.exit(0)
```

---

### 2. `MotorPH_GUI.java`
**Role:** Java Swing GUI layer — constructs all visual windows and handles all user interactions.

Constructs the visual user workspace using Java Swing components, handling dashboard path routing, event-triggered `ActionListener` implementations, `KeyListener` implementations, `MouseListener` implementations, and real-time input error prompts via `JOptionPane`.

#### Static Fields
| Field | Type | Description |
|---|---|---|
| `frame` | `JFrame` | Main application window shared across all screens |
| `monthCombo` | `JComboBox<String>` | Dropdown selector for pay coverage month (June–December) |
| `txtEmployeeNo` | `JTextField` | Input field for employee number |
| `txtEmployeeName` | `JTextField` | Read-only display field for employee name |
| `txtYear` | `JTextField` | Input field for pay coverage year |
| `txtResultArea` | `JTextArea` | Output display area for payroll results |
| `txtLookupInput` | `JTextField` | Input field for employee ID lookup |
| `txtLookupDisplay` | `JTextArea` | Output display area for employee lookup results |
| `loginDialog` | `JDialog` | Modal login window |
| `usernameField` | `JTextField` | Login username input |
| `passwordField` | `JPasswordField` | Login password input |
| `btnLogin` | `JButton` | Login action trigger button |

#### Font and Color Constants
| Constant | Description |
|---|---|
| `APP_FONT_BOLD` | Segoe UI Bold 14px — used for labels and buttons |
| `APP_FONT_PLAIN` | Segoe UI Plain 13px — used for input fields |
| `RECEIPT_FONT` | Consolas Plain 13px — used for payroll output |
| `ACCENT_BLUE` | `#1877F2` — primary blue for action buttons |
| `HOVER_BLUE` | `#0C67DE` — hover state for accent buttons |
| `TEXT_DARK_NAVY` | `#1C3970` — primary text color |
| `PALETTE_LIGHT_BLUE` | `#EBF3FF` — input field background |
| `BORDER_BLUE` | `#B4CDF0` — border color for panels and fields |

#### Methods
| Method | Description |
|---|---|
| `showCustomLoginDialog()` | Constructs and displays the modal login window with username, password fields, and login button. Attaches `ActionListener` for button click and `KeyListener` for Enter key support on both input fields. |
| `initialize()` | Creates the main `JFrame` and calls `showMainMenu()` to render the navigation dashboard. |
| `showMainMenu()` | Renders the main navigation panel with three buttons: Pay Coverage, Employee Information, and Logout. |
| `setupPayrollUI()` | Constructs the payroll processing form with Employee Number, Employee Name (read-only), Pay Coverage Month dropdown (June–December), Pay Coverage Year, Process Payroll button, and Back to Menu button. |
| `showEmployeeLookupUI()` | Renders the employee information lookup screen with an ID input field, Search Record button, and scrollable result display area. |
| `runEmployeeLookupAction()` | Reads the ID input, queries `FileHandlerModule`, and displays Employee ID, Full Name, and Birthday in the result area. Shows "Employee ID not found." if no match. |
| `runPayrollCalculation()` | Validates all input fields, highlights invalid fields in red, shows consolidated error messages, verifies employee ID against the database, populates the employee name on success, and calls `SalaryComputationModule.calculatePayroll()`. |
| `updateDisplay()` | Calls `revalidate()`, `repaint()`, and `setVisible(true)` to force accurate UI refresh after screen transitions. |
| `createStyledLabel(String)` | Helper that creates a `JLabel` with the app's standard bold font and dark navy color. |
| `createStyledTextField(boolean)` | Helper that creates a styled `JTextField` — white background if editable, light blue if read-only. |
| `createStyledAlertText(String)` | Helper that wraps an error message string in a styled `JLabel` for use inside `JOptionPane` dialogs. |
| `styleAccentButton(JButton)` | Applies blue accent styling to login button with hover effect via `MouseListener`. Includes macOS rendering fixes (`setOpaque`, `setContentAreaFilled`, `setBorderPainted`). |
| `guiStyleAccentButton(JButton)` | Applies blue accent styling to main GUI buttons. Includes macOS rendering fixes. |
| `styleStandardButton(JButton)` | Applies white standard styling with blue border to secondary buttons. |
| `styleInputField(JTextField)` | Applies login screen input field styling with light blue background and compound border. |

#### Validation Rules in `runPayrollCalculation()`
| Field | Validation |
|---|---|
| Employee Number | Required, must be numeric, must exist in database |
| Pay Coverage Month | Must not be blank (index 0) |
| Pay Coverage Year | Required, must be numeric, must equal `2024` |

#### Event Handling Summary
| Event | Trigger | Action |
|---|---|---|
| `ActionListener` | Login button click | Validates credentials, sets `loginSuccessful`, disposes dialog |
| `KeyListener` (VK_ENTER) | Enter key on username or password | Simulates login button click via `btnLogin.doClick()` |
| `ActionListener` | Process Payroll button | Validates fields, computes payroll |
| `ActionListener` | Search Record button | Looks up employee by ID |
| `ActionListener` | Back to Menu button | Returns to main navigation |
| `ActionListener` | Logout button | Calls `System.exit(0)` |
| `KeyListener` (keyReleased) | Employee Number field cleared | Clears the Employee Name field |
| `MouseListener` | Hover on accent buttons | Changes button background to `HOVER_BLUE` |

---

### 3. `EmployeeModule.java`
**Role:** CSV column index constants and employee data utility methods.

Maintains structural CSV column layout indices and offers centralized utility methods to extract formatted employee naming attributes and numerical wage statistics from raw `String[]` data arrays.

#### CSV Column Index Constants
| Constant | Index | CSV Column |
|---|---|---|
| `ID` | 0 | Employee Number |
| `LAST_NAME` | 1 | Last Name |
| `FIRST_NAME` | 2 | First Name |
| `BIRTHDAY` | 3 | Birthday |
| `HOURLY_RATE` | 18 | Hourly Rate |

#### Methods
| Method | Return Type | Description |
|---|---|---|
| `fullName(String[] emp)` | `String` | Concatenates `FIRST_NAME` and `LAST_NAME` from the employee array. Returns `"Unknown"` if the array is null or too short. |
| `getHourlyRate(String[] emp)` | `double` | Parses and returns the hourly rate from index 18. Removes commas before parsing. Returns `0.0` on any parse failure. |

---

### 4. `FileHandlerModule.java`
**Role:** Data-layer file I/O engine for reading and writing CSV records.

Acts as the dedicated data-layer connection engine responsible for executing line-by-line streaming, custom delimiter parsing, and exception-safe file I/O operations against the employee and attendance CSV data files.

#### File Path Constants
| Constant | Path | Description |
|---|---|---|
| `EMPLOYEE_FILE` | `resources/MotorPH_Employee Data - Employee Details.csv` | Employee master data file |
| `ATTENDANCE_FILE` | `resources/MotorPH_Employee Data - Attendance Record.csv` | Daily attendance records file |

#### Methods
| Method | Return Type | Description |
|---|---|---|
| `findEmployeeData(String id)` | `String` | Reads the employee CSV line by line and returns the first line where column 0 matches the given ID. Returns `null` if not found. |
| `findAttendanceData(String id)` | `List<String>` | Reads all attendance lines matching the given employee ID and returns them as a list. Skips the header row. |
| `getAllEmployees()` | `List<String[]>` | Reads and returns all employee records as parsed arrays, skipping the header row. Useful for tabular display. |
| `appendEmployeeRecord(String rawCsvLine)` | `boolean` | Appends a new employee record line to the employee CSV file. Returns `true` on success, `false` on failure. |
| `smartSplit(String line)` | `String[]` | Custom CSV parser that correctly handles quoted fields containing commas. Iterates character by character, toggling an `inQuotes` flag on `"` characters. Returns a trimmed `String[]`. |

#### `smartSplit()` Logic
Standard `String.split(",")` fails on CSV fields that contain commas inside quotes (e.g. `"Garcia, Manuel III"`). `smartSplit()` solves this by:
1. Iterating character by character
2. Toggling `inQuotes` on every `"` character
3. Only splitting on `,` when `inQuotes` is `false`
4. Trimming all results before returning

---

### 5. `SalaryComputationModule.java`
**Role:** Payroll computation engine and government deduction calculator.

Centralizes the application's mathematical processing rules to evaluate shift logs, compute semi-monthly gross pay, and calculate statutory government withholdings (SSS, PhilHealth, Pag-IBIG) and multi-bracket BIR income tax balances.

#### Methods

| Method | Return Type | Description |
|---|---|---|
| `calculatePayroll(String[], String, String, JTextArea)` | `void` | Main payroll engine. Retrieves attendance records, computes hours per cutoff period, calculates gross pay, applies all deductions, and appends formatted results to the GUI output area. |
| `calculateShift(String logIn, String logOut)` | `double` | Computes billable hours for a single shift. Applies 10-minute grace period, caps at 17:00, and deducts 60-minute lunch break for shifts longer than 5 hours. |
| `computeSSS(double salary)` | `double` | Computes SSS contribution using a threshold bracket loop. Returns `135.00` for salaries below `3,250` and caps at `1,125.00` for salaries above `24,750`. |
| `computePhilHealth(double salary)` | `double` | Computes employee PhilHealth share at 1.5% of salary (half of 3% total premium). Returns fixed `150.00` for salaries ≤ `10,000` and `900.00` for salaries ≥ `60,000`. |
| `computePagIBIG(double salary)` | `double` | Computes Pag-IBIG contribution at 1% for salaries ≤ `1,500` and 2% above. Caps the total contribution at `100.00`. |
| `calculateWithholdingTax(double taxableIncome)` | `double` | Computes BIR withholding tax using a 6-bracket progressive tax table. Taxable income is gross minus SSS, PhilHealth, and Pag-IBIG. |
| `monthName(String monthStr)` | `String` | Converts a numeric month string (e.g. `"6"`) to its full name (e.g. `"June"`) using a switch expression. Returns `"Invalid Month"` on parse failure. |
| `findWorkingPeriods(String id)` | `List<String>` | Scans attendance records for the given employee ID and returns a deduplicated list of `MM/YYYY` strings representing months with recorded attendance. |

#### Payroll Computation Flow
```
calculatePayroll()
    ↓
findAttendanceData(id)          ← gets all attendance rows
    ↓
for each row:
    calculateShift(timeIn, timeOut)   ← billable hours per day
    → hoursFirstCutoff  (days 1–15)
    → hoursSecondCutoff (days 16–31)
    ↓
grossFirstCutoff  = hoursFirstCutoff  × hourlyRate
grossSecondCutoff = hoursSecondCutoff × hourlyRate
totalMonthlyGross = grossFirstCutoff + grossSecondCutoff
    ↓
computeSSS(totalMonthlyGross)
computePhilHealth(totalMonthlyGross)
computePagIBIG(totalMonthlyGross)
taxableIncome = totalMonthlyGross - (sss + ph + pi)
calculateWithholdingTax(taxableIncome)
    ↓
netSalary1 = grossFirstCutoff                  (no deductions on first cutoff)
netSalary2 = grossSecondCutoff - totalDeductions (deductions on second cutoff)
    ↓
output.append(formatted payslip)
```

#### SSS Bracket Summary
| Monthly Salary Range | SSS Contribution |
|---|---|
| Below PHP 3,250 | PHP 135.00 |
| PHP 3,250 – PHP 24,749 | PHP 157.50 – PHP 1,102.50 (in PHP 22.50 steps per PHP 500) |
| PHP 24,750 and above | PHP 1,125.00 (maximum) |

#### PhilHealth Bracket Summary
| Monthly Salary | Employee Share |
|---|---|
| ≤ PHP 10,000 | PHP 150.00 (fixed) |
| PHP 10,001 – PHP 59,999 | 1.5% of monthly salary |
| ≥ PHP 60,000 | PHP 900.00 (maximum) |

#### BIR Withholding Tax Brackets
| Taxable Income | Tax Computation |
|---|---|
| ≤ PHP 20,832 | PHP 0 |
| PHP 20,833 – PHP 33,332 | 20% of excess over PHP 20,833 |
| PHP 33,333 – PHP 66,666 | PHP 2,500 + 25% of excess over PHP 33,333 |
| PHP 66,667 – PHP 166,666 | PHP 10,833 + 30% of excess over PHP 66,667 |
| PHP 166,667 – PHP 666,666 | PHP 40,833.33 + 32% of excess over PHP 166,667 |
| Above PHP 666,667 | PHP 200,833.33 + 35% of excess over PHP 666,667 |

---

## Features

- Login authentication with Enter key and button click support
- Employee lookup by ID with Search Record button trigger
- Payroll computation with semi-monthly cutoff breakdown (1–15 and 16–31)
- Government deduction computation (SSS, PhilHealth, Pag-IBIG, Withholding Tax)
- Dropdown month selector (June to December — matching available CSV data)
- Year validation — only 2024 data is currently supported
- Comma-formatted salary output (e.g. `PHP 38,883.62`)
- Hours worked rounded to 2 decimal places
- Red field highlighting for invalid or missing inputs
- Consolidated error popup listing all validation issues at once
- Employee name auto-populated on successful payroll computation

---

## Homework Progress

| Homework | Description | Status |
|---|---|---|
| Homework 1 | Class Diagram Design | ✅ Done |
| Homework 2 | Java Implementation (OOP) | ✅ Done |
| Homework 3 | GUI Interface Design (Procedural) | ✅ Done |

---

## Links

| Resource | Link |
|---|---|
| Project Plan | [Project Plan](https://docs.google.com/spreadsheets/d/1ZfEM7OL4OEOAmj9opmkJDVqFON5w2NJV129yVMBLhbI/edit?usp=sharing) |
| Figma Design | [Figma](https://www.figma.com/make/yrUok9BbHVPctk43Czg4Bo/MotorPH-Payroll-System-CP2?t=RpZwUqjSXZqqEahE-0) |
| Class Diagram | [Class Diagram](https://docs.google.com/spreadsheets/d/18u4H9f2NgLQ9XYUVs5bcBgl3MJHv__k_ww2FdErt1C0/edit?usp=sharing) |
| MPHCR01 - Feature 1 Change Request | [MPHCR01 - Feature 1 Change Request](https://docs.google.com/spreadsheets/d/1e4E4m4BBXRVHvcifofmNJzgd9UsucakaDIeEcfempWU/edit?gid=475634283#gid=475634283) |
| Computer Programming 1 | [Computer Programming 1](https://github.com/MitziReese04/MO-IT101-Group5.git) |

---

## Course Information

- **Subject:** MO-IT103 Computer Programming 2
- **Section:** H1101
- **Group:** 3 - TeamPURA
- **School:** Mapua Malayan Colleges Laguna (MMDC/MCL)
