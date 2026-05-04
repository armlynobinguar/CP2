# MotorPH Employee App

A Java console application that models core HR and payroll operations for MotorPH. This version is implemented as **one procedural program** in `MotorPHEmployeeApp/src/Main.java`: data is held in local variables in `main`, and behavior lives in **`static` helper methods** (no separate domain classes such as `Employee` or `Payroll`).

Homework 2 · MO-IT103 Computer Programming 2 · Group 3 - H1101.

---

## What the program does

The `main` method runs a **linear demo** in this order:

1. **Position & department** — strings for role and unit (no separate types).
2. **Employee** — primitives and strings for Maria Reyes (ID, salary, allowances, IDs, etc.).
3. **Attendance** — time-in / time-out, late check, hours worked (lunch break rule for long shifts).
4. **Leave** — sick leave request, status, duration.
5. **Payroll** — gross pay, SSS / PhilHealth / Pag-IBIG / withholding tax, net pay, payslip output.
6. **Benefits** — rice, phone, clothing allowances (separate summary block, same numbers as payroll).
7. **User** — login failure, successful login, logout.
8. **HR manager** — Ana Cruz with “full access”: add/update employee, approve leave, run payroll again, generate a simple report.

Console lines tagged `[LeaveRequest]`, `[Payroll]`, `[HRManager]`, etc. mirror the **old OOP module names** for readability; they are **not** separate Java classes in this branch.

---

## Code map (`Main.java`)

| Area | Static helpers (examples) |
|---|---|
| Names & rates | `fullName`, `hourlyRateFromBasic` |
| Employee UI | `displayEmployeeInfo` |
| Attendance | `parseTimeToMinutes`, `checkIfLate`, `computeHoursWorked`, `printAttendanceSummary` |
| Leave | `submitLeave`, `printLeaveStatus`, `getLeaveDuration` |
| Pay & deductions | `computeSSSContribution`, `computePhilHealth`, `computePagIbig`, `computeWithholdingTax`, `computePayFigures`, `generatePayslip` |
| Benefits | `displayBenefits` |
| Auth | `login`, `logout` |
| HR demo | `hrAddEmployee`, `hrUpdateEmployee`, `hrApproveLeave`, `hrProcessPayroll`, `hrGenerateReport` |

---

## How to run

From the **repository root**:

```bash
cd MotorPHEmployeeApp/src
javac Main.java
java Main
```

Or from the `MotorPHEmployeeApp` folder:

```bash
cd MotorPHEmployeeApp
javac -d build src/Main.java
java -cp build Main
```

(`build/` is optional; create it with `mkdir -p build` if you use `-d`.)

You can also open `MotorPHEmployeeApp/src/Main.java` in any Java IDE and run the `main` class.

---

## Sample output (excerpt)

```
============================================
     MOTORPH EMPLOYEE APP - DEMO RUN
============================================

--- Setting up Position & Department ---

--- Creating Employee ---
 Employee Created: Maria Reyes

========================================
         EMPLOYEE INFORMATION
========================================
Employee ID      : 10001
Name             : Maria Reyes
...
--- Processing Payroll ---
...
  NET PAY        : PHP ...
============================================
          DEMO RUN COMPLETE
============================================
```

Exact figures depend on hours worked, basic salary, and the deduction rules below.

---

## Deduction logic

| Deduction | Basis |
|---|---|
| SSS | Bracket table based on basic salary |
| PhilHealth | 3% of salary, employee pays half (1.5%), with floor/ceiling rules in code |
| Pag-IBIG | 2% of salary, capped at PHP 100 |
| Withholding Tax | BIR-style monthly tax table on taxable income (basic − SSS − PhilHealth − Pag-IBIG) |

---

## Authors

- Ryan Tavera
- Armielyn Obinguar
- Chantal Flor
- Mitzi Arrogante
- Group 3 - H1101 | MO-IT103 Computer Programming 2
