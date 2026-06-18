package pet;

public class Bird extends Pet implements Careable {
    public Bird(String name) { super(name, 20, 80, 40); }

    @Override
    public void play() {
        if (this.energy <= 15) {
            System.out.println("? " + name + " terlalu lemas untuk terbang! Dia butuh tidur.");
            return;
        }
        System.out.println("?? " + name + " mengepakkan sayap dan terbang berputar di dalam kamar!");
        setHappiness(this.happiness + 10);
        setEnergy(this.energy - 15);
    }

    @Override
    public void makeSound() {
        System.out.println("?? " + name + " berkicau merdu: Tweet! Chirp!");
    }

    @Override
    public String getSpecies() {
        return "Burung";
    }

    @Override
    public void groom() {
        System.out.println("?? " + name + " dimandikan! Disemprot air lembut, lalu dijemur.");
        setHappiness(this.happiness + 10);
    }

    @Override
    public void giveVitamin() {
        System.out.println("?? " + name + " dikasih vitamin. Sayapnya jadi lebih kuat!");
        setHealth(this.health + 15);
    }

    @Override
    protected void timePasses() {
        if (this.hunger >= 80) {
            setHunger(this.hunger + 5);
        } else {
            setHunger(this.hunger + 2);
        }
        setEnergy(this.energy - 5);
        setHappiness(this.happiness - 4);
        applyHungerHealthPenalty();
    }
}
