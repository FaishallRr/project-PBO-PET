package pet;

public class Pet {
    // [Milestone 1] membuat Atribut
    public String name;
    public int hunger;
    public int happiness;
    public int energy;

    // [Milestone 1] Constructor: inisialisasi 
    public Pet(String name, int hunger, int happiness, int energy) {
        this.name = name;
        this.hunger = hunger;
        this.happiness = happiness;
        this.energy = energy;
    }

    // [Milestone 1] Method feed: Mengurangi rasa lapar
    public void feed() {
        hunger -= 10;
        System.out.println(name + " sedang makan...");
    }

    // [Milestone 1] Method play: Menambah kebahagiaan, mengurangi energi
    public void play() {
        happiness += 20;
        energy -= 15;
        System.out.println(name + " sedang bermain!");
    }

    // [Milestone 1] Method sleep: Menambah energi
    public void sleep() {
        energy += 30;
        System.out.println(name + " sedang tidur...");
    }

    // [Milestone 1] Method showStatus: Menampilkan data ke layar
    public void showStatus() {
        System.out.println("Status " + name + ":");
        System.out.println("- Hunger: " + hunger);
        System.out.println("- Happiness: " + happiness);
        System.out.println("- Energy: " + energy);
        System.out.println("-------------------------");
    }
}