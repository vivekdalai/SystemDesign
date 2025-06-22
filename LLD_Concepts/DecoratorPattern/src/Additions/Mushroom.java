package Additions;

import PizzaTypes.BasePizza;

public class Mushroom  extends ToppingsDecorator{
    BasePizza basePizza;

    public Mushroom(BasePizza basePizza){
        this.basePizza = basePizza;
    }

    @Override
    public int cost(){
        System.out.println("Mushroom cost: " + 150);
        return this.basePizza.cost() + 150;
    }


}
