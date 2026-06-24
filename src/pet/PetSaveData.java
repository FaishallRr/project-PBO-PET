package pet;

import java.io.Serializable;

public class PetSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public String owner;
    public String petName;
    public String species;
    public int age;
    public int coins;
    public int hunger, happiness, energy, health, level;
    public int totalFeeds, totalPlays;
    public int dryFood, wetFood, treat, vitamin;
}
