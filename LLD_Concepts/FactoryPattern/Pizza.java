public abstract class Pizza {

    public enum PizzaSize {
        SMALL,
        MEDIUM,
        LARGE
    }

    private final PizzaSize size;

    public Pizza(PizzaSize size) {
        this.size = size;
    }

    public PizzaSize getSize() {
        return size;
    }

    public abstract void prepare();

    public void bake() {
        System.out.println("Baking " + size + " pizza");
    }

    public void pack() {
        System.out.println("Packing " + size + " pizza");
    }
}
