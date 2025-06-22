package PizzaTypes;

public class VegPizza extends BasePizza {

    @Override
    public int cost(){
        System.out.println("VegPizza cost: " + 350);
        return 350;
    }

}
