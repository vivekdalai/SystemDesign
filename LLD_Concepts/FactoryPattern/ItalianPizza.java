public class ItalianPizza extends Pizza {

    public ItalianPizza(PizzaSize size) {
        super(size);
    }

    @Override
    public void prepare() {
        System.out.println("Preparing Italian style pizza of size " + getSize());
    }
}
