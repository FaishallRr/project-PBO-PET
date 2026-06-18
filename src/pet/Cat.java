package pet;

public class Cat extends Pet implements Careable {
    public Cat(String name) { super(name, 30, 70, 60); }

    @Override
    public void play() {
        if (this.energy <= 10) {
            System.out.println("? " + name + " terlalu lemas untuk bermain bola benang! Dia butuh tidur.");
            return;
        }
        System.out.println("?? " + name + " bermain bola benang dengan ceria!");
        setHappiness(this.happiness + 15);
        setEnergy(this.energy - 10);
    }

    @Override
    public void makeSound() {
        System.out.println("?? " + name + " bersuara: Meowww~ Nyaaa~");
    }

    @Override
    public String getSpecies() {
        return "Kucing";
    }

    @Override
    public void groom() {
        System.out.println("?? " + name + " dimandikan di Pet Shop! Bulunya disabun, dibilas, dan dikeringkan dengan hair dryer.");
        setHappiness(this.happiness + 5);
    }

    @Override
    public void giveVitamin() {
        System.out.println("?? " + name + " dikasih vitamin. Bulunya jadi lebih sehat dan berkilau!");
        this.health = Math.min(100, this.health + 15);
    }

    @Override
    protected void timePasses() {
        if (this.hunger >= 80) {
            setHunger(this.hunger + 5);
        } else {
            setHunger(this.hunger + 2);
        }
        setEnergy(this.energy - 3);
        setHappiness(this.happiness - 4);
    }
}
