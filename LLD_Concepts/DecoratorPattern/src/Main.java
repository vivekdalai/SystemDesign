import Additions.ExtraCheese;
import Additions.Mushroom;
import Additions.ToppingsDecorator;
import PizzaTypes.BasePizza;
import PizzaTypes.FarmHousePizza;
import PizzaTypes.VegPizza;

public class Main {
    public static void main(String[] args) {
        System.out.println("Pizza LLD");

        //Farmhouse Pizza
        FarmHousePizza basePizza = new FarmHousePizza();

        System.out.println("FarmHousePizza : " + basePizza.cost());

        ToppingsDecorator extraCheesePizza = new ExtraCheese(basePizza);

        System.out.println("With extra cheese : " + extraCheesePizza.cost());

        ToppingsDecorator doubleCheese = new ExtraCheese(extraCheesePizza);
        System.out.println("Double Cheese : " + doubleCheese.cost());


        //VegPizza with Mushroom and extraCheese

        System.out.println("\n\n **********Processing VegPizza with Mushroom and extraCheese******** \n\n");

        ToppingsDecorator mushroomWithCheese = new Mushroom(new ExtraCheese(new VegPizza()));
        System.out.println("VegPizza + Mushroom + extraCheese : " + mushroomWithCheese.cost());

    }
}