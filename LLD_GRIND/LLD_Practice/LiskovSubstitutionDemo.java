/**
 * Demonstrates the Liskov Substitution Principle (LSP).
 *
 * Definition:
 *  - Objects of a superclass should be replaceable with objects of its subclasses
 *    without breaking the correctness of the program.
 *
 * Violation Example (RectangleBad/SquareBad):
 *  - SquareBad extends RectangleBad and overrides setters to enforce equal sides.
 *  - Clients that rely on the rectangle's width/height being independently settable
 *    (e.g., verifyArea) get incorrect results when a SquareBad is substituted.
 *
 * Compliant Example (Shape, Rectangle, Square):
 *  - Both Rectangle and Square implement a common Shape interface, without a
 *    Rectangle -> Square inheritance relationship.
 *  - Clients depend on the stable contract: area(), so substitution holds.
 */
public class LiskovSubstitutionDemo {

    public static void main(String[] args) {
        // LSP violation demo
        System.out.println("---- LSP Violation Demo ----");
        RectangleBad rect = new RectangleBad();
        verifyArea(rect, 5, 4); // OK: area = 20

        SquareBad sq = new SquareBad();
        verifyArea(sq, 5, 4); // Violates expectations: area != 20 due to enforced equal sides

        // LSP compliant design demo
        System.out.println("\n---- LSP Compliant Demo ----");
        Shape r = new Rectangle(5, 4); // area = 20
        Shape s = new Square(5);       // area = 25

        printArea(r); // Substitution holds: client relies only on area() contract
        printArea(s);
    }

    // Client code expecting independent width/height setters to work as specified.
    static void verifyArea(RectangleBad r, int width, int height) {
        r.setWidth(width);
        r.setHeight(height);
        int expected = width * height;
        int actual = r.getArea();
        System.out.println(r.getClass().getSimpleName() + " -> expected area: " + expected + ", actual: " + actual);
    }

    static void printArea(Shape shape) {
        System.out.println(shape.getClass().getSimpleName() + " area = " + shape.area());
    }
}

// ------ Violating design (for demonstration) ------
class RectangleBad {
    protected int width;
    protected int height;

    public void setWidth(int w) { this.width = w; }
    public void setHeight(int h) { this.height = h; }
    public int getArea() { return width * height; }
}

class SquareBad extends RectangleBad {
    @Override
    public void setWidth(int w) {
        this.width = w;
        this.height = w; // Enforce square invariant
    }

    @Override
    public void setHeight(int h) {
        this.height = h;
        this.width = h; // Enforce square invariant
    }
}

// ------ Compliant design ------
interface Shape {
    double area();
}

class Rectangle implements Shape {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        if (width < 0 || height < 0) throw new IllegalArgumentException("Dimensions must be non-negative");
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}

class Square implements Shape {
    private final double side;

    public Square(double side) {
        if (side < 0) throw new IllegalArgumentException("Side must be non-negative");
        this.side = side;
    }

    @Override
    public double area() {
        return side * side;
    }
}
