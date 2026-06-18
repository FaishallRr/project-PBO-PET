package pet;

public abstract class Pet {

    protected String name;
    protected int hunger, happiness, energy, health;

    public Pet(String name, int hunger, int happiness, int energy) {
        this.name = name;
        this.health = 100;
        this.hunger = clamp(hunger, 0, 100);
        this.happiness = clamp(happiness, 0, 100);
        this.energy = clamp(energy, 0, 100);
        if (this.hunger >= 90) this.health = Math.max(0, this.health - 10);
    }

    protected int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public void setHunger(int hunger) {
        this.hunger = clamp(hunger, 0, 100);
    }

    private void applyHungerHealthPenalty() {
        if (this.hunger >= 90) {
            this.health = Math.max(0, this.health - 10);
        }
    }

    public void setHappiness(int happiness) {
        this.happiness = clamp(happiness, 0, 100);
    }

    public void setEnergy(int energy) {
        this.energy = clamp(energy, 0, 100);
    }

    public void setHealth(int health) {
        this.health = clamp(health, 0, 100);
    }

    public void feed(Food food) {
        if (this.hunger < 20) {
            System.out.println("? " + name + " merasa MUAL! Perutnya sudah sangat kenyang (" + this.hunger + "%). Dia menolak makan " + food.getName() + ".");
            return; 
        }
        
        int nutrisiMasuk = food.getHungerReduction();
        int bonusHappy = food.getHappinessBoost();
        int pemulihanHealth = 0;

        if (this.hunger == 100) {
            nutrisiMasuk = nutrisiMasuk * 2; 
            pemulihanHealth = 10; 
            System.out.println("?? " + name + " KELAPARAN EKSTREM (100%)! Dia melahap " + food.getName() + " dengan sangat rakus!");
            System.out.println("?? Efek Makan Rakus: Kelaparan berkurang drastis (Nutrisi efektif: +" + nutrisiMasuk + "%)");
        } else if (this.hunger < 40) {
            pemulihanHealth = 1;
            System.out.println(name + " mulai kenyang. Memakan " + food.getName() + " pelan-pelan");
        } else {
            pemulihanHealth = nutrisiMasuk / 2; 
            if (pemulihanHealth < 1) pemulihanHealth = 1;
            System.out.println(name + " makan " + food.getName() + " dengan lahap (Nutrisi: +" + nutrisiMasuk + "%)");
        }

        setHunger(this.hunger - nutrisiMasuk);
        setHappiness(this.happiness + bonusHappy);
        
        if (this.health <= 0) {
            this.health = Math.min(100, pemulihanHealth + 5);
            System.out.println("?? " + name + " mulai pulih berkat makanan! (HP +" + (pemulihanHealth + 5) + ")");
        } else {
            this.health = Math.min(100, this.health + pemulihanHealth);
        }
        
        System.out.println("?? Health " + name + " bertambah +" + pemulihanHealth + " HP.");
        if (bonusHappy > 0) {
            System.out.println("? " + name + " menyukai rasa makanannya! (Happiness +" + bonusHappy + ")");
        }
    }

    public abstract void play();
    public abstract void makeSound();
    public abstract String getSpecies();

    public void sleep() {
        System.out.println(name + " sedang tidur...");
    }

    protected void timePasses() {
        int newHunger = clamp(this.hunger + 10, 0, 100);
        int newHappiness = clamp(this.happiness - 5, 0, 100);
        int newEnergy = clamp(this.energy - 5, 0, 100);
        this.hunger = newHunger;
        this.happiness = newHappiness;
        this.energy = newEnergy;
        applyHungerHealthPenalty();
    }

    public String getName() { return name; }
    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public int getEnergy() { return energy; }
    public int getHealth() { return health; }

    public void showStatus() {
        System.out.println("\n--- STATUS " + name.toUpperCase() + " (" + getSpecies() + ") ---");
        System.out.println("Hunger    : " + hunger + "% " + getBar(hunger)); 
        System.out.println("Happiness : " + happiness + " " + getBar(happiness));
        System.out.println("Energy    : " + energy + " " + getBar(energy));
        System.out.println("Health    : " + health + " HP");
    }

    private String getBar(int value) {
        int dots = value / 10;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            if (i < dots) bar.append("#");
            else bar.append("-");
        }
        return bar.append("]").toString();
    }
}
