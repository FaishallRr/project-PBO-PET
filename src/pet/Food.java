package pet;

public class Food {

    protected String name;
    protected int nutritionValue;

    public Food(String name, int nutritionValue) {
        this.name = name;
        this.nutritionValue = nutritionValue;
    }
    public String getName() { return name; }
    public int getNutritionValue() { return nutritionValue; }
}

// Subclasses Food
class DryFood extends Food { public DryFood(String name) { super(name, 10); } }
class WetFood extends Food { public WetFood(String name) { super(name, 25); } }
class Treat extends Food   { public Treat(String name)   { super(name, 5); } }