package pet;

public abstract class Food {

    protected String name;
    protected int price;

    public Food(String name) {
        this.name = name;
    }

    public String getName() { 
        return name; 
    }
    
    public int getPrice() { return price; }
    
    public abstract int getHungerReduction();
    public abstract int getHappinessBoost();
}
