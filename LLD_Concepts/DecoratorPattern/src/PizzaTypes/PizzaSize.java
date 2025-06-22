package PizzaTypes;

public class PizzaSize {
    // SMALL(8.0),   // Example: diameter in inches
    // MEDIUM(12.0),
    // LARGE(14.0),
    // EXTRA_LARGE(16.0);

    private final double diameterInInches;

    PizzaSize(double diameterInInches) {
        this.diameterInInches = diameterInInches;
    }

    public double getDiameterInInches() {
        return diameterInInches;
    }
}
