package pet;

public class Pet {
    // [M1] Atribut diubah ke PRIVATE agar tidak bisa diakses sembarangan 
    private String name;
    private int hunger;
    private int happiness;
    private int energy;

    // [M2] Atribut tambahan 
    private int health; 

    // [Milestone 1] Constructor: Inisialisasi atribut saat object dibuat
    public Pet(String name, int hunger, int happiness, int energy) {
        this.name = name;
        // [M2] Gunakan setter agar input awal tetap divalidasi 
        setHunger(hunger);
        setHappiness(happiness);
        setEnergy(energy);
        this.health = 100; // Default health
    }

    // [M2] GETTER & SETTER dengan VALIDASI 
    public String getName() { return name; }

    public void setHunger(int hunger) {
        // Validasi: hunger harus di antara 0-100 
        if (hunger < 0) this.hunger = 0;
        else if (hunger > 100) this.hunger = 100;
        else this.hunger = hunger;
        
        // [M2] Jika terlalu lapar (>= 90), health berkurang 
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

    // [M2] Method feed diperbarui menggunakan class Food
    public void feed(Food food) {
        System.out.println(name + " makan " + food.getName());
        setHunger(this.hunger - food.getNutritionValue());
        timePasses(); // [M2] Simulasi waktu berlalu 
    }

    public void play() {
        System.out.println(name + " sedang bermain!");
        setHappiness(this.happiness + 20);
        setEnergy(this.energy - 15);
        timePasses();
    }

    // [M2] Method internal untuk mengubah status setiap kali ada aksi 
    private void timePasses() {
        this.hunger += 10;
        this.happiness -= 5;
        this.energy -= 5;
    }

    public void showStatus() {
        System.out.println("\n--- STATUS " + name.toUpperCase() + " ---");
        // [M2] Tampilan dengan bar visual 
        System.out.println("Hunger    : " + hunger + " " + getBar(hunger));
        System.out.println("Happiness : " + happiness + " " + getBar(happiness));
        System.out.println("Energy    : " + energy + " " + getBar(energy));
        System.out.println("Health    : " + health);
    }

    // Helper untuk membuat bar visual [####------] 
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