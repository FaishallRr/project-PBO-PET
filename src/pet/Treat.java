package pet;

public class Treat extends Food {
    public Treat(String name) { super(name); price = 7; }

    @Override
    public int getHungerReduction() { return 5; }

    @Override
    public int getHappinessBoost() { return 15; }
}
