package pet;

public class Dog extends Pet {
    public Dog(String name) { super(name, 50, 50, 80); }

    @Override
    public void play() {
        if (this.energy <= 20) {
            System.out.println("❌ " + name + " terlalu lemas untuk bermain fetch! Dia butuh tidur.");
            return;
        }
        System.out.println("🥎 " + name + " berlari kencang mengambil bola (Bermain Fetch)!");
        setHappiness(this.happiness + 20);
        setEnergy(this.energy - 20);
        timePasses();
    }

    @Override
    public void makeSound() {
        System.out.println("🐶 " + name + " menggonggong: Woof! Woof! Guk!");
    }

    @Override
    protected void timePasses() {
        if (this.hunger >= 80) this.hunger += 10;
        else this.hunger += 3; 
        this.happiness -= 15; 
    }
}