package pet;

public class Pet {

    protected String name;
    protected int hunger, happiness, energy, health;

    public Pet(String name, int hunger, int happiness, int energy) {
        this.name = name;
        this.health = 100;
        setHunger(hunger);
        setHappiness(happiness);
        setEnergy(energy);
    }

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
        if (this.hunger < 20) {
            System.out.println("❌ " + name + " merasa MUAL! Perutnya sudah sangat kenyang (" + this.hunger + "%). Dia menolak makan " + food.getName() + ".");
            return; 
        }
        
        int nutrisiMasuk = food.getNutritionValue();
        int pemulihanHealth = 0;

        if (this.hunger == 100) {
            nutrisiMasuk = nutrisiMasuk * 2; 
            pemulihanHealth = 10; 
            System.out.println("⚠️ " + name + " KELAPARAN EKSTREM (100%)! Dia melahap " + food.getName() + " dengan sangat rakus!");
            System.out.println("💡 Efek Makan Rakus: Kelaparan berkurang drastis (Nutrisi efektif: +" + nutrisiMasuk + "%)");
        } else if (this.hunger < 40) {
            nutrisiMasuk = nutrisiMasuk / 2; 
            if (nutrisiMasuk < 1) nutrisiMasuk = 1;
            pemulihanHealth = nutrisiMasuk / 2;
            if (pemulihanHealth < 1) pemulihanHealth = 1;
            System.out.println(name + " mulai kenyang. Memakan " + food.getName() + " pelan-pelan (Nutrisi efektif: +" + nutrisiMasuk + "%)");
        } else {
            pemulihanHealth = nutrisiMasuk / 2; 
            if (pemulihanHealth < 1) pemulihanHealth = 1;
            System.out.println(name + " makan " + food.getName() + " dengan lahap (Nutrisi: +" + nutrisiMasuk + "%)");
        }

        setHunger(this.hunger - nutrisiMasuk);
        this.health += pemulihanHealth;
        if (this.health > 100) this.health = 100;
        
        System.out.println("❤️ Health " + name + " bertambah +" + pemulihanHealth + " HP.");
        timePasses();
    }

    public void play() {
        // Akan diisi oleh masing-masing hewan
    }

    public void makeSound() {
        System.out.println(name + " mengeluarkan suara.");
    }

    public void sleep() {
        System.out.println(name + " sedang tidur...");
        setEnergy(this.energy + 30);
        timePasses();
    }

        protected void timePasses() {
            this.hunger += 10;
            this.happiness -= 5;
            this.energy -= 5;
        }

    public void showStatus() {
        System.out.println("\n--- STATUS " + name.toUpperCase() + " ---");
        System.out.println("Hunger    : " + hunger + "% " + getBar(hunger)); 
        System.out.println("Happiness : " + happiness + " " + getBar(happiness));
        System.out.println("Energy    : " + energy + " " + getBar(energy));
        System.out.println("Health    : " + health);
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