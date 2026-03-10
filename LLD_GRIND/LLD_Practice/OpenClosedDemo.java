/**
 * Demonstrates the Open/Closed Principle (OCP).
 *
 * OCP: Software entities should be open for extension, but closed for modification.
 *
 * Violation (OrderDiscountCalculatorBad):
 *  - Uses a switch on DiscountType. Adding a new discount requires modifying existing code.
 *
 * Compliant (OrderTotalCalculator + DiscountPolicy):
 *  - Calculator depends on a DiscountPolicy abstraction.
 *  - New discounts are added by creating new DiscountPolicy implementations,
 *    without changing OrderTotalCalculator.
 */
public class OpenClosedDemo {

    public static void main(String[] args) {
        OrderOC order = new OrderOC();
        order.add(new ItemOC("Book", 2, 12.50));
        order.add(new ItemOC("Headphones", 1, 59.99));
        order.add(new ItemOC("Pen", 5, 1.20));

        System.out.println("---- OCP Violation Demo ----");
        double badTotalSeasonal = OrderDiscountCalculatorBad.totalWithDiscount(order, DiscountType.SEASONAL);
        double badTotalLoyalty  = OrderDiscountCalculatorBad.totalWithDiscount(order, DiscountType.LOYALTY);
        System.out.printf("Bad - Seasonal total: $%.2f%n", badTotalSeasonal);
        System.out.printf("Bad - Loyalty  total: $%.2f%n", badTotalLoyalty);

        System.out.println("\n---- OCP Compliant Demo ----");
        OrderTotalCalculator calculator = new OrderTotalCalculator();

        DiscountPolicy seasonal = new SeasonalDiscountPolicy(0.10); // 10% off
        DiscountPolicy loyalty  = new LoyaltyDiscountPolicy(5.00);  // flat $5 off
        DiscountPolicy bulk     = new BulkDiscountPolicy(5, 0.15);  // 15% off when item count >= 5
        DiscountPolicy none     = new NoDiscountPolicy();

        System.out.printf("Compliant - Seasonal total: $%.2f%n", calculator.total(order, seasonal));
        System.out.printf("Compliant - Loyalty  total: $%.2f%n", calculator.total(order, loyalty));
        System.out.printf("Compliant - Bulk    total: $%.2f%n", calculator.total(order, bulk));
        System.out.printf("Compliant - NoDisc  total: $%.2f%n", calculator.total(order, none));

        // To extend: implement DiscountPolicy (e.g., CouponDiscountPolicy) and pass it into calculator.total(order, new CouponDiscountPolicy("SAVE20"))
    }
}

/* ========================= Violation ========================= */

enum DiscountType { NONE, SEASONAL, LOYALTY }

class OrderDiscountCalculatorBad {
    public static double totalWithDiscount(OrderOC order, DiscountType type) {
        double subtotal = order.subtotal();
        switch (type) {
            case NONE:
                return subtotal;
            case SEASONAL:
                return subtotal * 0.90; // 10% off
            case LOYALTY:
                return Math.max(0.0, subtotal - 5.0); // flat $5 off
            // Adding a new discount requires MODIFYING this method (violation of OCP)
            default:
                throw new IllegalArgumentException("Unknown discount type: " + type);
        }
    }
}

/* ========================= Compliant ========================= */

interface DiscountPolicy {
    double apply(double subtotal, int totalItems);
}

class OrderTotalCalculator {
    public double total(OrderOC order, DiscountPolicy policy) {
        double subtotal = order.subtotal();
        int items = order.totalItems();
        double discounted = policy.apply(subtotal, items);
        return roundMoney(discounted);
    }

    private double roundMoney(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}

class NoDiscountPolicy implements DiscountPolicy {
    @Override
    public double apply(double subtotal, int totalItems) {
        return subtotal;
    }
}

class SeasonalDiscountPolicy implements DiscountPolicy {
    private final double percentOff; // 0.10 = 10%

    public SeasonalDiscountPolicy(double percentOff) {
        if (percentOff < 0 || percentOff > 1) throw new IllegalArgumentException("percentOff must be between 0 and 1");
        this.percentOff = percentOff;
    }

    @Override
    public double apply(double subtotal, int totalItems) {
        return subtotal * (1.0 - percentOff);
    }
}

class LoyaltyDiscountPolicy implements DiscountPolicy {
    private final double flatOff;

    public LoyaltyDiscountPolicy(double flatOff) {
        if (flatOff < 0) throw new IllegalArgumentException("flatOff must be non-negative");
        this.flatOff = flatOff;
    }

    @Override
    public double apply(double subtotal, int totalItems) {
        return Math.max(0.0, subtotal - flatOff);
    }
}

class BulkDiscountPolicy implements DiscountPolicy {
    private final int thresholdItems;
    private final double percentOff;

    public BulkDiscountPolicy(int thresholdItems, double percentOff) {
        if (thresholdItems < 1) throw new IllegalArgumentException("thresholdItems must be >= 1");
        if (percentOff < 0 || percentOff > 1) throw new IllegalArgumentException("percentOff must be between 0 and 1");
        this.thresholdItems = thresholdItems;
        this.percentOff = percentOff;
    }

    @Override
    public double apply(double subtotal, int totalItems) {
        if (totalItems >= thresholdItems) {
            return subtotal * (1.0 - percentOff);
        }
        return subtotal;
    }
}

/* ========================= Simple domain for demo ========================= */
class ItemOC {
    private final String name;
    private final int quantity;
    private final double unitPrice;

    public ItemOC(String name, int quantity, double unitPrice) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        if (unitPrice < 0) throw new IllegalArgumentException("unitPrice must be non-negative");
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int quantity() { return quantity; }
    public double lineTotal() { return quantity * unitPrice; }
}

class OrderOC {
    private java.util.List<ItemOC> items = new java.util.ArrayList<>();

    public void add(ItemOC item) {
        if (item == null) throw new IllegalArgumentException("item cannot be null");
        items.add(item);
    }

    public double subtotal() {
        double sum = 0.0;
        for (ItemOC i : items) sum += i.lineTotal();
        return sum;
    }

    public int totalItems() {
        int sum = 0;
        for (ItemOC i : items) sum += i.quantity();
        return sum;
    }
}
