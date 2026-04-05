public class NYPizza extends Pizza {

    public NYPizza(PizzaSize size) {
        super(size);
    }

    @Override
    public void prepare() {
        System.out.println("Preparing New York style pizza of size " + getSize());
    }
}
