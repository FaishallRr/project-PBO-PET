package pet;

public class Cat extends Pet {
    public Cat(String name) { super(name, 30, 70, 60); }

    @Override
    public void play() {
        if (this.energy <= 10) {
            System.out.println("❌ " + name + " terlalu lemas untuk bermain bola benang! Dia butuh tidur.");
            return;
        }
        System.out.println("🧶 " + name + " bermain bola benang dengan ceria!");
        setHappiness(this.happiness + 15);
        setEnergy(this.energy - 10);
        timePasses();
    }

    @Override
    public void makeSound() {
        System.out.println("🐱 " + name + " bersuara: Meowww~ Nyaaa~");
    }

    @Override
    protected void timePasses() {
        if (this.hunger >= 80) this.hunger += 5; 
        else this.hunger += 2; 
        this.energy -= 2; 
    }
}