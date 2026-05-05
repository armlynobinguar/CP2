# MotorPH Employee App

A Java console application for MotorPH HR/payroll demo scenarios.

This codebase is intentionally **procedural and modular**:
- No domain OOP classes like `Employee`, `Payroll`, `Department`, etc.
- `Main.java` handles the program flow.
- Logic is split into static helper modules.

Homework 2 · MO-IT103 Computer Programming 2 · Group 3 - H1101.

---

## Project Structure

All source files are in `MotorPHEmployeeApp/src`:

- `Main.java` — orchestration of the end-to-end demo
- `DisplayModule.java` — header, employee summary, benefits summary
- `AttendanceModule.java` — time parsing, late check, hours worked
- `LeaveModule.java` — leave request and duration
- `PayrollModule.java` — payroll math and payslip rendering
- `UserModule.java` — login/logout demo flow
- `HrModule.java` — HR action demo flow

---

## Demo Flow

`Main.main()` runs the following steps:

1. Print app header.
2. Initialize sample employee values.
3. Show employee information.
4. Compute attendance hours from time-in/time-out.
5. Print leave request details.
6. Generate payroll/payslip output.
7. Show benefits summary.
8. Run user login/logout simulation.
9. Run HR action simulation.
10. Print end-of-demo footer.

---

## How to Run

From the repository root:

```bash
cd MotorPHEmployeeApp/src
javac *.java
java Main
```

Or from `MotorPHEmployeeApp` using a build folder:

```bash
cd MotorPHEmployeeApp
mkdir -p build
javac -d build src/*.java
java -cp build Main
```

---

## Deduction Rules (PayrollModule)

| Deduction | Basis |
|---|---|
| SSS | Bracket table based on basic salary |
| PhilHealth | 3% of salary, employee share is half (1.5%), with floor/ceiling handling |
| Pag-IBIG | 1%/2% rule with max contribution cap of PHP 100 |
| Withholding Tax | Tiered monthly tax table on taxable income |

---

## Authors

- Ryan Tavera
- Armielyn Obinguar
- Chantal Flor
- Mitzi Arrogante
- Group 3 - H1101 | MO-IT103 Computer Programming 2
