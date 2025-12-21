/**
 * Minimal Factory Pattern example: create Admin, Moderator, and User
 * 
 * Basically, using a switch case determine what object is to be created
 * If using 'new' keyword regularly consider using the factory pattern.
 */
public final class UserFactory {
    private UserFactory() {
        // prevent instantiation
    }

    // Simple factory method
    public static User create(Role role, String name) {
        switch (role) {
            case ADMIN:
                return new Admin(name);
            case MODERATOR:
                return new Moderator(name);
            case CLIENT:
                return new Client(name);
            default:
                throw new IllegalArgumentException("Unknown role: " + role + " for : " + name);
        }
    }

    public static void main(String[] args) {
        User admin = UserFactory.create(Role.ADMIN, "Alice");
        User moderator = UserFactory.create(Role.MODERATOR, "Bob");
        User user = UserFactory.create(Role.CLIENT, "Carol");
        // User superUser = UserFactory.create(Role.SUPERUSER, "Vivek");
        User admin2 = UserFactory.create(Role.ADMIN, "Vivek");

        System.out.println(admin);
        System.out.println(moderator);
        System.out.println(user);
        // System.out.println(superUser);
        System.out.println(admin2);
    }
}

// Simple role indicator
enum Role { ADMIN, MODERATOR, CLIENT, SUPERUSER}

// Product interface
interface User {
    String name();
    Role role();
}

// Admin configuration
final class Admin implements User {
    private final String name;
    
    Admin(String name) { this.name = name; }
    
    public String name() { return name; }
    
    public Role role() { return Role.ADMIN; }
    
    @Override 
    public String toString() { return "Admin{name='" + name + "'}"; }
}

//Moderator configuration
final class Moderator implements User {
    private final String name;
    
    Moderator(String name) { this.name = name; }
    
    public String name() { return name; }
    
    public Role role() { return Role.MODERATOR; }
    
    @Override 
    public String toString() { return "Moderator{name='" + name + "'}"; }
}


//Client configuration
final class Client implements User {
    private final String name;
    
    Client(String name) { this.name = name; }
    
    public String name() { return name; }
    
    public Role role() { return Role.CLIENT; }
    
    @Override 
    public String toString() { return "User{name='" + name + "'}"; }
}
