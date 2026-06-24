package pet;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileSaveManager {
    private static final String SAVE_DIR = "saves";
    private static final String SAVE_FILE = SAVE_DIR + "/pet_save.dat";

    public FileSaveManager() {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            System.out.println("[FileSave] Gagal buat folder saves: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<PetSaveData> load() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (List<PetSaveData>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[FileSave] Gagal load file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void save(List<PetSaveData> pets) {
        if (pets == null || pets.isEmpty()) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(pets);
        } catch (IOException e) {
            System.out.println("[FileSave] Gagal simpan file: " + e.getMessage());
        }
    }
}
