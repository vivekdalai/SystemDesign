public class NYPizzaFactory extends PizzaFactory {

    @Override
    public Pizza createPizza(Pizza.PizzaSize size) {
        return new NYPizza(size);
    }
}
