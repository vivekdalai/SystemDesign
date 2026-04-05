public class ItalianPizzaFactory extends PizzaFactory {

    @Override
    public Pizza createPizza(Pizza.PizzaSize size) {
        return new ItalianPizza(size);
    }
}
