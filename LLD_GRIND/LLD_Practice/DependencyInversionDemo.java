/**
 * Demonstrates the Dependency Inversion Principle (DIP).
 *
 * High-level modules should not depend on low-level modules; both should depend on abstractions.
 * Abstractions should not depend on details; details should depend on abstractions.
 *
 * Example:
 *  - High-level: NotificationService (business logic)
 *  - Abstraction: MessageSender (interface)
 *  - Low-level: EmailSender, SmsSender, WhatsAppSender (implementations)
 *
 * NotificationService depends only on MessageSender (the abstraction) and receives it via constructor
 * injection. New senders can be added without modifying NotificationService.
 */
public class DependencyInversionDemo {
    public static void main(String[] args) {
        // Inject an EmailSender (low-level) via the abstraction
        MessageSender email = new EmailSender();
        NotificationService service = new NotificationService(email);
        service.notifyUser("alice@example.com", "Welcome to the platform!");

        // Swap to SMS without changing NotificationService
        service = new NotificationService(new SmsSender());
        service.notifyUser("+1-555-123-4567", "Your OTP is 123456");

        // Extend with a new sender without touching NotificationService
        service = new NotificationService(new WhatsAppSender());
        service.notifyUser("+1-555-987-6543", "Your order has been shipped.");
    }
}

// Abstraction (the stable contract)
interface MessageSender {
    void send(String to, String message);
}

// High-level module depends on the abstraction, not concrete classes
class NotificationService {
    private final MessageSender sender;

    public NotificationService(MessageSender sender) {
        if (sender == null) throw new IllegalArgumentException("sender cannot be null");
        this.sender = sender;
    }

    public void notifyUser(String to, String message) {
        // Business rules could live here (validation, templates, logging, etc.)
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("recipient (to) cannot be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message cannot be blank");
        }
        sender.send(to, "[NOTIFY] " + message);
    }
}

// Low-level details depend on the abstraction
class EmailSender implements MessageSender {
    @Override
    public void send(String to, String message) {
        System.out.println("EmailSender -> To: " + to + " | Body: " + message);
    }
}

class SmsSender implements MessageSender {
    @Override
    public void send(String to, String message) {
        System.out.println("SmsSender   -> To: " + to + " | Body: " + message);
    }
}

class WhatsAppSender implements MessageSender {
    @Override
    public void send(String to, String message) {
        System.out.println("WhatsAppSender -> To: " + to + " | Body: " + message);
    }
}
