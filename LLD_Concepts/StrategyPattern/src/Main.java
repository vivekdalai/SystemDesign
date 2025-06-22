import WithStrategyPattern.PassengerVehicle;
import WithStrategyPattern.Vehicle;
import WithoutStrategyPattern.OffRoadVehicle;
import WithoutStrategyPattern.SportsVehicle;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        WithoutStrategyPattern.SportsVehicle vehicle1 = new SportsVehicle();
        WithoutStrategyPattern.OffRoadVehicle vehicle2 = new OffRoadVehicle();

        WithStrategyPattern.Vehicle vehicle3 = new PassengerVehicle();
        WithStrategyPattern.Vehicle vehicle4 = new WithStrategyPattern.SportsVehicle();


    }
}