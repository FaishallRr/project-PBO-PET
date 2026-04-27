package pet;

public class Main {
    public static void main(String[] args) {
        Pet myPet = new Pet("T-Rex Kalimantan", 50, 50, 50);
        
        Food snack = new Food("Snack Daging", 20);

        myPet.showStatus();

        myPet.play();
        myPet.feed(snack); 
        myPet.sleep();

        System.out.println("\nSetelah rangkaian aktivitas:");
        myPet.showStatus();
    }
}