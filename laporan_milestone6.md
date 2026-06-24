# LAPORAN MILESTONE 6 — PET SIMULATOR 2D MODERN
## PBO (Pemrograman Berorientasi Objek)

---

### 1. PENDAHULUAN

**Pet Simulator 2D Modern** adalah aplikasi simulasi hewan peliharaan virtual berbasis JavaFX dengan tampilan 2D modern (glassmorphism UI). Aplikasi ini menerapkan konsep OOP secara penuh dan terintegrasi dengan database MySQL untuk menyimpan data pemain, fitur inter-user (leaderboard, gift, guestbook), serta fitur file save/load sebagai fallback.

---

### 2. KONSEP OOP YANG DIGUNAKAN

#### 2.1 Inheritance (Pewarisan)

```
Pet (abstract class)
├── Cat
├── Dog
└── Bird

Food (abstract class)
├── DryFood
├── WetFood
└── Treat
```

- `Pet` adalah class abstrak yang menjadi parent dari Cat, Dog, Bird. Seluruh child mewarisi field `name`, `hunger`, `happiness`, `energy`, `health`, `age`, `coins` serta method `feed()`, `sleep()`, `timePasses()`.
- `Food` adalah class abstrak parent dari DryFood, WetFood, Treat. Masing-masing child mengimplementasikan `getHungerReduction()` dan `getHappinessBoost()` dengan nilai berbeda.

#### 2.2 Polymorphism (Polimorfisme)

- Method `play()` dideklarasikan abstract di Pet dan diimplementasikan berbeda oleh tiap child:
  - `Cat.play()` → bermain bola benang (+15 happiness, -10 energy)
  - `Dog.play()` → bermain fetch (+20 happiness, -20 energy)
  - `Bird.play()` → terbang (+10 happiness, -15 energy)
- Method `makeSound()` juga polimorfik: Kucing "Meow", Anjing "Woof", Burung "Tweet"
- Method `timePasses()` dioverride di setiap child dengan formula degrasi berbeda sesuai karakter hewan.

#### 2.3 Encapsulation (Enkapsulasi)

- Semua field di Pet menggunakan `protected` atau class dengan getter/setter (`getHunger()`, `setHunger()`, `getHappiness()`, `setHappiness()`, dll.)
- Setter menggunakan `clamp()` untuk memastikan nilai tetap di rentang 0–100.
- `DatabaseManager` menggunakan pattern Singleton dengan constructor private.
- `coins` dan `age` hanya bisa diakses melalui getter (`getCoins()`, `getAge()`) dan diubah melalui setter (`setCoins()`, `setAge()`, `addCoins()`).

#### 2.4 Abstraction (Abstraksi)

- `Pet` adalah abstract class — tidak bisa diinstansiasi langsung. Memiliki 3 method abstrak: `play()`, `makeSound()`, `getSpecies()`.
- `Food` adalah abstract class dengan 2 method abstrak: `getHungerReduction()`, `getHappinessBoost()`.
- `Careable` adalah interface dengan method `groom()` dan `giveVitamin()`, diimplementasikan oleh Cat, Dog, Bird.

---

### 3. CLASS DIAGRAM

```
┌─────────────────────────────────────┐
│          <<abstract>>               │
│               Pet                    │
├─────────────────────────────────────┤
│ # name : String                     │
│ # hunger : int                      │
│ # happiness : int                   │
│ # energy : int                      │
│ # health : int                      │
│ # age : int                         │
│ # coins : int                       │
├─────────────────────────────────────┤
│ + Pet(name, hunger, happy, energy)  │
│ + feed(Food) : void                 │
│ + sleep() : void                    │
│ + <<abstract>> play() : void        │
│ + <<abstract>> makeSound() : void   │
│ + <<abstract>> getSpecies() : String│
│ + getters/setters                   │
└──────────┬──────────────────────────┘
           │ extends
     ┌─────┼─────────────────────┐
     │     │                     │
┌────▼────┐ ┌────▼────┐ ┌────▼────┐
│   Cat   │ │   Dog   │ │  Bird   │
├─────────┤ ├─────────┤ ├─────────┤
│ + play()│ │ + play()│ │ + play()│
│ + makeS.│ │ + makeS.│ │ + makeS.│
│ + groom │ │ + groom │ │ + groom │
│ + vitam.│ │ + vitam.│ │ + vitam.│
└─────────┘ └─────────┘ └─────────┘
     implements        implements        implements
     ┌───────────────────────────────────────┐
     │           <<interface>>               │
     │              Careable                 │
     ├───────────────────────────────────────┤
     │ + groom() : void                      │
     │ + giveVitamin() : void                │
     └───────────────────────────────────────┘

┌─────────────────────────────────────┐
│          <<abstract>>               │
│               Food                   │
├─────────────────────────────────────┤
│ # name : String                     │
│ # price : int                       │
├─────────────────────────────────────┤
│ + <<abstract>> getHungerReduction() │
│ + <<abstract>> getHappinessBoost()  │
└──────────┬──────────────────────────┘
     ┌─────┼─────────────────────┐
     │     │                     │
┌────▼────┐ ┌────▼────┐ ┌────▼────┐
│ DryFood │ │ WetFood │ │  Treat  │
│ price=5 │ │price=10 │ │ price=7 │
│ red.=10  │ │ red.=25 │ │ red.=5  │
│ boost=2 │ │ boost=8 │ │boost=15 │
└─────────┘ └─────────┘ └─────────┘

┌─────────────────────────────────────┐
│         GameGUI (Main UI)           │
├─────────────────────────────────────┤
│ - pet : Pet                         │
│ - pet2D : Pet2D                     │
│ - petList : List<PetSaveData>       │
│ - db : DatabaseManager              │
│ - fileSave : FileSaveManager        │
│ - sound : SoundManager              │
│ - owner, age, level, coins          │
├─────────────────────────────────────┤
│ + createPet(name, species)          │
│ + switchPet(direction)              │
│ + feedPet(), playPet(), bathPet()   │
│ + showMinigame()                    │
│ + showShop()                        │
│ + showLeaderboard()                 │
│ + sendGift(), showGuestbook()       │
│ + saveToDB(), auto-save timer       │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│      DatabaseManager (Singleton)    │
├─────────────────────────────────────┤
│ - connection : Connection           │
├─────────────────────────────────────┤
│ + getPetsByOwner()                  │
│ + getLeaderboard()                  │
│ + savePet()                         │
│ + sendGift() / canSendGiftToday()   │
│ + getGuestbook() / addGuestbook()   │
│ + PetSaveData (inner class)         │
└─────────────────────────────────────┘
```

---

### 4. FITUR-FITUR YANG DIIMPLEMENTASIKAN (Project B)

| No | Fitur | Keterangan |
|----|-------|------------|
| 1 | **Multiple Pets** | Pemain bisa memiliki banyak hewan dan berpindah antar hewan dengan tombol ◀ ▶ |
| 2 | **Age System** | 3 fase: Baby (0–49 tick, scale 0.75), Adult (50–199), Senior (200+, scale 0.85) |
| 3 | **Mini Game** | Reaction Clicker: klik target selama 20 detik, reward koin berdasarkan score |
| 4 | **Shop & Currency** | 4 item: DryFood (5), WetFood (10), Treat (7), Vitamin (15). Koin dari bermain & minigame |
| 5 | **Save/Load File** | Java serialization ke `saves/pet_save.dat` sebagai fallback jika MySQL offline |

#### Fitur Inter-User (Tambahan)

| Fitur | Keterangan |
|-------|------------|
| **Leaderboard** | Ranking pemain berdasarkan level + koin (top 50) |
| **Send Gift** | Kirim snack (+5 happiness) atau vitamin (+5 health) ke pemain lain (1x/hari) |
| **Guestbook** | Tulis pesan di halaman pengunjung setiap hewan |

---

### 5. STRUKTUR DATABASE (MySQL)

**Tabel `pet_save`**: id, owner, pet_name, species, age, coins, hunger, happiness, energy, health, level, total_feeds, total_plays, created_at, updated_at

**Tabel `gifts`**: id, from_owner, to_owner, to_pet_name, gift_type, created_at

**Tabel `guestbook`**: id, pet_id, visitor_owner, message, created_at

---

### 6. PENGUJIAN & DEMO

**Lingkungan:**
- JDK 26 (JavaFX 26)
- MySQL 8+ (root@localhost:3306, no password)
- OS: Windows

**Skenario Demo:**
1. Jalankan `run.bat` (build + run otomatis)
2. Login dengan nama owner
3. Buat pet baru (pilih nama + species)
4. Interaksi: beri makan, ajak bermain, mandi, kasih vitamin
5. Mainkan mini game Reaction Clicker
6. Belanja di Shop dengan koin
7. Lihat Leaderboard → Kirim Gift ke pemain lain → Tulis Guestbook
8. Tutup game → data tersimpan otomatis ke MySQL + file backup

---

### 7. KESIMPULAN

Pet Simulator 2D Modern berhasil mengimplementasikan seluruh konsep OOP (Inheritance, Polymorphism, Encapsulation, Abstraction) serta fitur Project B (multiple pets, age system, mini game, shop & currency, save/load file) ditambah fitur inter-user (leaderboard, gift, guestbook). Aplikasi dapat dikompilasi dengan 0 error dan siap untuk demo.
