package WithoutStrategyPattern;

public class OffRoadVehicle extends Vehicle{

    public OffRoadVehicle(){
        Drive();
    }

    @Override
    public void Drive(){
        System.out.println("SportsUtility in OffRoadVehicle");
        System.out.println("Normal Drive in OffRoadVehicle");
    }
}
