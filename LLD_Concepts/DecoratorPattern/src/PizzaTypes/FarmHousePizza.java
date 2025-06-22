package PizzaTypes;

public class FarmHousePizza extends BasePizza{

    @Override
    public int cost(){
        System.out.println("Farmhouse Pizza cost :" + 500);
        return 500;
    }
}
