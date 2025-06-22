package Additions;

import PizzaTypes.BasePizza;

public class ExtraCheese extends ToppingsDecorator {
    BasePizza basePizza;

    //constructor Injection
    public ExtraCheese(BasePizza basePizza){
        this.basePizza = basePizza;
    }

    @Override
    public int cost(){
        System.out.println("Extra cheese cost : 100");
        return this.basePizza.cost() + 100;
    }


}
