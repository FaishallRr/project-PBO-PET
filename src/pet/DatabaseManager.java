package pet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pet_simulator";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            ensureTableExists();
        } catch (SQLException e) {
            System.out.println("[DB] Gagal konek: " + e.getMessage());
            try {
                Connection initConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306", DB_USER, DB_PASS);
                initConn.createStatement().execute("CREATE DATABASE IF NOT EXISTS pet_simulator");
                initConn.close();
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                ensureTableExists();
            } catch (SQLException e2) {
                System.out.println("[DB] Gagal buat database: " + e2.getMessage());
            }
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public boolean isConnected() {
        try { return connection != null && !connection.isClosed(); }
        catch (SQLException e) { return false; }
    }

    private void ensureTableExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS pet_save (
                id INT AUTO_INCREMENT PRIMARY KEY,
                pet_name VARCHAR(50) NOT NULL,
                species VARCHAR(20) NOT NULL,
                hunger INT DEFAULT 50,
                happiness INT DEFAULT 50,
                energy INT DEFAULT 50,
                health INT DEFAULT 100,
                level INT DEFAULT 1,
                total_feeds INT DEFAULT 0,
                total_plays INT DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("[DB] Gagal buat tabel: " + e.getMessage());
        }
    }

    public static class PetSaveData {
        public int id;
        public String petName;
        public String species;
        public int hunger, happiness, energy, health, level;
        public int totalFeeds, totalPlays;
    }

    public PetSaveData loadLatestPet() {
        if (!isConnected()) return null;
        String sql = "SELECT * FROM pet_save ORDER BY updated_at DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                PetSaveData data = new PetSaveData();
                data.id = rs.getInt("id");
                data.petName = rs.getString("pet_name");
                data.species = rs.getString("species");
                data.hunger = rs.getInt("hunger");
                data.happiness = rs.getInt("happiness");
                data.energy = rs.getInt("energy");
                data.health = rs.getInt("health");
                data.level = rs.getInt("level");
                data.totalFeeds = rs.getInt("total_feeds");
                data.totalPlays = rs.getInt("total_plays");
                return data;
            }
        } catch (SQLException e) {
            System.out.println("[DB] Gagal load: " + e.getMessage());
        }
        return null;
    }

    public List<String> getSavedPetNames() {
        List<String> names = new ArrayList<>();
        if (!isConnected()) return names;
        String sql = "SELECT DISTINCT pet_name, species FROM pet_save ORDER BY updated_at DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                names.add(rs.getString("pet_name") + " (" + rs.getString("species") + ")");
            }
        } catch (SQLException e) {
            System.out.println("[DB] Gagal list: " + e.getMessage());
        }
        return names;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            System.out.println("[DB] Gagal tutup: " + e.getMessage());
        }
    }
}
