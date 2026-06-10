package pet;

public class Bird extends Pet {
    public Bird(String name) { super(name, 20, 80, 40); }

    @Override
    public void play() {
        if (this.energy <= 15) {
            System.out.println("❌ " + name + " terlalu lemas untuk terbang! Dia butuh tidur.");
            return;
        }
        System.out.println("🦜 " + name + " mengepakkan sayap dan terbang berputar di dalam kamar!");
        setHappiness(this.happiness + 10);
        setEnergy(this.energy - 15);
        timePasses();
    }

    @Override
    public void makeSound() {
        System.out.println("🐦 " + name + " berkicau merdu: Tweet! Chirp!");
    }

    @Override
    protected void timePasses() {
        if (this.hunger >= 80) this.hunger += 20;
        else this.hunger += 5; 
        this.energy -= 15; 
    }
}