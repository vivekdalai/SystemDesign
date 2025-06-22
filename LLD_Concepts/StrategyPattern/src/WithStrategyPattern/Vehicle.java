package WithStrategyPattern;

import WithStrategyPattern.Strategy.DriveStrategy;

public class Vehicle {

    DriveStrategy drive1;
    Vehicle(DriveStrategy drive)
    {
        this.drive1 = drive;
        System.out.println("Parent Vehicle Class");
        driveVehicle();
    }

    public void driveVehicle(){
        drive1.Drive();
    }

}
