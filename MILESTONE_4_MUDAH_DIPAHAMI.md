# 🎮 MILESTONE 4 - Penjelasan Mudah Dipahami

---

## 🏠 APA ITU MILESTONE 4?

Milestone 4 adalah fitur-fitur yang membuat game pet lebih lengkap:
1. Bisa punya lebih dari 1 pet (ganti-ganti)
2. Pet punya umur (mulai kecil, jadi besar, terus tua)
3. Ada mini-game buat main bareng pet
4. Ada toko buat beli makanan (pakai koin)
5. Data pet disimpan (biar tidak hilang)

---

## 1️⃣ PUNYA BANYAK PET (GANTI-GANTI) 👥

### **Konsep Mudah:**
Bayangkan Anda punya 3 hewan peliharaan di rumah:
- Kucing bernama Mimi
- Anjing bernama Boncel
- Burung bernama Tweety

Nah, Anda bisa pilih mana yang mau diurus hari ini. Saat ganti hewan, **semua data hewan itu tersimpan** (makanan yang dikasih, level, koinnya, dll).

### **Alur Ganti Pet di Game:**

```
User klik tombol ▶ (kanan)
        ↓
Game: "OK, simpan dulu data Mimi"
        ↓
"Sekarang keluarkan Boncel dari penyimpanan"
        ↓
"Baca data Boncel: umur 50, lapar 40, koin 100"
        ↓
"Update layar dengan data Boncel"
        ↓
Boncel muncul di layar 🐕
```

### **Di Kode (Bahasa Kita):**

**Baris 36 - Daftar Pet:**
```java
private List<PetSaveData> petList = new ArrayList<>();
```
Artinya: Kita punya KOTAK (list) yang berisi semua pet player

**Baris 37 - Pet Sekarang Mana?**
```java
private int currentPetIndex = -1;
```
Artinya: Nomor berapa pet yang sekarang aktif (-1 = belum ada pet)

**Baris 952-968 - Ganti Pet (Kode Utama):**
```java
currentPetIndex = index;  // Set pet nomor berapa yang aktif sekarang
PetSaveData data = petList.get(index);  // Ambil data pet itu

// Sesuaikan dengan spesiesnya, buat object pet
if (data.species = "kucing")
    pet = new Cat(data.petName);  // Bikin kucing baru
else if (data.species = "anjing")
    pet = new Dog(data.petName);  // Bikin anjing baru
```

**Baris 970-985 - Restore Semua Data Pet:**
```java
pet.setHunger(data.hunger);       // Set lapar = lapar sebelumnya
pet.setHappiness(data.happiness); // Set senang = senang sebelumnya
pet.setEnergy(data.energy);       // Set energi = energi sebelumnya
// ... dan seterusnya
coins = data.coins;               // Ambil koin yang disimpan
dryFoodStock = data.dryFood;      // Ambil stok makanan yang disimpan
```

Artinya: Tarik semua data dari penyimpanan dan masukkan ke variable game

**Baris 989-992 - Tampilkan Pet:**
```java
updateUI();                          // Update label di layar
build2DPet(data.species);            // Gambar pet di layar
showSpeech("Bertemu lagi! ✨");     // Pet ngomong greeting
```

### **Kapan Ganti Pet Terjadi?**
- User klik tombol ▶ atau ◀
- User bikin pet baru
- Game startup (load pet pertama kali)

---

## 2️⃣ UMUR PET (BABY → DEWASA → TUA) 👶👨👴

### **Konsep Mudah:**
Pet Anda itu seperti manusia:
- **Umur 0-49 hari**: BABY 👶 (kecil dan imut, scale 75%)
- **Umur 50-199 hari**: DEWASA 👨 (normal, scale 100%)
- **Umur 200+ hari**: TUA 👴 (agak mengecil, scale 85%)

Setiap **~100 detik main**, umur pet naik 1 hari.

### **Alur Umur Pet:**

```
Game jalan (game loop tiap 2 detik)
        ↓
Setiap 50 kali loop (≈ 100 detik)
        ↓
age++ (umur naik 1)
        ↓
Cek: Umur berapa?
        ├─ Umur < 50? → Tampilkan "Baby 👶", shrink jadi 75%
        ├─ Umur 50-199? → Tampilkan "Adult 👨", normal 100%
        └─ Umur > 200? → Tampilkan "Senior 👴", shrink jadi 85%
        ↓
Update display + update visual pet
```

### **Di Kode (Bahasa Kita):**

**Baris 39 - Umur Pet:**
```java
private int age = 0;
```
Artinya: Umur mulai dari 0

**Baris 1091 - Naik Umur (Kode Utama):**
```java
if (tickCount % 50 == 0) {  // Setiap 50 kali loop
    age++;                   // Umur naik 1
```

**Baris 1098-1105 - Ubah Ukuran Sesuai Umur:**
```java
double ageScale = 1.0;  // Default ukuran normal (100%)

if (age < 50)
    ageScale = 0.75;    // Umur muda = kecil 75%
else if (age > 200)
    ageScale = 0.85;    // Umur tua = agak kecil 85%

pet2D.setScaleX(ageScale);  // Ubah lebar pet
pet2D.setScaleY(ageScale);  // Ubah tinggi pet
```

**Baris 1010-1017 - Fungsi Baca Umur:**
```java
private String getAgeLabel(int age) {
    if (age < 50)
        return "Baby 👶";       // Umur < 50 = baby
    
    if (age < 200)
        return "Adult 👨";      // Umur 50-199 = adult
    
    return "Senior 👴";         // Umur >= 200 = senior
}
```

### **Tabel Umur Cepat:**

| Umur | Status | Display | Ukuran |
|------|--------|---------|--------|
| 0-49 | Baby | "Baby 👶" | 75% (kecil) |
| 50-199 | Dewasa | "Adult 👨" | 100% (normal) |
| 200+ | Tua | "Senior 👴" | 85% (agak kecil) |

---

## 3️⃣ MINI-GAME (KLIK-KLIKAN) 🎮⚡

### **Konsep Mudah:**
Mini-game itu simple: **Ada tombol yang loncat-loncat, Anda klik sebanyak mungkin dalam 20 detik!**

Makin banyak diklik → tombol loncat makin cepat → reward semakin banyak

**Reward:** Koin + kebahagiaan pet

### **Alur Mini-Game:**

```
User: "Aku mau main mini-game" 🎮
        ↓
Game: OK, loading...
        ↓
[ Layar jadi gelap (overlay), ada tombol di tengah ]
Tulisan: "🎮 Reaction Clicker"
         "Score: 0"
         "Time: 20s"
        ↓
START! Tombol mulai loncat-loncat
        ↓
User klik tombol
        ├─ Score jadi 1
        ├─ Tombol loncat 1000ms sekali
        │
User klik lagi
        ├─ Score jadi 2
        ├─ Tombol loncat 900ms sekali (lebih cepat!)
        │
... terus berulang sampai waktu habis ...
        ↓
Waktu 0 detik → SELESAI!
        ↓
Hitung hadiah:
  Reward = (Score × 3) + 5 koin
  Contoh: Score 10 → (10×3)+5 = 35 koin
  Happiness = +20 (score×2)
        ↓
"Selesai! +35 koin! 🎉"
```

### **Di Kode (Bahasa Kita):**

**Baris 1320-1328 - Start Mini-Game:**
```java
private void showMiniGame() {
    if (pet == null) return;  // Cek ada pet? Tidak ada exit
    
    if (sleeping) {            // Jika pet tidur
        showToast("Bangunkan pet dulu!");  // Kasih peringatan
        return;
    }
    
    beginOverlay(0.7);  // Layar jadi gelap 70%
```

**Baris 1357-1359 - Setup Variable Game:**
```java
int timeLeft = 20;       // Waktu 20 detik
int score = 0;           // Score awal 0
int moveInterval = 1200; // Tombol loncat tiap 1200ms (awal)
```

**Baris 1365-1369 - Tombol Loncat (Timer):**
```java
// Tiap moveInterval ms, tombol pindah ke posisi random

if (moveInterval millis sudah lewat) {
    x = random position (0 - layar lebar)
    y = random position (0 - layar tinggi)
    
    target.setLayoutX(x);  // Pindah ke X
    target.setLayoutY(y);  // Pindah ke Y
    target.show();         // Tampilkan tombol
}
```

**Baris 1404-1416 - User Klik Tombol (Kode Utama):**
```java
target.setOnAction(e -> {
    score++;  // Score naik 1
    
    // Tombol loncat makin cepat (kurangi interval 100ms)
    moveInterval = max(300, moveInterval - 100);
    //  • Awal: 1200ms
    //  • Klik 1x: 1100ms
    //  • Klik 2x: 1000ms
    //  • Min: 300ms (tidak boleh kurang)
    
    // Jalankan ulang timer dengan speed baru
    moveTimer.stop();
    moveTimer = new Timeline(...);
    moveTimer.play();
});
```

**Baris 1372-1386 - Countdown & Game Over:**
```java
// Tiap 1 detik, waktu kurang 1

if (timeLeft <= 0) {  // Waktu habis!
    moveTimer.stop();   // Tombol berhenti loncat
    countdown.stop();   // Countdown berhenti
    
    // Hitung hadiah
    int reward = score * 3 + 5;
    
    pet.addCoins(reward);  // Beri koin ke pet
    
    // Nambah kebahagiaan (max 100)
    pet.setHappiness(min(100, happiness + score*2));
    
    showToast("Selesai! +" + reward + " koin!");
    closeCurrentOverlay();  // Tutup overlay
}
```

### **Rumus Reward:**
- **Koin yang dapat** = (Score × 3) + 5
- **Kebahagiaan** = Score × 2

Contoh: Jika score 15
- Koin = (15 × 3) + 5 = **50 koin** 🤑
- Kebahagiaan = 15 × 2 = **+30** 😊

---

## 4️⃣ TOKO BELANJA (BELI MAKANAN) 🛒💰

### **Konsep Mudah:**
Toko itu seperti toko sungguhan:
1. Ada daftar barang dengan harga
2. Anda cek saldo (punya uang berapa?)
3. Jika uang cukup → beli → stok bertambah
4. Jika uang kurang → tidak bisa beli

**4 Item Toko:**
| Item | Harga | Efek |
|------|-------|------|
| 🍲 Makanan Kering | 5 koin | Lapar -10 |
| 🥫 Makanan Basah | 10 koin | Lapar -25 (lebih ampuh!) |
| 🍰 Snack | 7 koin | Senang +15 |
| 💊 Vitamin | 15 koin | Sehat (obat) |

### **Alur Belanja:**

```
User: "Aku mau beli makanan" 🛒
        ↓
Toko terbuka
        ↓
Tampilkan 4 item dengan harga

User: "Saya mau beli Makanan Kering (5 koin)" → Klik "Beli"
        ↓
Game cek: Saldo cukup?
        ├─ TIDAK (misal saldo cuma 3 koin)
        │  ↓
        │  "Maaf, uang tidak cukup! Butuh 5 koin"
        │  ❌ Pembeli tidak jadi beli
        │
        └─ YA (saldo 10 koin)
           ↓
           "OK, proses pembayaran..."
           ↓
           • Potong saldo: 10 - 5 = 5 koin sisa
           • Tambah stok: dryFood = dryFood + 1
           • Simpan ke database
           ↓
           "Berhasil membeli! ✅"
```

### **Di Kode (Bahasa Kita):**

**Baris 1446-1450 - Buka Toko:**
```java
private void showShop() {
    if (pet == null) return;  // Cek ada pet? Tidak ada exit
    
    beginOverlay(0.55);  // Layar jadi gelap 55%
```

**Baris 1483-1485 - Daftar Item:**
```java
String[] itemNames = {"Makanan Kering", "Makanan Basah", 
                      "Snack", "Vitamin"};

int[] itemPrices = {5, 10, 7, 15};
// Harga: Kering 5, Basah 10, Snack 7, Vitamin 15
```

**Baris 1513-1541 - Proses Beli (Kode Utama):**
```java
buyBtn.setOnAction(e -> {
    // CEK SALDO CUKUP?
    if (pet.getCoins() < itemPrices[idx]) {
        showToast("Koin tidak cukup! Butuh " + itemPrices[idx]);
        return;  // Exit, tidak jadi beli
    }
    
    // SALDO CUKUP → PROSES BELI
    
    // 1. Potong koin dari pet
    pet.addCoins(-itemPrices[idx]);
    // Contoh: pet punya 10 koin, addCoins(-5) → sisa 5 koin
    
    // 2. Tambah stok item yang dibeli
    switch (idx) {
        case 0: dryFoodStock++;    break;   // Item 0 = Kering
        case 1: wetFoodStock++;    break;   // Item 1 = Basah
        case 2: treatStock++;      break;   // Item 2 = Snack
        case 3: vitaminStock++;    break;   // Item 3 = Vitamin
    }
    
    // 3. Update display dan simpan
    updateStatus();              // Refresh tampilan
    showToast("Berhasil membeli " + itemNames[idx] + "! ✅");
    saveToDB();                  // Simpan ke database
});
```

### **Cara Dapat Koin:**
- Kasih makan pet → +1 koin
- Bermain dengan pet → +2 koin
- Selesai mini-game → +(reward) koin

---

## 5️⃣ SIMPAN & MUAT DATA (SAVE/LOAD) 💾

### **Konsep Mudah:**
Bayangkan Anda main game, terus mati/shutdown komputer. Saat hidup lagi:
- **Tanpa Save**: Semua data hilang (pet hilang, koin hilang, level hilang)
- **Dengan Save**: Semua kembali seperti sebelumnya (pet masih ada, koin masih ada)

Nah, game kita **menyimpan data setiap 30 detik secara otomatis** (agar tidak hilang).

### **Apa yang Disimpan?**

Data per pet disimpan dalam **PetSaveData** (kotak data):
```
┌─ PetSaveData ───────────────┐
│ • id: 1                      │
│ • owner: "John"              │
│ • petName: "Mimi"            │
│ • species: "Kucing"          │
│ • age: 75                    │
│ • hunger: 40                 │
│ • happiness: 85              │
│ • energy: 60                 │
│ • health: 100                │
│ • level: 5                   │
│ • coins: 250                 │
│ • dryFood: 5 (stok)          │
│ • wetFood: 3 (stok)          │
│ • treat: 2 (stok)            │
│ • vitamin: 1 (stok)          │
└──────────────────────────────┘
```

### **Alur Save/Load:**

```
┌─ STARTUP (Game baru dibuka) ─────┐
│                                  │
│ 1. Coba load dari Database MySQL │
│    (jika MySQL on)               │
│    ├─ Ada? → Pakai data dari DB  │
│    └─ Tidak ada? → Lanjut...     │
│                                  │
│ 2. Coba load dari file .dat      │
│    (file backup di folder saves) │
│    ├─ Ada? → Pakai data dari file│
│    └─ Tidak ada? → Lanjut...     │
│                                  │
│ 3. Tidak ada sama sekali?        │
│    → Tampilkan form "Buat Pet"   │
└──────────────────────────────────┘
         ↓
    GAME JALAN
         ↓
┌─ SAVE OTOMATIS (tiap 30 detik) ──┐
│ saveToDB() dipanggil              │
│ → Copy semua data pet             │
│ → Simpan ke MySQL (jika on)       │
│ → Simpan ke file .dat (backup)    │
└──────────────────────────────────┘
         ↓
┌─ SAVE MANUAL ────────────────────┐
│ Dipanggil saat:                  │
│ • User klik tombol 💾            │
│ • User tutup game (✖)            │
│ • Selesai kasih makan            │
│ • Selesai bermain                │
│ • Selesai minigame               │
│ • Selesai belanja                │
└──────────────────────────────────┘
```

### **Di Kode (Bahasa Kita):**

**Baris 1-16 - Struktur Data Pet (PetSaveData.java):**
```java
public class PetSaveData implements Serializable {
    public int id;              // ID pet di database
    public String owner;        // Siapa pemiliknya
    public String petName;      // Nama pet
    public String species;      // Jenis (Kucing/Anjing/Burung)
    
    public int age;             // Umur
    public int coins;           // Koin punya
    public int hunger;          // Lapar (0-100)
    public int happiness;       // Senang (0-100)
    public int energy;          // Energi (0-100)
    public int health;          // Kesehatan (0-100)
    public int level;           // Level pet
    
    public int totalFeeds;      // Berapa kali diberi makan (statistik)
    public int totalPlays;      // Berapa kali dimainkan (statistik)
    
    public int dryFood;         // Stok makanan kering
    public int wetFood;         // Stok makanan basah
    public int treat;           // Stok snack
    public int vitamin;         // Stok vitamin
}
```

Ini kotak (class) yang menampung SEMUA data pet dalam 1 paket

**Baris 109-117 - Load Saat Startup (Kode Utama):**
```java
// Coba load dari database
if (db.isConnected()) {           // MySQL nyala?
    petList = db.getPetsByOwner(owner);  // Ambil data dari MySQL
}

// Jika database kosong atau tidak terhubung, load dari file
if (petList.isEmpty()) {
    petList = fileSave.load();    // Ambil dari file saves/pet_save.dat
}

// Jika masih kosong (tidak ada save apapun), buat pet baru
if (petList.isEmpty()) {
    showCreateScreen();  // Tampilkan form create pet
}
```

**Baris 1030-1052 - Simpan Data (Kode Utama):**
```java
private void saveToDB() {
    // 1. BUAT KOTAK DATA BARU
    PetSaveData data = new PetSaveData();
    
    // 2. ISI KOTAK DENGAN DATA SEKARANG
    data.petName = pet.getName();        // Dari pet object
    data.hunger = pet.getHunger();       // Dari pet object
    data.happiness = pet.getHappiness(); // Dari pet object
    data.energy = pet.getEnergy();       // Dari pet object
    data.health = pet.getHealth();       // Dari pet object
    data.age = age;                      // Dari variable global
    data.coins = coins;                  // Dari variable global
    data.level = level;                  // Dari variable global
    
    // Stok inventory
    data.dryFood = dryFoodStock;         // Dari variable global
    data.wetFood = wetFoodStock;         // Dari variable global
    data.treat = treatStock;             // Dari variable global
    data.vitamin = vitaminStock;         // Dari variable global
    
    // 3. SIMPAN KE DATABASE
    if (db.isConnected()) {              // MySQL nyala?
        db.savePet(data);                // Simpan ke MySQL
    }
    
    // 4. SIMPAN KE FILE (BACKUP)
    fileSave.save(petList);              // Simpan ke .dat file
}
```

**Baris 1167-1174 - Auto-Save Tiap 30 Detik:**
```java
saveTimer = new Timeline(
    new KeyFrame(Duration.seconds(30), e -> {
        // Tiap 30 detik, jalankan ini:
        if (pet != null) {
            saveToDB();  // Simpan otomatis
        }
    })
);

saveTimer.setCycleCount(Timeline.INDEFINITE);  // Loop terus
saveTimer.play();  // Jalankan
```

Artinya: Setiap 30 detik, game otomatis mengcopy data pet dan menyimpan

---

## 📊 RINGKASAN SINGKAT (5 Fitur)

| # | Fitur | Apa? | Dipakai? |
|---|-------|------|----------|
| 1 | **Multiple Pets** | Punya banyak hewan, bisa ganti-ganti | Tombol ◀▶ |
| 2 | **Umur** | Pet tumbuh dari baby → dewasa → tua | Otomatis setiap ~100s |
| 3 | **Mini-Game** | Klik tombol yang loncat 20 detik, dapat reward | Tombol 🎮 |
| 4 | **Toko** | Belanja makanan pakai koin | Tombol 🛒 |
| 5 | **Save/Load** | Data disimpan otomatis & manual | Otomatis setiap 30s |

---

## ✅ STATUS FINAL

✔️ **Tidak ada error** (compile berhasil)
✔️ **Game berjalan lancar** (tidak crash)
✔️ **Semua fitur siap** (bisa presentasi)

**Pertanyaan?** Tanya saja! Penjelasan ini mudah dipahami? 😊
