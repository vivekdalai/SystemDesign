package WithStrategyPattern;

import WithStrategyPattern.Strategy.DriveStrategy;
import WithStrategyPattern.Strategy.SportDrive;

public class SportsVehicle extends Vehicle {

    public SportsVehicle() {
        super(new SportDrive());
    }
}
