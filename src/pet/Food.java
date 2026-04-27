package pet;

public class Food {
    private String name;
    private int nutritionValue;

    public Food(String name, int nutritionValue) {
        this.name = name;
        this.nutritionValue = nutritionValue;
    }

    // Getter untuk akses data makanan
    public String getName() { return name; }
    public int getNutritionValue() { return nutritionValue; }
}