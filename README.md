# MotorPH Employee App

**MO-IT103 Computer Programming 2**  
**Group 3 | Section H1101**

> **Demo Credentials**
>
> | Portal | Username | Password | Notes |
> |---|---|---|---|
> | **Employee** | `employee` | `12345` | Linked to employee **#10001** |
> | **HR** | `hr` | `hr12345` | Full HR management access |
> | **HR (legacy)** | `payroll_staff` | `password123` | Still accepted for backward compatibility |

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

The MotorPH Employee App is a Java Swing payroll and employee management system for MO-IT103. It supports **two role-based portals** — **Employee** and **HR** — with shared CSV-backed data for employee profiles and daily attendance.

The app uses a **procedural modular architecture**: business logic lives in static module classes (`EmployeeModule`, `FileHandlerModule`, `SalaryComputationModule`, etc.), while `MotorPH_GUI.java` handles all screens, navigation, and user events. This keeps computation and file I/O separate from the UI layer.

---

## What Changed (Current vs. Earlier Version)

| Area | Earlier version | Current version |
|---|---|---|
| **Authentication** | Single login; `payroll_staff` / `password123` only | Dual portal: Employee (`employee` / `12345`) and HR (`hr` / `hr12345`); legacy HR credentials still work |
| **Main navigation** | 3-button menu: Pay Coverage, Employee Information, Logout | Role-based sidebar dashboards with cards for Payslip, Profile, Records, Payroll, Directory, Notifications |
| **Employee portal** | Not available | Dashboard, My Payslip, My Profile, directory lookup, in-app notifications |
| **HR portal** | Basic payroll + lookup only | Employee Records CRUD, revision history/revert, batch payroll, announcements, calendar dashboard |
| **Payroll** | Single employee only; plain text output | **Single** and **Batch** modes; attendance panel; styled results; Copy / `.txt` / `.pdf` export |
| **Batch payroll** | N/A | Filter by department & status, select-all, sortable table, gross/deduction/net summary chips |
| **Employee records** | Read-only lookup by ID | Full CRUD with validation, search/filter, department/position dropdowns, gross semi-monthly → hourly rate auto-compute |
| **Revisions** | N/A | Snapshot log on add/edit/delete with revert support |
| **Departments** | Hard-coded in forms | `DepartmentModule` — departments, positions per department, supervisor resolution |
| **Notifications** | N/A | Dynamic HR and employee notifications (payroll, attendance, birthdays) |
| **CSV schema** | 19 columns; hourly rate at index 18 | 24 columns; gross semi-monthly, hourly rate, computed pay fields; schema migration via `ensureEmployeeFileSchema()` |
| **PDF payslip** | N/A or broken layout | Clean PDF export with proper borders, centered header, and PHP formatting |
| **Employee My Payslip** | Manual form (employee #, month, year, Generate) | Full-width scrollable PDF-style viewer; **Older** / **Newer** navigation; funnel filter for pay periods and date ranges; save all/range PDFs; **Report Issue** |
| **Employee responsive UI** | Fixed layout | Dashboard, profile, payslip, and notifications adapt on window resize |
| **HR batch payroll UX** | Basic table + calculate | Funnel filter (department/status), live gross/deductions/net sync on filter changes; works with 1+ employees |
| **HR employee records** | Manual hourly rate entry | Gross semi-monthly editable; hourly rate auto-computed; birthday picker; NA normalization |

---

## Features by Portal

### Employee Portal (`employee` / `12345`)

- **Dashboard** — welcome cards for payslip and profile; calendar stacks on narrow windows
- **My Payslip** — scrollable PDF-style payslip for the linked employee (#10001)
  - Header shows the active pay period (e.g. `June 2024 · Jun 1–15`)
  - **Older** / **Newer** — step through available periods
  - **Filter (funnel icon)** — pick a pay period, select a **From / To** range, **View Selected Period**, **Save Range as PDFs**, or **Save All Payslips (PDF)**
  - Actions: Copy, Download PDF, Download `.txt`, **Report Issue** (saved to `resources/payslip_issues.txt`)
- **My Profile** — view compensation and edit address/phone; single-column layout on narrow screens
- **Notifications** — payroll reminders, attendance alerts, birthday notices; filter chips wrap on small widths

### HR Portal (`hr` / `hr12345`)

- **Dashboard** — overview with calendar widget
- **Employee Records** — add, edit, delete, search, filter; view full profile; attendance dialog (edit/delete via modal popups — see [Implementation notes](#hr-employee-records--implementation-notes) below)
- **Revision History** — audit trail of record changes with revert
- **Payroll Processing**
  - **Single mode** — one employee, pay period attendance table, calculate, export
  - **Batch mode** — filter employees, checkbox selection with select-all, sortable results table, styled summary with Gross / Deductions / Net Pay chips
- **Employee Directory** — company-wide lookup
- **HR Announcements / Notifications**

### Payroll & Deductions

- Attendance-based hours with grace period, lunch break, and end-of-day cap
- Semi-monthly cutoffs: **1–15** and **16–31**
- Government deductions: **SSS**, **PhilHealth**, **Pag-IBIG**, **BIR withholding tax**
- Export options: **Copy to clipboard**, **Download `.txt`**, **Download `.pdf`**
- Year validation: **2024** (matches bundled CSV data); month range **June–December**

---

## Project Structure

```
CP2/
├── resources/
│   ├── MotorPH_Employee Data - Employee Details.csv   ← Master employee data (24 columns)
│   ├── MotorPH_Employee Data - Attendance Record.csv  ← Daily login/logout logs
│   ├── employee_revision_index.txt                    ← Generated at runtime (gitignored)
│   ├── payslip_issues.txt                             ← Employee issue reports (gitignored)
│   ├── notifications.txt                              ← Optional notification persistence (gitignored)
│   └── revisions/                                     ← Revision snapshots (gitignored)
├── src/motorph_employeeapp/
│   ├── MotorPH_EmployeeApp.java     ← Entry point, role-based authentication
│   ├── MotorPH_GUI.java             ← All Swing UI, navigation, and event handlers
│   ├── EmployeeModule.java          ← CSV column constants, name/rate/search helpers
│   ├── FileHandlerModule.java       ← CSV read/write, smartSplit, schema migration
│   ├── SalaryComputationModule.java ← Payroll math, bulk compute, deductions
│   ├── EmployeeRecordsModule.java   ← CRUD validation, table rows, gross→hourly rate
│   ├── EmployeeRevisionModule.java  ← Change snapshots and revert
│   ├── DepartmentModule.java        ← Departments, positions, supervisors
│   └── NotificationModule.java      ← In-app notification model and helpers
├── bin/                             ← Compiled classes (gitignored)
├── build.xml                        ← NetBeans Ant build
└── nbproject/                       ← NetBeans project config
```

---

## How to Run

### Prerequisites

- [JDK 21](https://adoptium.net/) or newer (`java` and `javac` on your PATH)
- Both CSV files under `resources/` at the project root
- Run commands from the **project root** (`CP2/`) so CSV paths resolve correctly

Verify Java:

```bash
java -version
javac -version
```

### Option 1 — Terminal (recommended)

```bash
# Compile
mkdir -p bin
javac -d bin src/motorph_employeeapp/*.java

# Run
java -cp bin motorph_employeeapp.MotorPH_EmployeeApp
```

The login dialog opens. Sign in with Employee or HR credentials above. You are routed to the matching portal dashboard.

### Option 2 — VS Code

1. Open the `CP2` folder in VS Code.
2. Install the **Extension Pack for Java** (Microsoft).
3. Open `src/motorph_employeeapp/MotorPH_EmployeeApp.java`.
4. Run above `main` (F5 or **Run > Run Without Debugging**).
5. Set the working directory to the project root if prompted.

### Option 3 — NetBeans

1. Open the project in NetBeans.
2. Right-click → **Run** (main class: `motorph_employeeapp.MotorPH_EmployeeApp`).

Or from the project root:

```bash
ant run
```

### Troubleshooting

| Issue | Fix |
|---|---|
| `Unable to locate a Java Runtime` | Install JDK 21+; on macOS try `/usr/libexec/java_home -V` to locate JDK |
| Employee or attendance data not loading | Run from `CP2/`, not from `src/` or `bin/` |
| Compile errors after pulling changes | Delete `bin/` and recompile |
| Employee portal shows wrong profile | Demo account `employee` is hard-linked to employee **#10001** |
| PDF export looks wrong | Use **Download .pdf** from payroll results; layout uses custom PDF drawing helpers |
| Revision files missing | Normal on first run — created under `resources/` when HR edits records |
| Payslip issue reports not in repo | `resources/payslip_issues.txt` is runtime-only and gitignored |
| Employee payslip empty for a month | Use the filter icon to pick a period with attendance; demo data is mostly **2024** (Jun–Dec) |

---

## Module Reference

### `MotorPH_EmployeeApp.java` — Entry & Auth

| Item | Description |
|---|---|
| `EMPLOYEE_USERNAME` / `EMPLOYEE_PASSWORD` | `employee` / `12345` |
| `HR_USERNAME` / `HR_PASSWORD` | `hr` / `hr12345` |
| `HR_USERNAME_LEGACY` / `HR_PASSWORD_LEGACY` | `payroll_staff` / `password123` |
| `LINKED_EMPLOYEE_ID` | `"10001"` — demo employee portal account |
| `resolveLinkedEmployeeId(username)` | Maps portal username to CSV employee ID |
| `main(String[] args)` | Shows login → initializes GUI on success |

### `MotorPH_GUI.java` — UI Layer

Central Swing application (~8,900 lines). Key responsibilities:

| Area | Key methods / behavior |
|---|---|
| Login | `showCustomLoginDialog()` — styled modal, Enter key support |
| Bootstrap | `initialize()` — sets frame title by role, opens dashboard |
| Employee screens | PDF-style My Payslip viewer, period filter, bulk PDF export, report issue, responsive profile/notifications |
| HR screens | Employee Records CRUD (popup edit/delete), revision history, payroll single/batch |
| Payroll UI | HR: Single/Batch toggle, funnel filter, styled results; Employee: scrollable payslip document + summary chips |
| Export | Clipboard copy, `.txt` download, `.pdf` payslip; employee bulk export to a chosen folder |
| Navigation | Sidebar, page headers, role-gated menu items |

Legacy methods like `showMainMenu()`, `setupPayrollUI()` are superseded by the portal dashboard flow but payroll calculation still flows through `SalaryComputationModule`.

### `EmployeeModule.java` — Column Constants & Utilities

Employee CSV rows are `String[]` with **24 columns** (`COLUMN_COUNT = 24`):

| Index | Constant | Field |
|---|---|---|
| 0 | `ID` | Employee number |
| 1–2 | `LAST_NAME`, `FIRST_NAME` | Name |
| 3–9 | `BIRTHDAY` … `PAGIBIG` | Personal & gov IDs |
| 10–13 | `STATUS`, `POSITION`, `DEPARTMENT`, `IMMEDIATE_SUPERVISOR` | Employment |
| 14–18 | `BASIC_SALARY` … `GROSS_SEMI_MONTHLY` | Compensation |
| 19–23 | `HOURLY_RATE` … `NET_PAY` | Rates & computed pay fields |

| Method | Description |
|---|---|
| `fullName(String[] emp)` | `"First Last"` display name |
| `getHourlyRate(String[] emp)` | Parses hourly rate (comma-safe) |
| `searchByNameOrId(String query)` | Directory search helper |

### `FileHandlerModule.java` — CSV I/O

| Constant / Method | Description |
|---|---|
| `EMPLOYEE_FILE` | `resources/MotorPH_Employee Data - Employee Details.csv` |
| `ATTENDANCE_FILE` | `resources/MotorPH_Employee Data - Attendance Record.csv` |
| `resolveDataFile()` | Finds CSV from project root, `bin/`, or IDE cwd |
| `findEmployeeData(id)` | Raw CSV line by employee ID |
| `findAttendanceData(id)` | All attendance rows for an employee |
| `getAllEmployees()` | Parsed employee rows (no header) |
| `getAllAttendanceRecords()` | All attendance rows |
| `smartSplit(line)` | Quote-aware CSV parser |
| `ensureEmployeeFileSchema()` | Migrates older CSV to 24-column format |
| `normalizeEmployeeRow(row)` | Pads/truncates row to `COLUMN_COUNT` |
| `appendEmployeeRecord(line)` | Add new employee |
| `updateEmployeeRecord(id, row)` | Update existing employee |
| `deleteEmployeeRecord(id)` | Remove employee |
| `rewriteEmployeeFile(list)` | Full file rewrite after bulk changes |
| `getNextEmployeeNumber()` | Auto-increment ID for new records |
| `loadStructuredNotifications()` / `saveStructuredNotifications()` | Notification persistence |
| `appendPayslipIssueReport(...)` | Appends employee payslip concern lines to `resources/payslip_issues.txt` |

### `SalaryComputationModule.java` — Payroll Engine

| Method | Description |
|---|---|
| `calculatePayroll(emp, month, year, output)` | Single-employee payslip to `JTextArea` |
| `computeAllEmployeeSalaries(month, year, employees)` | Batch payroll for selected employees |
| `validatePayrollInputs(employees, month, year)` | Pre-flight validation |
| `calculateShift(logIn, logOut)` | Billable hours (grace, lunch, 17:00 cap) |
| `sumAttendanceHours(id, month, year)` | Total hours for pay period |
| `computeSSS` / `computePhilHealth` / `computePagIBIG` | Statutory contributions |
| `computeWithholdingTax` / `calculateWithholdingTax` | BIR tax (6-bracket table) |
| `computeGrossPay`, `computeDeductions`, `computeNetPay` | Array helpers for bulk mode |
| `findWorkingPeriods(id)` | Months with attendance for an employee |
| `monthName(monthStr)` | `"6"` → `"June"` |

**Payroll flow (single employee):**

```
findAttendanceData → calculateShift per day → split by cutoff (1–15 / 16–31)
→ gross = hours × hourlyRate → SSS + PhilHealth + Pag-IBIG + tax
→ net (deductions applied on 2nd cutoff) → formatted payslip output
```

Static fields (`lastGrossFirst`, `lastSss`, `summaryNet`, etc.) hold the most recent calculation for export.

### `EmployeeRecordsModule.java` — HR CRUD

| Method | Description |
|---|---|
| `TABLE_COLUMNS` | Column headers for the records table |
| `toTableRow(emp)` | Maps CSV row to table display |
| `formatFullProfile(emp)` | Multi-line profile text |
| `RecordFormData` | Form field container for add/edit popups |
| `validateForm` / `validateAddPopup` / `validateEditPopup` | Field validation |
| `applyFormToRow` / `createNewRow` / `buildFullRowFromForm` | Form → CSV row |
| `computeHourlyRateFromGrossSemiMonthly(gross)` | Live rate: `(gross × 2) / (22 × 8)` |
| `collectViewWarnings(emp)` | Missing-field warnings on view |

#### HR Employee Records — implementation notes

These behaviors satisfy the update/delete requirements but differ slightly from a classic inline-form layout:

- **Popup dialogs, not inline forms** — The Employee Records screen is table-only. Selecting a row enables **View**, **Edit**, and **Delete**; **Edit** and **Add** open modal dialogs with labeled fields pre-filled from the CSV. There is no inline form panel on the main screen.
- **Legacy helpers** — `runUpdateEmployeeRecord()` and `populateEmployeeRecordForm()` remain in `MotorPH_GUI.java` from an earlier inline-form layout. The live edit path is **Edit** → `showSelectedEmployeeEditDialog()` → `showEmployeeEditPopup()`.
- **Revision history (extra)** — Before each add, edit, or delete, `EmployeeRevisionModule.logChange()` saves a full CSV snapshot so HR can review history and revert. This goes beyond the basic CRUD spec but does not change normal save/delete behavior.

| GUI entry point | Module calls |
|---|---|
| Edit → Save | `validateEditPopup()` → `applyFormToRow()` → `FileHandlerModule.updateEmployeeRecord()` → `refreshEmployeeTable()` |
| Delete | Confirm dialog → `FileHandlerModule.deleteEmployeeRecord()` → `refreshEmployeeTable()` |

### `EmployeeRevisionModule.java` — Audit Trail

| Method | Description |
|---|---|
| `loadFromDisk()` | Loads revision index on startup |
| `logChange(action, id, summary, beforeSnapshot)` | Records add/edit/delete |
| `getEntries()` | All revision entries (newest first) |
| `revert(entry)` | Restores a prior employee snapshot |

Data stored in `resources/employee_revision_index.txt` and `resources/revisions/` (gitignored).

### `DepartmentModule.java` — Org Structure

| Method | Description |
|---|---|
| `DEPARTMENTS` | Fixed department list |
| `allDepartments()` | All department names |
| `positionsForDepartment(dept)` | Positions in a department |
| `inferDepartmentFromPosition(position)` | Reverse lookup |
| `resolveSupervisor(dept, position)` | Default supervisor name |
| `formatSupervisorName(emp)` | Display helper |

### `NotificationModule.java` — Notifications

| Item | Description |
|---|---|
| `Notification` | Model: type, title, message, timestamp, read flag |
| `Notification.parseLine(line)` | Deserialize from storage |
| Built dynamically in GUI | Payroll deadlines, attendance gaps, birthdays |

---

## Government Deduction Reference

### SSS (monthly)

| Monthly Salary | Contribution |
|---|---|
| Below PHP 3,250 | PHP 135.00 |
| PHP 3,250 – PHP 24,749 | PHP 157.50 – PHP 1,102.50 (PHP 22.50 steps per PHP 500) |
| PHP 24,750+ | PHP 1,125.00 (max) |

### PhilHealth (employee share)

| Monthly Salary | Share |
|---|---|
| ≤ PHP 10,000 | PHP 150.00 |
| PHP 10,001 – PHP 59,999 | 1.5% of salary |
| ≥ PHP 60,000 | PHP 900.00 (max) |

### BIR Withholding Tax (taxable income = gross − SSS − PhilHealth − Pag-IBIG)

| Taxable Income | Tax |
|---|---|
| ≤ PHP 20,832 | PHP 0 |
| PHP 20,833 – PHP 33,332 | 20% of excess over PHP 20,833 |
| PHP 33,333 – PHP 66,666 | PHP 2,500 + 25% of excess over PHP 33,333 |
| PHP 66,667 – PHP 166,666 | PHP 10,833 + 30% of excess over PHP 66,667 |
| PHP 166,667 – PHP 666,666 | PHP 40,833.33 + 32% of excess over PHP 166,667 |
| Above PHP 666,667 | PHP 200,833.33 + 35% of excess over PHP 666,667 |

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
| MPHCR01 - Feature 1 Change Request | [MPHCR01](https://docs.google.com/spreadsheets/d/1e4E4m4BBXRVHvcifofmNJzgd9UsucakaDIeEcfempWU/edit?gid=475634283#gid=475634283) |
| Computer Programming 1 | [MO-IT101-Group5](https://github.com/MitziReese04/MO-IT101-Group5.git) |

---

## Course Information

- **Subject:** MO-IT103 Computer Programming 2
- **Section:** H1101
- **Group:** 3 - TeamPURA
- **School:** Mapua Malayan Colleges Laguna (MMDC/MCL)
