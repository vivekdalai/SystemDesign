package WithStrategyPattern.Strategy;

public class NormalDrive implements DriveStrategy{

    @Override
    public void Drive() {
        System.out.println("impl of NormalDrive");
    }
}
