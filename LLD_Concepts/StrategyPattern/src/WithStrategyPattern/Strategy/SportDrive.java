package WithStrategyPattern.Strategy;

public class SportDrive implements DriveStrategy{

    @Override
    public void Drive() {
        System.out.println("Impl SportsDrive");
    }
}
