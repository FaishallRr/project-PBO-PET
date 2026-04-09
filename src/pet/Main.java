package pet;

public class Main {
    public static void main(String[] args) {
        // Membuat 1 object Pet (Instansiasi) 
        Pet myPet = new Pet("T-Rex Kalimantan", 50, 50, 50);

        // Menampilkan status awal
        myPet.showStatus();

        // Memanggil semua method aksi sesuai instruksi 
        myPet.play();
        myPet.feed();
        myPet.sleep();

        // Menampilkan status akhir
        System.out.println("\nSetelah beraktivitas:");

        // Menampilkan status akhir
        myPet.showStatus();
    }
}