package pet;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("=== VIRTUAL PET SIMULATOR ===");
        System.out.println("Pilih Jenis Hewan:");
        System.out.println("1. Cat (Kucing)");
        System.out.println("2. Dog (Anjing)");
        System.out.println("3. Bird (Burung)");
        System.out.print("Masukkan pilihan (1-3): ");
        int pilihan = sc.nextInt();
        
        System.out.print("Beri nama pet kamu: ");
        String nama = sc.next();

        Pet myPet; 
        
        if (pilihan == 1) {
            myPet = new Cat(nama);
        } else if (pilihan == 2) {
            myPet = new Dog(nama);
        } else if (pilihan == 3) {
            myPet = new Bird(nama);
        } else {
            System.out.println("❌ Pilihan salah! Otomatis memilih Cat.");
            myPet = new Cat(nama);
        }

        Food snack = new Treat("Biskuit Kecil");
        Food dinner = new WetFood("Ikan Tuna");

        boolean gameBerjalan = true;
        while (gameBerjalan) {
            myPet.showStatus();
            
            System.out.println("\n--- PILIH AKSI ---");
            System.out.println("1. Beri Makan Snack (" + snack.getName() + ")");
            System.out.println("2. Beri Makan Malam (" + dinner.getName() + ")");
            System.out.println("3. Ajak Bermain (Spesifik per Hewan)");
            System.out.println("4. Dengarkan Suara Hewan (makeSound)");
            System.out.println("5. Tidurkan Pet");
            System.out.println("6. Keluar Simulator");
            System.out.print("Masukkan pilihan (1-6): ");
            int aksi = sc.nextInt();
            
            System.out.println("\n-----------------------------");
            switch (aksi) {
                case 1: myPet.feed(snack); break;
                case 2: myPet.feed(dinner); break;
                case 3: myPet.play(); break; 
                case 4: myPet.makeSound(); break;
                case 5: myPet.sleep(); break;
                case 6:
                    System.out.println("Keluar simulator. Sampai jumpa!");
                    gameBerjalan = false;
                    break;
                default:
                    System.out.println("❌ Pilihan tidak valid!");
            }
            System.out.println("-----------------------------");
        }
        sc.close();
    }
}