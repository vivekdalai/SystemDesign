package WithoutStrategyPattern;

public class SportsVehicle extends Vehicle{

    public SportsVehicle(){
        Drive();
    }

    @Override
    public void Drive(){
        System.out.println("SportsUtility in SportsVehicle");
    }
}
