package pet;

public class WetFood extends Food {
    public WetFood(String name) { super(name); price = 10; }

    @Override
    public int getHungerReduction() { return 25; }

    @Override
    public int getHappinessBoost() { return 8; }
}
