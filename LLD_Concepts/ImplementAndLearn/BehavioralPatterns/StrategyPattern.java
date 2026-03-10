/**
 * Strategy Pattern Example — Payments with Card, UPI, Cash, Coupons
 *
 * Goal:
 * - Show "without strategy": PaymentProcessorWithoutStrategy uses conditionals to apply different payment logic.
 * - Show "with strategy": PaymentStrategy encapsulates each payment option's logic; PaymentContext delegates to it.
 *
 * This file has no package declaration to avoid package mismatch errors and keep it runnable as-is.
 */
public class StrategyPattern {
    public static void main(String[] args) {
        double amount = 1000.00;

        System.out.println("=== WITHOUT STRATEGY ===");
        PaymentProcessorWithoutStrategy legacy = new PaymentProcessorWithoutStrategy();
        System.out.println(legacy.pay(amount, Method.CARD));
        System.out.println(legacy.pay(amount, Method.UPI));
        System.out.println(legacy.pay(amount, Method.CASH));
        System.out.println(legacy.pay(amount, Method.COUPON));

        System.out.println("\n=== WITH STRATEGY ===");
        PaymentContext ctx = new PaymentContext();

        ctx.setStrategy(new CardPayment("4111111111111111"));
        System.out.println(ctx.pay(amount));

        ctx.setStrategy(new UpiPayment("user@upi"));
        System.out.println(ctx.pay(amount));

        ctx.setStrategy(new CashPayment());
        System.out.println(ctx.pay(amount));

        ctx.setStrategy(new CouponPayment("WELCOME10"));
        System.out.println(ctx.pay(amount));
    }
}

/* =========================================================
   WITHOUT STRATEGY (conditionals in one class)
   ========================================================= */
enum Method { CARD, UPI, CASH, COUPON }

class PaymentProcessorWithoutStrategy {
    public Receipt pay(double amount, Method method) {
        switch (method) {
            case CARD:
                // Example: 2% fee
                double cardFee = amount * 0.02;
                double cardNet = amount + cardFee;
                return Receipt.of("CARD", amount, cardFee, 0, cardNet, "Charged via card (2% fee)");
            case UPI:
                // Example: 0.5% fee, min 2.00
                double upiFee = Math.max(2.0, amount * 0.005);
                double upiNet = amount + upiFee;
                return Receipt.of("UPI", amount, upiFee, 0, upiNet, "Paid via UPI (0.5% fee, min 2.00)");
            case CASH:
                // No fee
                return Receipt.of("CASH", amount, 0, 0, amount, "Paid in cash (no fee)");
            case COUPON:
                // Example: 10% discount capped at 200
                double discount = Math.min(200.0, amount * 0.10);
                double net = Math.max(0.0, amount - discount);
                return Receipt.of("COUPON", amount, 0, discount, net, "Applied coupon (10% up to 200)");
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
    }
}

/* =========================================================
   WITH STRATEGY (behaviors encapsulated and swappable)
   ========================================================= */
class Receipt {
    private final String method;
    private final double gross;
    private final double fee;
    private final double discount;
    private final double net;
    private final String message;

    private Receipt(String method, double gross, double fee, double discount, double net, String message) {
        this.method = method;
        this.gross = gross;
        this.fee = fee;
        this.discount = discount;
        this.net = net;
        this.message = message;
    }

    public static Receipt of(String method, double gross, double fee, double discount, double net, String message) {
        return new Receipt(method, gross, fee, discount, net, message);
    }

    @Override
    public String toString() {
        return String.format("[%s] gross=%s, fee=%s, discount=%s, net=%s | %s",
                method, Money.fmt(gross), Money.fmt(fee), Money.fmt(discount), Money.fmt(net), message);
    }
}

interface PaymentStrategy {
    //produce a 'Receipt' and implement 'pay' method.
    Receipt pay(double amount);
}

class PaymentContext {
    private PaymentStrategy strategy;

    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public Receipt pay(double amount) {
        if (strategy == null) {
            throw new IllegalStateException("PaymentStrategy not set");
        }
        return strategy.pay(amount);
    }
}

/* ---------------- Strategies ---------------- */

class CardPayment implements PaymentStrategy {
    private final String cardNumberMasked;

    public CardPayment(String cardNumber) {
        this.cardNumberMasked = mask(cardNumber);
    }

    private String mask(String card) {
        if (card == null || card.length() < 4) return "****";
        String last4 = card.substring(card.length() - 4);
        return "**** **** **** " + last4;
    }

    @Override
    public Receipt pay(double amount) {
        double fee = amount * 0.02; // 2%
        double net = amount + fee;
        return Receipt.of("CARD", amount, fee, 0, net, "Charged to " + cardNumberMasked);
    }
}

class UpiPayment implements PaymentStrategy {
    private final String upiId;

    public UpiPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public Receipt pay(double amount) {
        double fee = Math.max(2.0, amount * 0.005); // 0.5% with min 2.00
        double net = amount + fee;
        return Receipt.of("UPI", amount, fee, 0, net, "Paid via UPI: " + upiId);
    }
}

class CashPayment implements PaymentStrategy {
    @Override
    public Receipt pay(double amount) {
        return Receipt.of("CASH", amount, 0, 0, amount, "Collected cash (no fee)");
    }
}

class CouponPayment implements PaymentStrategy {
    private final String couponCode;

    public CouponPayment(String couponCode) {
        this.couponCode = couponCode;
    }

    @Override
    public Receipt pay(double amount) {
        // Example: 10% discount capped at 200; invalid/empty codes give no discount
        boolean valid = couponCode != null && !couponCode.trim().isEmpty();
        double discount = valid ? Math.min(200.0, amount * 0.10) : 0.0;
        double net = Math.max(0.0, amount - discount);
        String msg = valid
                ? ("Coupon " + couponCode + " applied")
                : "No valid coupon applied";
        return Receipt.of("COUPON", amount, 0, discount, net, msg);
    }
}

/* ---------------- Helpers ---------------- */

class Money {
    public static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
