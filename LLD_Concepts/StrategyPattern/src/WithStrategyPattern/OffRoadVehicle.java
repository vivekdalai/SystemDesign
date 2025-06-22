package WithStrategyPattern;

import WithStrategyPattern.Strategy.NormalDrive;
import WithStrategyPattern.Strategy.SportDrive;

public class OffRoadVehicle extends Vehicle {

    OffRoadVehicle(){
        super(new SportDrive());
    }
}
