# 🧾 Employee Payroll System

A comprehensive Java-based payroll management system that handles employee data, attendance, leave applications, and salary processing. It demonstrates object-oriented programming principles and real-world application design.

## ✨ Features

- **Employee Management**: Support for full-time, part-time, contract, and managerial staff
- **Attendance Tracking**: Record and monitor employee presence and working hours
- **Leave Management**: Process employee leave applications and track balances
- **Payroll Processing**: Automatically calculate salaries, taxes, bonuses, and deductions
- **Pay Slip Generation**: Create detailed pay slips with export functionality
- **Data Persistence**: Save and load employee data between sessions
- **Reporting**: Generate payroll summaries and leave reports

## 🛠️ Technologies Used

- Java Programming Language
- Object-Oriented Programming (Inheritance, Encapsulation, Interfaces)
- Java Serialization for data storage
- Date/Time API for attendance and leave tracking

## 📦 Installation & Usage

1. Clone or download the project source code
2. Compile the Java files: `javac *.java`
3. Run the main class: `java EnhancedEmployeePayrollSystem`
4. Use the menu system to navigate features

## 🏗️ System Design

The system uses a structured class hierarchy with:
- Abstract `Employee` base class
- Specialized employee type subclasses
- Multiple interfaces for bonus eligibility, taxation, and attendance tracking
- Manager class with team management capabilities

## 📄 License

This is a demonstration project for educational purposes.