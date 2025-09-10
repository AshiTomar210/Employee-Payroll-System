import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.util.*;

// Interface for employees who can receive bonuses
interface BonusEligible {
    double calculateBonus();
}

// Interface for employees who have tax deductions
interface Taxable {
    double calculateTax();
}

// Interface for employees with attendance tracking
interface AttendanceTrackable {
    void markAttendance(LocalDate date, boolean present);
    int getAttendanceDays();
    int getAbsenceDays();
}

// Interface for employees with leave management
interface LeaveManageable {
    void applyLeave(LocalDate startDate, LocalDate endDate, String reason);
    int getAvailableLeaves();
    List<LeaveRecord> getLeaveHistory();
}

// Interface for salary slip generation
interface PaySlipGeneratable {
    void generatePaySlip(int month, int year);
}

// Record for leave management
class LeaveRecord {
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private boolean approved;
    
    public LeaveRecord(LocalDate startDate, LocalDate endDate, String reason) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.approved = false; // Initially not approved
    }
    
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    
    public int getLeaveDays() {
        return Period.between(startDate, endDate).getDays() + 1;
    }
    
    @Override
    public String toString() {
        return "Leave: " + startDate + " to " + endDate + 
               " (" + getLeaveDays() + " days), Reason: " + reason + 
               ", Status: " + (approved ? "Approved" : "Pending");
    }
}

// Record for attendance
class AttendanceRecord {
    private LocalDate date;
    private boolean present;
    private int hoursWorked;
    
    public AttendanceRecord(LocalDate date, boolean present, int hoursWorked) {
        this.date = date;
        this.present = present;
        this.hoursWorked = hoursWorked;
    }
    
    public LocalDate getDate() { return date; }
    public boolean isPresent() { return present; }
    public int getHoursWorked() { return hoursWorked; }
    
    @Override
    public String toString() {
        return "Date: " + date + ", Present: " + present + 
               (present ? ", Hours: " + hoursWorked : "");
    }
}

// Abstract base class for all employees
abstract class Employee implements AttendanceTrackable, LeaveManageable, PaySlipGeneratable {
    private int id;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDate hireDate;
    private String department;
    private String position;
    private String address;
    private String bankAccount;
    
    private List<AttendanceRecord> attendanceRecords;
    private List<LeaveRecord> leaveRecords;
    private int totalLeaves = 20; // Standard annual leaves
    
    // Constructor
    public Employee(int id, String name, String email, String phoneNumber, 
                   LocalDate hireDate, String department, String position, 
                   String address, String bankAccount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.hireDate = hireDate;
        this.department = department;
        this.position = position;
        this.address = address;
        this.bankAccount = bankAccount;
        
        this.attendanceRecords = new ArrayList<>();
        this.leaveRecords = new ArrayList<>();
    }
    
    // Getters with encapsulation
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getHireDate() { return hireDate; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public String getAddress() { return address; }
    public String getBankAccount() { return bankAccount; }
    
    // Setters
    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }
    public void setAddress(String address) { this.address = address; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    
    // Attendance implementation
    @Override
    public void markAttendance(LocalDate date, boolean present) {
        markAttendance(date, present, present ? 8 : 0); // Default 8 hours if present
    }
    
    public void markAttendance(LocalDate date, boolean present, int hoursWorked) {
        // Remove existing record for the date if any
        attendanceRecords.removeIf(record -> record.getDate().equals(date));
        attendanceRecords.add(new AttendanceRecord(date, present, hoursWorked));
    }
    
    @Override
    public int getAttendanceDays() {
        return (int) attendanceRecords.stream()
                .filter(AttendanceRecord::isPresent)
                .count();
    }
    
    @Override
    public int getAbsenceDays() {
        return (int) attendanceRecords.stream()
                .filter(record -> !record.isPresent())
                .count();
    }
    
    public List<AttendanceRecord> getAttendanceRecords() {
        return new ArrayList<>(attendanceRecords);
    }
    
    public int getHoursWorkedInMonth(int month, int year) {
        return attendanceRecords.stream()
                .filter(record -> record.isPresent() && 
                                 record.getDate().getMonthValue() == month && 
                                 record.getDate().getYear() == year)
                .mapToInt(AttendanceRecord::getHoursWorked)
                .sum();
    }
    
    // Leave management implementation
    @Override
    public void applyLeave(LocalDate startDate, LocalDate endDate, String reason) {
        leaveRecords.add(new LeaveRecord(startDate, endDate, reason));
    }
    
    @Override
    public int getAvailableLeaves() {
        int usedLeaves = leaveRecords.stream()
                .filter(LeaveRecord::isApproved)
                .mapToInt(LeaveRecord::getLeaveDays)
                .sum();
        return totalLeaves - usedLeaves;
    }
    
    @Override
    public List<LeaveRecord> getLeaveHistory() {
        return new ArrayList<>(leaveRecords);
    }
    
    public void approveLeave(int index) {
        if (index >= 0 && index < leaveRecords.size()) {
            leaveRecords.get(index).setApproved(true);
        }
    }
    
    public void rejectLeave(int index) {
        if (index >= 0 && index < leaveRecords.size()) {
            leaveRecords.remove(index);
        }
    }
    
    // Pay slip generation
    @Override
    public void generatePaySlip(int month, int year) {
        double grossSalary = calculateSalary();
        double tax = 0;
        double bonus = 0;
        double deductions = 0;
        double netSalary = grossSalary;
        
        // Calculate tax if applicable
        if (this instanceof Taxable) {
            tax = ((Taxable) this).calculateTax();
            netSalary -= tax;
        }
        
        // Calculate bonus if applicable
        if (this instanceof BonusEligible) {
            bonus = ((BonusEligible) this).calculateBonus();
            netSalary += bonus;
        }
        
        // Calculate other deductions (simplified)
        double pfDeduction = grossSalary * 0.12; // 12% PF
        deductions += pfDeduction;
        netSalary -= pfDeduction;
        
        // Generate payslip
        System.out.println("\n========== PAYSLIP ==========");
        System.out.println("Employee ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Department: " + department);
        System.out.println("Position: " + position);
        System.out.println("Pay Period: " + month + "/" + year);
        System.out.println("Bank Account: " + bankAccount);
        System.out.println("-----------------------------");
        System.out.println("Gross Salary: $" + String.format("%.2f", grossSalary));
        System.out.println("Bonus: $" + String.format("%.2f", bonus));
        System.out.println("Tax: $" + String.format("%.2f", tax));
        System.out.println("PF Deduction: $" + String.format("%.2f", pfDeduction));
        System.out.println("Other Deductions: $" + String.format("%.2f", 0.0));
        System.out.println("Total Deductions: $" + String.format("%.2f", deductions + tax));
        System.out.println("Net Salary: $" + String.format("%.2f", netSalary));
        System.out.println("=============================\n");
        
        // Save to file
        savePaySlipToFile(month, year, grossSalary, bonus, tax, deductions, netSalary);
    }
    
    private void savePaySlipToFile(int month, int year, double gross, double bonus, 
                                  double tax, double deductions, double net) {
        String filename = "payslip_" + id + "_" + month + "_" + year + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("========== PAYSLIP ==========");
            writer.println("Employee ID: " + id);
            writer.println("Name: " + name);
            writer.println("Department: " + department);
            writer.println("Position: " + position);
            writer.println("Pay Period: " + month + "/" + year);
            writer.println("Bank Account: " + bankAccount);
            writer.println("-----------------------------");
            writer.println("Gross Salary: $" + String.format("%.2f", gross));
            writer.println("Bonus: $" + String.format("%.2f", bonus));
            writer.println("Tax: $" + String.format("%.2f", tax));
            writer.println("PF Deduction: $" + String.format("%.2f", deductions));
            writer.println("Other Deductions: $" + String.format("%.2f", 0.0));
            writer.println("Total Deductions: $" + String.format("%.2f", deductions + tax));
            writer.println("Net Salary: $" + String.format("%.2f", net));
            writer.println("=============================");
        } catch (IOException e) {
            System.out.println("Error saving payslip to file: " + e.getMessage());
        }
    }
    
    // Abstract method to be implemented by subclasses
    public abstract double calculateSalary();
    
    public int getYearsOfService() {
        return Period.between(hireDate, LocalDate.now()).getYears();
    }
    
    @Override
    public String toString() {
        return "ID: " + id + "\nName: " + name + "\nEmail: " + email + 
               "\nPhone: " + phoneNumber + "\nHire Date: " + hireDate +
               "\nDepartment: " + department + "\nPosition: " + position +
               "\nAddress: " + address + "\nBank Account: " + bankAccount +
               "\nYears of Service: " + getYearsOfService();
    }
}

// Full-time employee class
class FullTimeEmployee extends Employee implements BonusEligible, Taxable {
    private double monthlySalary;
    private int overtimeHours;
    private double overtimeRate;
    
    public FullTimeEmployee(int id, String name, String email, String phoneNumber, 
                           LocalDate hireDate, String department, String position, 
                           String address, String bankAccount, double monthlySalary, double overtimeRate) {
        super(id, name, email, phoneNumber, hireDate, department, position, address, bankAccount);
        this.monthlySalary = monthlySalary;
        this.overtimeRate = overtimeRate;
        this.overtimeHours = 0;
    }
    
    public void setOvertimeHours(int hours) {
        this.overtimeHours = hours;
    }
    
    @Override
    public double calculateSalary() {
        return monthlySalary + (overtimeHours * overtimeRate);
    }
    
    @Override
    public double calculateBonus() {
        // Bonus based on years of service
        int years = getYearsOfService();
        if (years < 1) return monthlySalary * 0.05; // 5% for less than 1 year
        if (years < 3) return monthlySalary * 0.10; // 10% for 1-2 years
        if (years < 5) return monthlySalary * 0.15; // 15% for 3-4 years
        return monthlySalary * 0.20; // 20% for 5+ years
    }
    
    @Override
    public double calculateTax() {
        double annualSalary = monthlySalary * 12;
        if (annualSalary <= 50000) {
            return monthlySalary * 0.10; // 10% tax
        } else if (annualSalary <= 100000) {
            return monthlySalary * 0.15; // 15% tax
        } else {
            return monthlySalary * 0.20; // 20% tax
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + "\nType: Full-Time\nMonthly Salary: $" + monthlySalary +
               "\nOvertime Rate: $" + overtimeRate + "/hour";
    }
}

// Part-time employee class
class PartTimeEmployee extends Employee implements Taxable {
    private double hourlyRate;
    private int hoursWorked;
    
    public PartTimeEmployee(int id, String name, String email, String phoneNumber, 
                           LocalDate hireDate, String department, String position, 
                           String address, String bankAccount, double hourlyRate) {
        super(id, name, email, phoneNumber, hireDate, department, position, address, bankAccount);
        this.hourlyRate = hourlyRate;
        this.hoursWorked = 0;
    }
    
    public void setHoursWorked(int hours) {
        this.hoursWorked = hours;
    }
    
    @Override
    public double calculateSalary() {
        return hourlyRate * hoursWorked;
    }
    
    @Override
    public double calculateTax() {
        double monthlyEarnings = calculateSalary();
        if (monthlyEarnings <= 3000) {
            return monthlyEarnings * 0.05; // 5% tax
        } else {
            return monthlyEarnings * 0.10; // 10% tax
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + "\nType: Part-Time\nHourly Rate: $" + hourlyRate;
    }
}

// Contractor employee class
class Contractor extends Employee {
    private double contractAmount;
    private int contractDuration; // in months
    
    public Contractor(int id, String name, String email, String phoneNumber, 
                     LocalDate hireDate, String department, String position, 
                     String address, String bankAccount, double contractAmount, int contractDuration) {
        super(id, name, email, phoneNumber, hireDate, department, position, address, bankAccount);
        this.contractAmount = contractAmount;
        this.contractDuration = contractDuration;
    }
    
    @Override
    public double calculateSalary() {
        return contractAmount / contractDuration;
    }
    
    @Override
    public String toString() {
        return super.toString() + "\nType: Contractor\nContract Amount: $" + contractAmount + 
               "\nContract Duration: " + contractDuration + " months";
    }
}

// Manager class with additional benefits
class Manager extends FullTimeEmployee {
    private double allowance;
    private List<Employee> teamMembers;
    
    public Manager(int id, String name, String email, String phoneNumber, 
                  LocalDate hireDate, String department, String position, 
                  String address, String bankAccount, double monthlySalary, 
                  double overtimeRate, double allowance) {
        super(id, name, email, phoneNumber, hireDate, department, position, 
              address, bankAccount, monthlySalary, overtimeRate);
        this.allowance = allowance;
        this.teamMembers = new ArrayList<>();
    }
    
    public void addTeamMember(Employee employee) {
        teamMembers.add(employee);
    }
    
    public void removeTeamMember(int employeeId) {
        teamMembers.removeIf(emp -> emp.getId() == employeeId);
    }
    
    public List<Employee> getTeamMembers() {
        return new ArrayList<>(teamMembers);
    }
    
    @Override
    public double calculateSalary() {
        return super.calculateSalary() + allowance + (teamMembers.size() * 50); // $50 per team member
    }
    
    @Override
    public double calculateBonus() {
        // Managers get higher bonus
        return super.calculateBonus() * 1.5;
    }
    
    @Override
    public String toString() {
        return super.toString() + "\nRole: Manager\nAllowance: $" + allowance +
               "\nTeam Size: " + teamMembers.size();
    }
}

// Payroll system class with file persistence
class PayrollSystem {
    private List<Employee> employees;
    private static final String DATA_FILE = "employees.dat";
    
    public PayrollSystem() {
        employees = new ArrayList<>();
        loadEmployees();
    }
    
    public void addEmployee(Employee employee) {
        employees.add(employee);
        saveEmployees();
    }
    public void removeEmployee(int id) {
        employees.removeIf(emp -> emp.getId() == id);
        saveEmployees();
    }
    
    public Employee findEmployee(int id) {
        for (Employee emp : employees) {
            if (emp.getId() == id) {
                return emp;
            }
        }
        return null;
    }
    
    public void generatePaySlipForEmployee(int id, int month, int year) {
        Employee emp = findEmployee(id);
        if (emp == null) {
            System.out.println("Employee not found!");
            return;
        }
        
        emp.generatePaySlip(month, year);
    }
    
    public void displayAllEmployees() {
        if (employees.isEmpty()) {
            System.out.println("No employees in the system.");
            return;
        }
        
        System.out.println("\n======== ALL EMPLOYEES ========");
        for (Employee emp : employees) {
            System.out.println(emp);
            System.out.println("-----------------------------");
        }
    }
    
    public void markAttendance(int id, LocalDate date, boolean present, int hours) {
        Employee emp = findEmployee(id);
        if (emp != null) {
            emp.markAttendance(date, present, hours);
            saveEmployees();
        } else {
            System.out.println("Employee not found!");
        }
    }
    
    public void applyForLeave(int id, LocalDate start, LocalDate end, String reason) {
        Employee emp = findEmployee(id);
        if (emp != null) {
            emp.applyLeave(start, end, reason);
            saveEmployees();
            System.out.println("Leave application submitted successfully!");
        } else {
            System.out.println("Employee not found!");
        }
    }
    
    public void processLeaveApplications() {
        System.out.println("\n=== PENDING LEAVE APPLICATIONS ===");
        int count = 0;
        for (Employee emp : employees) {
            List<LeaveRecord> leaves = emp.getLeaveHistory();
            for (int i = 0; i < leaves.size(); i++) {
                LeaveRecord leave = leaves.get(i);
                if (!leave.isApproved()) {
                    System.out.println("Employee: " + emp.getName() + " (ID: " + emp.getId() + ")");
                    System.out.println("Application #" + (count + 1) + ": " + leave);
                    count++;
                }
            }
        }
        
        if (count == 0) {
            System.out.println("No pending leave applications.");
            return;
        }
    }
    
    public void approveLeave(int empId, int leaveIndex) {
        Employee emp = findEmployee(empId);
        if (emp != null) {
            emp.approveLeave(leaveIndex);
            saveEmployees();
            System.out.println("Leave approved successfully!");
        } else {
            System.out.println("Employee not found!");
        }
    }
    
    public void generatePayrollReport(int month, int year) {
        System.out.println("\n=== PAYROLL REPORT FOR " + month + "/" + year + " ===");
        double totalSalary = 0;
        double totalTax = 0;
        double totalBonus = 0;
        
        for (Employee emp : employees) {
            double salary = emp.calculateSalary();
            double tax = 0;
            double bonus = 0;
            
            if (emp instanceof Taxable) {
                tax = ((Taxable) emp).calculateTax();
            }
            
            if (emp instanceof BonusEligible) {
                bonus = ((BonusEligible) emp).calculateBonus();
            }
            
            totalSalary += salary;
            totalTax += tax;
            totalBonus += bonus;
            
            System.out.println(emp.getName() + ": Salary=$" + String.format("%.2f", salary) + 
                              ", Tax=$" + String.format("%.2f", tax) + 
                              ", Bonus=$" + String.format("%.2f", bonus));
        }
        
        System.out.println("-----------------------------------");
        System.out.println("TOTAL: Salary=$" + String.format("%.2f", totalSalary) + 
                          ", Tax=$" + String.format("%.2f", totalTax) + 
                          ", Bonus=$" + String.format("%.2f", totalBonus));
        System.out.println("Net Payout: $" + String.format("%.2f", totalSalary + totalBonus - totalTax));
    }
    
    @SuppressWarnings("unchecked")
    private void loadEmployees() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            employees = (List<Employee>) ois.readObject();
            System.out.println("Employee data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No previous data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading employee data: " + e.getMessage());
        }
    }
    
    private void saveEmployees() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(employees);
        } catch (IOException e) {
            System.out.println("Error saving employee data: " + e.getMessage());
        }
    }
}

// Main class to run the payroll system
public class EnhancedEmployeePayrollSystem {
    private static LocalDate getDateFromInput(Scanner scanner, String prompt) {
        LocalDate date = null;
        boolean validDate = false;
        
        while (!validDate) {
            System.out.print(prompt);
            String dateStr = scanner.nextLine().trim();
            
            try {
                if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                    String[] parts = dateStr.split("-");
                    String formattedDate = String.format("%s-%02d-%02d", 
                        parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                    date = LocalDate.parse(formattedDate);
                    validDate = true;
                } else {
                    System.out.println("Invalid date format. Please use YYYY-MM-DD or YYYY-M-D format.");
                }
            } catch (Exception e) {
                System.out.println("Error parsing date: " + e.getMessage());
                System.out.println("Please enter a valid date in YYYY-MM-DD format (e.g., 2025-10-02).");
            }
        }
        
        return date;
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PayrollSystem payrollSystem = new PayrollSystem();
        
        // Adding sample employees if none exist
        if (payrollSystem.findEmployee(1) == null) {
            payrollSystem.addEmployee(new FullTimeEmployee(1, "John Doe", "john@company.com", "123-456-7890", 
                    LocalDate.of(2020, 1, 15), "IT", "Developer", 
                    "123 Main St, City", "ACC123456", 5000, 25.0));
            
            payrollSystem.addEmployee(new PartTimeEmployee(2, "Jane Smith", "jane@company.com", "098-765-4321", 
                    LocalDate.of(2021, 3, 10), "Marketing", "Assistant", 
                    "456 Oak St, Town", "ACC654321", 20.0));
            
            payrollSystem.addEmployee(new Contractor(3, "Bob Johnson", "bob@contractor.com", "555-123-4567", 
                    LocalDate.of(2022, 6, 1), "Operations", "Consultant", 
                    "789 Pine St, Village", "ACC987654", 30000, 6));
            
            Manager manager = new Manager(4, "Alice Brown", "alice@company.com", "555-987-6543", 
                    LocalDate.of(2019, 8, 20), "IT", "Manager", 
                    "321 Elm St, Metropolis", "ACC456789", 7000, 30.0, 1000);
            
            // Add team members to manager
            manager.addTeamMember(payrollSystem.findEmployee(1));
            manager.addTeamMember(payrollSystem.findEmployee(2));
            
            payrollSystem.addEmployee(manager);
        }
        
        int choice;
        do {
            System.out.println("\n===== ENHANCED EMPLOYEE PAYROLL SYSTEM =====");
            System.out.println("1. Display All Employees");
            System.out.println("2. Generate Payslip");
            System.out.println("3. Add New Employee");
            System.out.println("4. Mark Attendance");
            System.out.println("5. Apply for Leave");
            System.out.println("6. Process Leave Applications");
            System.out.println("7. Generate Payroll Report");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            
            switch (choice) {
                case 1:
                    payrollSystem.displayAllEmployees();
                    break;
                    
                case 2:
                    System.out.print("Enter Employee ID: ");
                    int id = scanner.nextInt();
                    System.out.print("Enter Month (1-12): ");
                    int month = scanner.nextInt();
                    System.out.print("Enter Year: ");
                    int year = scanner.nextInt();
                    payrollSystem.generatePaySlipForEmployee(id, month, year);
                    break;
                    
                case 3:
                    addNewEmployee(scanner, payrollSystem);
                    break;
                    
                case 4:
                    markAttendance(scanner, payrollSystem);
                    break;
                    
                case 5:
                    applyForLeave(scanner, payrollSystem);
                    break;
                    
                case 6:
                    payrollSystem.processLeaveApplications();
                    // In a real system, you would add logic to approve/reject leaves
                    break;
                    
                case 7:
                    System.out.print("Enter Month (1-12): ");
                    int rptMonth = scanner.nextInt();
                    System.out.print("Enter Year: ");
                    int rptYear = scanner.nextInt();
                    payrollSystem.generatePayrollReport(rptMonth, rptYear);
                    break;
                    
                case 8:
                    System.out.println("Exiting...");
                    break;
                    
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 8);
        
        scanner.close();
    }
    
    private static void addNewEmployee(Scanner scanner, PayrollSystem payrollSystem) {
        System.out.println("Select Employee Type:");
        System.out.println("1. Full-Time");
        System.out.println("2. Part-Time");
        System.out.println("3. Contractor");
        System.out.println("4. Manager");
        System.out.print("Enter choice: ");
        int type = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        System.out.print("Enter ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        
        System.out.print("Enter Department: ");
        String department = scanner.nextLine();
        
        System.out.print("Enter Position: ");
        String position = scanner.nextLine();
        
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();
        
        System.out.print("Enter Bank Account: ");
        String bankAccount = scanner.nextLine();
        
      // Replace this section in addNewEmployee method:
        // System.out.print("Enter Hire Date (YYYY-MM-DD): ");
        // String dateStr = scanner.nextLine();
        // LocalDate hireDate = LocalDate.parse(dateStr);

// With this improved version:
   // Replace the hire date section with this:
LocalDate hireDate = null;
boolean validDate = false;
while (!validDate) {
    System.out.print("Enter Hire Date (YYYY-MM-DD or YYYY-M-D): ");
    String dateStr = scanner.nextLine().trim();
    
    try {
        if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            String[] parts = dateStr.split("-");
            String formattedDate = String.format("%s-%02d-%02d", 
                parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            hireDate = LocalDate.parse(formattedDate);
            validDate = true;
        } else {
            System.out.println("Invalid date format. Please use YYYY-MM-DD or YYYY-M-D format.");
        }
    } catch (Exception e) {
        System.out.println("Error parsing date: " + e.getMessage());
        System.out.println("Please enter a valid date in YYYY-MM-DD format (e.g., 2025-10-02).");
    }
}
        switch (type) {
            case 1:
                System.out.print("Enter Monthly Salary: ");
                double salary = scanner.nextDouble();
                System.out.print("Enter Overtime Rate: ");
                double overtimeRate = scanner.nextDouble();
                payrollSystem.addEmployee(new FullTimeEmployee(id, name, email, phone, hireDate, 
                        department, position, address, bankAccount, salary, overtimeRate));
                break;
                
            case 2:
                System.out.print("Enter Hourly Rate: ");
                double hourlyRate = scanner.nextDouble();
                payrollSystem.addEmployee(new PartTimeEmployee(id, name, email, phone, hireDate, 
                        department, position, address, bankAccount, hourlyRate));
                break;
                
            case 3:
                System.out.print("Enter Contract Amount: ");
                double contractAmount = scanner.nextDouble();
                System.out.print("Enter Contract Duration (months): ");
                int duration = scanner.nextInt();
                payrollSystem.addEmployee(new Contractor(id, name, email, phone, hireDate, 
                        department, position, address, bankAccount, contractAmount, duration));
                break;
                
            case 4:
                System.out.print("Enter Monthly Salary: ");
                double mgrSalary = scanner.nextDouble();
                System.out.print("Enter Overtime Rate: ");
                double mgrOvertimeRate = scanner.nextDouble();
                System.out.print("Enter Allowance: ");
                double allowance = scanner.nextDouble();
                payrollSystem.addEmployee(new Manager(id, name, email, phone, hireDate, 
                        department, position, address, bankAccount, mgrSalary, mgrOvertimeRate, allowance));
                break;
                
            default:
                System.out.println("Invalid employee type.");
        }
        
        System.out.println("Employee added successfully!");
    }
    
    private static void markAttendance(Scanner scanner, PayrollSystem payrollSystem) {
        System.out.print("Enter Employee ID: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        LocalDate date = getDateFromInput(scanner, "Enter Date (YYYY-MM-DD or YYYY-M-D): ");
        
        System.out.print("Was employee present? (true/false): ");
        boolean present = false;
        String presentInput = scanner.nextLine().trim().toLowerCase();
        
        // Handle different ways user might input true/false
        if (presentInput.equals("true") || presentInput.equals("t") || 
            presentInput.equals("yes") || presentInput.equals("y") || 
            presentInput.equals("1")) {
            present = true;
        } else if (presentInput.equals("false") || presentInput.equals("f") || 
                   presentInput.equals("no") || presentInput.equals("n") || 
                   presentInput.equals("0")) {
            present = false;
        } else {
            System.out.println("Invalid input. Assuming employee was not present.");
            present = false;
        }
        
        int hours = 0;
        if (present) {
            boolean validHours = false;
            while (!validHours) {
                System.out.print("Enter hours worked (0-24): ");
                try {
                    hours = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    if (hours >= 0 && hours <= 24) {
                        validHours = true;
                    } else {
                        System.out.println("Invalid hours. Please enter a value between 0 and 24.");
                    }
                } catch (Exception e) {
                    System.out.println("Invalid input. Please enter a number between 0 and 24.");
                    scanner.nextLine(); // Clear invalid input
                }
            }
        }
        
        payrollSystem.markAttendance(id, date, present, hours);
        System.out.println("Attendance marked successfully!");
    }
    
    private static void applyForLeave(Scanner scanner, PayrollSystem payrollSystem) {
        System.out.print("Enter Employee ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        
        LocalDate startDate = getDateFromInput(scanner, "Enter Start Date (YYYY-MM-DD or YYYY-M-D): ");
        
        LocalDate endDate = null;
        boolean validEndDate = false;
        while (!validEndDate) {
            endDate = getDateFromInput(scanner, "Enter End Date (YYYY-MM-DD or YYYY-M-D): ");
            
            // Validate that end date is not before start date
            if (endDate.isBefore(startDate)) {
                System.out.println("End date cannot be before start date. Please try again.");
            } else {
                validEndDate = true;
            }
        }
        
        System.out.print("Enter Reason: ");
        String reason = scanner.nextLine();
        
        payrollSystem.applyForLeave(id, startDate, endDate, reason);
    }
}