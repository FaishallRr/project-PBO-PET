package pet;

public class Main {
    public static void main(String[] args) {
        Pet myPet = new Pet("Momo", 50, 50, 50);
        Food snack = new Food("Snack Ikan", 20);

        myPet.showStatus();
        
        myPet.play();
        myPet.feed(snack); // Memberi makan menggunakan objek Food
        
        myPet.showStatus();
    }
}