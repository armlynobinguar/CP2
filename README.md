# MotorPH Employee App

A Java console application that models the core HR and payroll operations of MotorPH. Built as Homework 2 for MO-IT103 Computer Programming 2 (Group 3 - H1101).

---

## Classes

| Class | Description |
|---|---|
| `Employee` | Stores personal and employment information for each employee |
| `Position` | Represents a job role with salary grade and pay range |
| `Department` | Groups employees under an organizational unit |
| `Attendance` | Records daily time-in/time-out and computes hours worked |
| `LeaveRequest` | Manages leave applications and approval status |
| `Payroll` | Computes gross pay, deductions, and net pay for a pay period |
| `Benefit` | Holds employee allowances (rice subsidy, phone, clothing) |
| `Deduction` | Computes mandatory deductions: SSS, PhilHealth, Pag-IBIG, withholding tax |
| `User` | Manages login credentials and role-based access |
| `HRManager` | Extends `Employee` with HR actions: add/update employees, approve leaves, process payroll |

---

## How to Run

1. Open the project in any Java IDE (NetBeans, IntelliJ, VS Code with Java extension)
2. Compile all `.java` files in the `src/` folder
3. Run `Main.java`

Or via terminal:
```bash
cd src
javac *.java
java Main
```

---

## Sample Output

```
============================================
     MOTORPH EMPLOYEE APP - DEMO RUN
============================================

--- Creating Employee ---
 Employee Created: Maria Reyes

========================================
         EMPLOYEE INFORMATION
========================================
Employee ID      : 10001
Name             : Maria Reyes
Position         : Software Engineer
Department       : IT Department
Basic Salary     : PHP 35000.0
...

--- Processing Payroll ---
  NET PAY        : PHP 15351.62
============================================
```

---

## Deduction Logic

| Deduction | Basis |
|---|---|
| SSS | Bracket table based on basic salary |
| PhilHealth | 3% of salary, employee pays half (1.5%) |
| Pag-IBIG | 2% of salary, capped at PHP 100 |
| Withholding Tax | BIR monthly tax table applied to taxable income |

---

## Authors

- Ryan Tavera
- Group 3 - H1101 | MO-IT103 Computer Programming 2
