package WithStrategyPattern;

import WithStrategyPattern.Strategy.NormalDrive;

public class PassengerVehicle extends Vehicle {

    public PassengerVehicle(){
        super(new NormalDrive());
    }
}
