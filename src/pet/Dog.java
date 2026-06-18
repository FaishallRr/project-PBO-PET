package pet;

public class Dog extends Pet implements Careable {
    public Dog(String name) { super(name, 50, 50, 80); }

    @Override
    public void play() {
        if (this.energy <= 20) {
            System.out.println("? " + name + " terlalu lemas untuk bermain fetch! Dia butuh tidur.");
            return;
        }
        System.out.println("?? " + name + " berlari kencang mengambil bola (Bermain Fetch)!");
        setHappiness(this.happiness + 20);
        setEnergy(this.energy - 20);
    }

    @Override
    public void makeSound() {
        System.out.println("?? " + name + " menggonggong: Woof! Woof! Guk!");
    }

    @Override
    public String getSpecies() {
        return "Anjing";
    }

    @Override
    public void groom() {
        System.out.println("?? " + name + " dimandikan di Pet Shop! Disabun, digosok, dibilas, dan dikeringkan pakai mesin hair dryer.");
        setHappiness(this.happiness + 15);
    }

    @Override
    public void giveVitamin() {
        System.out.println("?? " + name + " dikasih vitamin. Energi dan kesehatannya bertambah!");
        setHealth(this.health + 15);
    }

    @Override
    protected void timePasses() {
        if (this.hunger >= 80) {
            setHunger(this.hunger + 10);
        } else {
            setHunger(this.hunger + 3);
        }
        setHappiness(this.happiness - 6);
        setEnergy(this.energy - 4);
        applyHungerHealthPenalty();
    }
}
