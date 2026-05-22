# MotorPH Employee App

**MO-IT103 Computer Programming 2**
**Group 3 | Section H1101**

> **Demo Credentials**
> Username: `employee` &nbsp;|&nbsp; Password: `12345`

---

Open the integrated VS Code terminal (Ctrl + \`` or Cmd + ``) at your main workspace directory root, copy the entire block below matching your operating system, paste it, and press Enter:  

For Windows  
javac -d bin src/motorph_employeeapp/*.java  
java -cp bin motorph_employeeapp.MotorPH_EmployeeApp  

When launching the application interface, use any of the configured testing credentials below to access the system dashboard:  
Valid Usernames: employee or payroll_staff  
Valid Passwords: 12345 or password123  

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

---

## Component Architecture & Class Purposes

| Class | Purpose |
|---|---|
| `MotorPH_EmployeeApp` | Manages the application's root execution pipeline by enforcing credential verification dialog blocks before bootstrapping the GUI runtime thread. |
| `MotorPH_GUI` | Constructs the visual user workspace using Java Swing components, handling dashboard path routing, event-triggered action listeners, and real-time input error prompts. |
| `EmployeeModule` | Maintains structural CSV column layout indices and offers centralized tracking utilities to extract formatted employee naming attributes and flat numerical wage statistics. |
| `FileHandlerModule` | Acts as the dedicated data-layer connection engine responsible for executing line-by-line streaming, custom delimiter parsing, and exception-safe file I/O operations against data files. |
| `SalaryComputationModule` | Centralizes the application's mathematical processing rules to evaluate shift logs, statutory government withholdings (SSS, PhilHealth, Pag-IBIG), and multi-bracket income tax balances. |

---

## Features

- Login authentication with username and password
- Employee lookup by ID with real-time name display
- Payroll computation with semi-monthly cutoff breakdown
- Government deduction computation (SSS, PhilHealth, Pag-IBIG, Withholding Tax)
- Dropdown month selector for pay coverage
- Year validation (2024 data only)
- Comma-formatted salary output
- Exception handling for invalid inputs

---

## How to Run

1. Open the project in VS Code or NetBeans
2. Compile all `.java` files inside `src/motorph_employeeapp/`
3. Run `MotorPH_EmployeeApp.java` as the main entry point
4. Log in using the demo credentials above
5. Select **1. MPHCRO1: Pay Coverage** to compute payroll or **2. Employee Information** to look up an employee

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
- **Group:** 3
- **School:** Mapua Malayan Colleges Laguna (MMDC/MCL)


