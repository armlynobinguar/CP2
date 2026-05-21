# MotorPH Employee App  
**Username:** employee  
**Password:** 12345  

**Members of Group 3**  
Arrogante, Flor, Obinguar, Tavera

## Component Architecture & Class Purposes
MotorPH_EmployeeApp (MAIN)  
Manages the application's root execution pipeline by enforcing credential verification dialog blocks before bootstrapping the GUI runtime thread.  

MotorPH_GUI  
Constructs the visual user workspace using Java Swing components, handling dashboard path routing, event-triggered action listeners, and real-time input error prompts.  

EmployeeModule  
Maintains structural CSV column layout indices and offers centralized tracking utilities to extract formatted employee naming attributes and flat numerical wage statistics.  

FileHandlerModule  
Acts as the dedicated data-layer connection engine responsible for executing line-by-line streaming, custom delimiter parsing, and exception-safe file I/O operations against data files.

SalaryComputationModule  
Centralizes the application’s mathematical processing rules to evaluate shift logs, statutory government withholdings (SSS, PhilHealth, Pag-IBIG), and multi-bracket income tax balances.

Links  
[Project Plan](https://docs.google.com/spreadsheets/d/1ZfEM7OL4OEOAmj9opmkJDVqFON5w2NJV129yVMBLhbI/edit?usp=sharing)  
[Figma](https://www.figma.com/make/yrUok9BbHVPctk43Czg4Bo/MotorPH-Payroll-System-CP2?t=RpZwUqjSXZqqEahE-0)  
[Class Diagram](https://docs.google.com/spreadsheets/d/18u4H9f2NgLQ9XYUVs5bcBgl3MJHv__k_ww2FdErt1C0/edit?usp=sharing)   
