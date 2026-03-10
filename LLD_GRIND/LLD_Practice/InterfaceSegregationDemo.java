/**
 * Demonstrates the Interface Segregation Principle (ISP).
 *
 * Definition:
 *  - Clients should not be forced to depend on methods they do not use.
 *  - Prefer many small, role-focused interfaces over a single "fat" interface.
 *
 * Domain example:
 *  - A Waiter should not be forced to implement cookFood() or decideMenu().
 *  - Chef cooks, Manager plans menu, Waiter serves customers.
 *
 * This file contains:
 *  1) A violating design with a fat interface (EmployeeBad).
 *  2) A compliant design with segregated role interfaces (Server, Cook, MenuPlanner).
 */
public class InterfaceSegregationDemo {

    public static void main(String[] args) {
        // Violation demo
        System.out.println("---- ISP Violation Demo ----");
        EmployeeBad waiterBad = new WaiterBad();
        serveCustomerBad(waiterBad);
        try {
            cookFoodBad(waiterBad); // Throws at runtime: Waiter shouldn't cook
        } catch (UnsupportedOperationException ex) {
            System.out.println("waiterBad.cookFood -> " + ex.getMessage());
        }
        try {
            decideMenuBad(waiterBad); // Throws at runtime: Waiter shouldn't decide menu
        } catch (UnsupportedOperationException ex) {
            System.out.println("waiterBad.decideMenu -> " + ex.getMessage());
        }

        EmployeeBad chefBad = new ChefBad();
        cookFoodBad(chefBad); // OK
        decideMenuBad(chefBad); // Chef may suggest, but often not responsible for menu planning

        // Compliant demo
        System.out.println("\n---- ISP Compliant Demo ----");
        Server waiter = new Waiter();
        Cook chef = new Chef();
        MenuPlanner manager = new Manager();
        HeadChef headChef = new HeadChef(); // multi-role example

        serveCustomer(waiter);
        cookFood(chef);
        decideMenu(manager);

        // Multi-role: head chef can both cook and plan menu
        cookFood(headChef);
        decideMenu(headChef);

        // The following lines won't even compile (which is good),
        // preventing misuses at compile time instead of runtime:
        // cookFood(waiter);        // Waiter is not a Cook
        // decideMenu(waiter);      // Waiter is not a MenuPlanner
    }

    // --- Client code for violating design (accepts fat interface) ---
    static void serveCustomerBad(EmployeeBad e) { e.serveCustomers(); }
    static void cookFoodBad(EmployeeBad e) { e.cookFood(); }
    static void decideMenuBad(EmployeeBad e) { e.decideMenu(); }

    // --- Client code for compliant design (accepts minimal interfaces) ---
    static void serveCustomer(Server s) { s.serveCustomers(); }
    static void cookFood(Cook c) { c.cookFood(); }
    static void decideMenu(MenuPlanner m) { m.decideMenu(); }
}

// ================= Violating design =================
interface EmployeeBad {
    void serveCustomers();
    void cookFood();
    void decideMenu();
}

class WaiterBad implements EmployeeBad {
    @Override
    public void serveCustomers() {
        System.out.println("WaiterBad: Serving food and taking orders.");
    }
    @Override
    public void cookFood() {
        throw new UnsupportedOperationException("Waiter should not cook food.");
    }
    @Override
    public void decideMenu() {
        throw new UnsupportedOperationException("Waiter should not decide the menu.");
    }
}

class ChefBad implements EmployeeBad {
    @Override
    public void serveCustomers() {
        System.out.println("ChefBad: Occasionally greets customers.");
    }
    @Override
    public void cookFood() {
        System.out.println("ChefBad: Cooking dishes.");
    }
    @Override
    public void decideMenu() {
        System.out.println("ChefBad: Suggesting specials (but not solely responsible).");
    }
}

// ================= Compliant design =================
interface Server {
    void serveCustomers();
}

interface Cook {
    void cookFood();
}

interface MenuPlanner {
    void decideMenu();
}

class Waiter implements Server {
    @Override
    public void serveCustomers() {
        System.out.println("Waiter: Serving food, taking orders, clearing tables.");
    }
}

class Chef implements Cook {
    @Override
    public void cookFood() {
        System.out.println("Chef: Prepping, cooking, plating dishes.");
    }
}

class Manager implements MenuPlanner {
    @Override
    public void decideMenu() {
        System.out.println("Manager: Planning menu with cost, seasonality, and customer feedback.");
    }
}

// Multi-role example: a head chef cooks and also participates in menu planning.
class HeadChef implements Cook, MenuPlanner {
    @Override
    public void cookFood() {
        System.out.println("HeadChef: Executing dishes and overseeing kitchen.");
    }
    @Override
    public void decideMenu() {
        System.out.println("HeadChef: Designing seasonal menu with the manager.");
    }
}
