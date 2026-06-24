package pet;

import java.io.Serializable;
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
            migrateSchema();
        } catch (SQLException e) {
            System.out.println("[DB] Gagal konek: " + e.getMessage());
            try {
                Connection initConn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306", DB_USER, DB_PASS);
                initConn.createStatement().execute("CREATE DATABASE IF NOT EXISTS pet_simulator");
                initConn.close();
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                ensureTableExists();
                migrateSchema();
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
                owner VARCHAR(50) DEFAULT 'Player',
                pet_name VARCHAR(50) NOT NULL,
                species VARCHAR(20) NOT NULL,
                age INT DEFAULT 0,
                coins INT DEFAULT 0,
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

        String[] newTables = {
            "CREATE TABLE IF NOT EXISTS gifts (id INT AUTO_INCREMENT PRIMARY KEY, from_owner VARCHAR(50) NOT NULL, to_owner VARCHAR(50) NOT NULL, to_pet_name VARCHAR(50) NOT NULL, gift_type VARCHAR(30) NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS guestbook (id INT AUTO_INCREMENT PRIMARY KEY, pet_id INT NOT NULL, visitor_owner VARCHAR(50) NOT NULL, message TEXT NOT NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)"
        };
        for (String s : newTables) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(s);
            } catch (SQLException e) {
                System.out.println("[DB] Gagal buat tabel tambahan: " + e.getMessage());
            }
        }
    }

    private void migrateSchema() {
        String[] migrations = {
            "ALTER TABLE pet_save ADD COLUMN owner VARCHAR(50) DEFAULT 'Player'",
            "ALTER TABLE pet_save ADD COLUMN age INT DEFAULT 0",
            "ALTER TABLE pet_save ADD COLUMN coins INT DEFAULT 0",
            "ALTER TABLE pet_save ADD COLUMN dry_food INT DEFAULT 0",
            "ALTER TABLE pet_save ADD COLUMN wet_food INT DEFAULT 0",
            "ALTER TABLE pet_save ADD COLUMN treat INT DEFAULT 0",
            "ALTER TABLE pet_save ADD COLUMN vitamin INT DEFAULT 0"
        };
        for (String sql : migrations) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                System.out.println("[DB] Migrasi: " + sql);
            } catch (SQLException e) {
                // Kolom sudah ada, skip
            }
        }
    }

    public static class PetSaveData implements Serializable {
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

    public List<PetSaveData> getPetsByOwner(String owner) {
        List<PetSaveData> list = new ArrayList<>();
        if (!isConnected()) return list;
        String sql = "SELECT * FROM pet_save WHERE owner = ? ORDER BY updated_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, owner);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readPetData(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("[DB] Gagal list pets: " + e.getMessage());
        }
        return list;
    }

    public List<PetSaveData> getLeaderboard() {
        List<PetSaveData> list = new ArrayList<>();
        if (!isConnected()) return list;
        String sql = "SELECT * FROM pet_save ORDER BY level DESC, coins DESC LIMIT 50";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(readPetData(rs));
            }
        } catch (SQLException e) {
            System.out.println("[DB] Gagal leaderboard: " + e.getMessage());
        }
        return list;
    }

    public PetSaveData loadLatestPet() {
        if (!isConnected()) return null;
        String sql = "SELECT * FROM pet_save ORDER BY updated_at DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return readPetData(rs);
            }
        } catch (SQLException e) {
            System.out.println("[DB] Gagal load: " + e.getMessage());
        }
        return null;
    }

    private PetSaveData readPetData(ResultSet rs) throws SQLException {
        PetSaveData data = new PetSaveData();
        data.id = rs.getInt("id");
        data.owner = rs.getString("owner");
        data.petName = rs.getString("pet_name");
        data.species = rs.getString("species");
        data.age = rs.getInt("age");
        data.coins = rs.getInt("coins");
        data.hunger = rs.getInt("hunger");
        data.happiness = rs.getInt("happiness");
        data.energy = rs.getInt("energy");
        data.health = rs.getInt("health");
        data.level = rs.getInt("level");
        data.totalFeeds = rs.getInt("total_feeds");
        data.totalPlays = rs.getInt("total_plays");
        data.dryFood = rs.getInt("dry_food");
        data.wetFood = rs.getInt("wet_food");
        data.treat = rs.getInt("treat");
        data.vitamin = rs.getInt("vitamin");
        return data;
    }

    public int savePet(PetSaveData data) {
        if (!isConnected()) return -1;
        if (data.id > 0) {
            String sql = "UPDATE pet_save SET owner=?, pet_name=?, species=?, age=?, coins=?, hunger=?, happiness=?, energy=?, health=?, level=?, total_feeds=?, total_plays=?, dry_food=?, wet_food=?, treat=?, vitamin=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, data.owner);
                ps.setString(2, data.petName);
                ps.setString(3, data.species);
                ps.setInt(4, data.age);
                ps.setInt(5, data.coins);
                ps.setInt(6, data.hunger);
                ps.setInt(7, data.happiness);
                ps.setInt(8, data.energy);
                ps.setInt(9, data.health);
                ps.setInt(10, data.level);
                ps.setInt(11, data.totalFeeds);
                ps.setInt(12, data.totalPlays);
                ps.setInt(13, data.dryFood);
                ps.setInt(14, data.wetFood);
                ps.setInt(15, data.treat);
                ps.setInt(16, data.vitamin);
                ps.setInt(17, data.id);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("[DB] Gagal update: " + e.getMessage());
            }
            return data.id;
        } else {
            String sql = "INSERT INTO pet_save (owner, pet_name, species, age, coins, hunger, happiness, energy, health, level, total_feeds, total_plays, dry_food, wet_food, treat, vitamin) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, data.owner);
                ps.setString(2, data.petName);
                ps.setString(3, data.species);
                ps.setInt(4, data.age);
                ps.setInt(5, data.coins);
                ps.setInt(6, data.hunger);
                ps.setInt(7, data.happiness);
                ps.setInt(8, data.energy);
                ps.setInt(9, data.health);
                ps.setInt(10, data.level);
                ps.setInt(11, data.totalFeeds);
                ps.setInt(12, data.totalPlays);
                ps.setInt(13, data.dryFood);
                ps.setInt(14, data.wetFood);
                ps.setInt(15, data.treat);
                ps.setInt(16, data.vitamin);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException e) {
                System.out.println("[DB] Gagal insert: " + e.getMessage());
            }
            return -1;
        }
    }

    public void sendGift(String fromOwner, String toOwner, String toPetName, String giftType) {
        if (!isConnected()) return;
        String sql = "INSERT INTO gifts (from_owner, to_owner, to_pet_name, gift_type) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fromOwner);
            ps.setString(2, toOwner);
            ps.setString(3, toPetName);
            ps.setString(4, giftType);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[DB] Gagal kirim hadiah: " + e.getMessage());
        }
    }

    public boolean canSendGiftToday(String fromOwner, String toOwner, String toPetName) {
        if (!isConnected()) return true;
        String sql = "SELECT COUNT(*) FROM gifts WHERE from_owner=? AND to_owner=? AND to_pet_name=? AND DATE(created_at)=CURDATE()";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fromOwner);
            ps.setString(2, toOwner);
            ps.setString(3, toPetName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.out.println("[DB] Gagal cek hadiah: " + e.getMessage());
        }
        return true;
    }

    public List<String[]> getGuestbook(int petId) {
        List<String[]> list = new ArrayList<>();
        if (!isConnected()) return list;
        String sql = "SELECT visitor_owner, message, created_at FROM guestbook WHERE pet_id=? ORDER BY created_at DESC LIMIT 50";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{rs.getString("visitor_owner"), rs.getString("message"), rs.getString("created_at")});
                }
            }
        } catch (SQLException e) {
            System.out.println("[DB] Gagal load guestbook: " + e.getMessage());
        }
        return list;
    }

    public void addGuestbookEntry(int petId, String visitorOwner, String message) {
        if (!isConnected()) return;
        String sql = "INSERT INTO guestbook (pet_id, visitor_owner, message) VALUES (?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, petId);
            ps.setString(2, visitorOwner);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[DB] Gagal tulis guestbook: " + e.getMessage());
        }
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
