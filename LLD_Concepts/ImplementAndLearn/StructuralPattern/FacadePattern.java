/**
 * FacadePattern demo
 * The ECommerceFacade hides complex interactions among subsystems:
 * FraudChecker, InventoryManager, PaymentProcess, ShippingHandler.
 */
public class FacadePattern {
    public static void main(String[] args) {
        // Without Facade example: client coordinates all subsystems
        WithoutFacadePattern without = new WithoutFacadePattern();
        String result1 = without.processPayment("user", "item");
        System.out.println("Without Facade: " + result1);

        // With Facade example: simplified single call
        ECommerceFacade facade = new ECommerceFacade();

        //Just provide a simple endpoint to perform task.
        //complex actions are handled within -- abstracted here!
        String result2 = facade.placeOrder("user", "item");
        System.out.println("With Facade: " + result2);
    }
}

/**
 * Facade that provides a simple API to place an order.
 */
class ECommerceFacade {
    private final PaymentProcess paymentProcess = new PaymentProcess();
    private final InventoryManager inventoryManager = new InventoryManager();
    private final ShippingHandler shippingHandler = new ShippingHandler();
    private final FraudChecker fraudChecker = new FraudChecker();

    public String placeOrder(String user, String item) {
        if (!fraudChecker.verifyUser(user)) {
            return "Error: user verification failed";
        }
        if (!inventoryManager.processInventory(item)) {
            return "Error: inventory processing failed";
        }
        if (!paymentProcess.paymentHandler(user, item)) {
            return "Error: payment failed";
        }
        String shippingDetails = shippingHandler.shipProduct(user, item);
        return "processed -> " + shippingDetails;
    }
}

class WithoutFacadePattern {
    public String processPayment(String user, String item) {
        PaymentProcess paymentProcess = new PaymentProcess();
        InventoryManager inventoryManager = new InventoryManager();
        ShippingHandler shippingHandler = new ShippingHandler();
        FraudChecker fraudChecker = new FraudChecker();

        if (fraudChecker.verifyUser(user)) {
            if (inventoryManager.processInventory(item)) {
                if (paymentProcess.paymentHandler(user, item)) {
                    String shipping = shippingHandler.shipProduct(user, item);
                    if (shipping != null && !shipping.isEmpty()) {
                        return "processed -> " + shipping;
                    }
                }
            }
        }
        return "Error processing item";
    }
}

 /**
 * Subsystems (complex internals hidden behind the Facade)
 */
class PaymentProcess {
    // payment processor
    boolean paymentHandler(String user, String product) {
        // handle payment
        return true;
    }
}

class InventoryManager {
    // inventory manager
    boolean processInventory(String item) {
        // process inventory
        return true;
    }
}

class ShippingHandler {
    // shipping handler
    String shipProduct(String user, String product) {
        // process shipping and produce tracking/shipping details
        return "shipping-details: AWB123";
    }
}

class FraudChecker {
    // fraud checker
    boolean verifyUser(String user) {
        // verify user identity/risk
        return true;
    }
}
