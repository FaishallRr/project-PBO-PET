package pet;

public class Main {
    public static void main(String[] args) {
        // M1 - Membuat object Pet dengan atribut dasar
        Pet myPet = new Pet("Momo", 50, 50, 50);
        
        // [M2] Membuat object Food 
        Food snack = new Food("Snack Ikan", 20);

        // Demo interaksi
        myPet.showStatus();
        
        myPet.play();
        myPet.feed(snack);
        
        myPet.showStatus();
    }
}