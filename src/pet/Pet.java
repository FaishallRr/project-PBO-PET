package pet;

public class Pet {
    private String name;
    private int hunger;
    private int happiness;
    private int energy;
    private int health; 

    public Pet(String name, int hunger, int happiness, int energy) {
        this.name = name;
      
        setHunger(hunger);
        setHappiness(happiness);
        setEnergy(energy);
        this.health = 100;
    }

    public String getName() { return name; }

    public void setHunger(int hunger) {
        if (hunger < 0) this.hunger = 0;
        else if (hunger > 100) this.hunger = 100;
        else this.hunger = hunger;
        
        if (this.hunger >= 90) this.health -= 10;
    }

    public void setHappiness(int happiness) {
        if (happiness < 0) this.happiness = 0;
        else if (happiness > 100) this.happiness = 100;
        else this.happiness = happiness;
    }

    public void setEnergy(int energy) {
        if (energy < 0) this.energy = 0;
        else if (energy > 100) this.energy = 100;
        else this.energy = energy;
    }

    public void feed(Food food) {
        System.out.println(name + " makan " + food.getName());
        setHunger(this.hunger - food.getNutritionValue());
        timePasses();
    }

    public void play() {
        System.out.println(name + " sedang bermain!");
        setHappiness(this.happiness + 20);
        setEnergy(this.energy - 15);
        timePasses();
    }

    public void sleep() {
        System.out.println(name + " sedang tidur...");
        setEnergy(this.energy + 30);
        timePasses();
    }

    private void timePasses() {
        this.hunger += 10;
        this.happiness -= 5;
        this.energy -= 5;
    }

    public void showStatus() {
        System.out.println("\n--- STATUS " + name.toUpperCase() + " ---");
        System.out.println("Hunger    : " + hunger + " " + getBar(hunger));
        System.out.println("Happiness : " + happiness + " " + getBar(happiness));
        System.out.println("Energy    : " + energy + " " + getBar(energy));
        System.out.println("Health    : " + health);
    }

    private String getBar(int value) {
        int dots = value / 10;
        String bar = "[";
        for (int i = 0; i < 10; i++) {
            if (i < dots) bar += "#";
            else bar += "-";
        }
        return bar + "]";
    }
}   