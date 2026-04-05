public class Main {

    public static void main(String[] args) {
        PizzaFactory nyPizzaFactory = new NYPizzaFactory();
        Pizza nyPizza = nyPizzaFactory.createPizza(Pizza.PizzaSize.MEDIUM);
        nyPizza.prepare();
        nyPizza.bake();
        nyPizza.pack();

        PizzaFactory italianPizzaFactory = new ItalianPizzaFactory();
        Pizza italianPizza = italianPizzaFactory.createPizza(Pizza.PizzaSize.LARGE);
        italianPizza.prepare();
        italianPizza.bake();
        italianPizza.pack();
    }
}
