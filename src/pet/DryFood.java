package pet;

public class DryFood extends Food {
    public DryFood(String name) { super(name); price = 5; }

    @Override
    public int getHungerReduction() { return 10; }

    @Override
    public int getHappinessBoost() { return 2; }
}
