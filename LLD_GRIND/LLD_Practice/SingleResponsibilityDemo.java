package LLD_Practice;

/**
 * Demonstrates the Single Responsibility Principle (SRP).
 *
 * SRP: A class should have only one reason to change.
 *
 * Violation:
 *  - EmployeeServiceBad mixes business rules (pay calc), persistence (save), and presentation (report).
 *    Any change in tax computation, DB, or report layout forces changes in the same class.
 *
 * Compliant:
 *  - Separate classes per responsibility:
 *      - SalaryCalculator: business rule for pay calculation
 *      - EmployeeRepository: persistence responsibility
 *      - EmployeeReportPrinter: presentation responsibility
 *  - Employee remains a simple data model.
 */
public class SingleResponsibilityDemo {

    public static void main(String[] args) {
        Employee emp = new Employee(101, "Alice", 50.0, 8.0);

        System.out.println("---- SRP Violation Demo ----");
        EmployeeServiceBad bad = new EmployeeServiceBad();
        double payBad = bad.calculatePay(emp);
        bad.saveToDatabase(emp);
        bad.generateReport(emp, payBad);

        System.out.println("\n---- SRP Compliant Demo ----");
        SalaryCalculator calculator = new SalaryCalculator();
        EmployeeRepository repository = new EmployeeRepository();
        EmployeeReportPrinter printer = new EmployeeReportPrinter();

        double pay = calculator.calculatePay(emp);
        repository.save(emp);
        printer.print(emp, pay);
    }
}

// ---------- Domain Model ----------
class Employee {
    private final int id;
    private final String name;
    private final double hourlyRate;
    private final double hoursWorked;

    public Employee(int id, String name, double hourlyRate, double hoursWorked) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (hourlyRate < 0 || hoursWorked < 0) throw new IllegalArgumentException("non-negative values only");
        this.id = id;
        this.name = name;
        this.hourlyRate = hourlyRate;
        this.hoursWorked = hoursWorked;
    }

    public int id() { return id; }
    public String name() { return name; }
    public double hourlyRate() { return hourlyRate; }
    public double hoursWorked() { return hoursWorked; }
}

// ---------- Violating Design (too many responsibilities) ----------
class EmployeeServiceBad {
    // Business rule (pay calculation)
    public double calculatePay(Employee e) {
        // Imagine tax/bonus rules embedded here, making this a frequent change point.
        return e.hourlyRate() * e.hoursWorked();
    }

    // Persistence concern
    public void saveToDatabase(Employee e) {
        // Pretend DB call; mixing DB concerns here
        System.out.println("EmployeeServiceBad: Saving employee " + e.id() + " to database...");
    }

    // Presentation concern
    public void generateReport(Employee e, double pay) {
        // Report formatting mixed in
        System.out.println("EmployeeServiceBad Report -> id=" + e.id() + ", name=" + e.name() + ", pay=" + pay);
    }
}

// ---------- Compliant SRP Design ----------
class SalaryCalculator {
    // Single responsibility: compute pay; if tax logic changes, only this changes.
    public double calculatePay(Employee e) {
        double gross = e.hourlyRate() * e.hoursWorked();
        double tax = gross * 0.10; // example tax rule
        return gross - tax;
    }
}

class EmployeeRepository {
    // Single responsibility: persistence (DB/file/cache). Swapping DB does not affect others.
    public void save(Employee e) {
        // Simulate persistence
        System.out.println("EmployeeRepository: Saved employee " + e.id() + " (" + e.name() + ") to storage.");
    }
}

class EmployeeReportPrinter {
    // Single responsibility: presentation/reporting.
    public void print(Employee e, double netPay) {
        System.out.println("===== EMPLOYEE REPORT =====");
        System.out.println("ID   : " + e.id());
        System.out.println("Name : " + e.name());
        System.out.printf("Net Pay: $%.2f%n", netPay);
        System.out.println("===========================");
    }
}
