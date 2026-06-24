# Pet Simulator 2D Modern 🐱🐶🐦

Virtual Pet Simulator interaktif — pelihara hewan virtualmu dalam mode 2D!
Dibuat sebagai project PBO (Pemrograman Berorientasi Objek) dengan JavaFX + MySQL.

## ✨ Fitur

- **3 Spesies Unik** — Kucing 🐱, Anjing 🐶, Burung 🐦 — masing-masing behavior berbeda
- **Multi-Pet** — Punya banyak pet sekaligus, ganti dengan navigasi ◀ ▶
- **Sistem Umur** — Baby 👶 → Adult 🐾 → Senior 👴, tampilan berubah sesuai umur
- **Mini Game** — Reaction Clicker 🎮, dapetin koin + happiness
- **Toko & Koin** — Beli makanan/vitamin pake koin 🪙 dari minigame & interaksi
- **Leaderboard** — Ranking pemain global 🏆 lihat siapa yang paling jago
- **Kirim Hadiah** — Kasih snack/vitamin ke pet teman 🎁
- **Guestbook** — Tinggalin pesan di profile pet orang lain 📝
- **Status Real-time** — Lapar, Senang, Energi, Sehat dalam progress bar warna dinamis
- **Animasi Smooth** — Floating indicator stat, speech bubble, bounce, hop, level-up sparkle
- **Persistence** — Auto-save MySQL + file backup (offline fallback)
- **Efek Suara** — 10 suara imut FM-synthesis (meow, bark, chirp, eat, happy, dll)
- **Keyboard Shortcuts** — `1` Makan, `2` Main, `3` Mandi, `4` Vitamin, `5` Tidur
- **Drag Interaksi** — Petting (drag) kasih happiness + hati melayang
- **Level System** — Level naik lewat interaksi, toast + sparkle notification
- **Sick State** — Pet sakit (health ≤0) butuh vitamin; sembuh bisa makan lagi
- **Pet 2D Click** — Klik pet 2D → pet bersuara

## 🎮 Cara Main

| Tombol | Shortcut | Fungsi |
|--------|----------|--------|
| 🍖 **MAKAN** | `1` | Kasi makan (Dry → Wet → Treat, cycle) |
| ⚽ **MAIN** | `2` | Ajak bermain (setiap spesies beda) |
| 🛁 **MANDI** | `3` | Mandikan/grooming pet |
| 💊 **VITAMIN** | `4` | Beri vitamin (satu-satunya action saat sakit) |
| 😴 **TIDUR** | `5` | Tidurkan / bangunkan pet |
| 🔊 **Sound** | — | Nyalakan/matikan suara |
| 💾 **Save** | — | Simpan manual ke database |
| 🎮 **Mini Game** | — | Main reaction clicker, dapetin koin! |
| 🛒 **Shop** | — | Beli makanan & vitamin pakai koin |
| 🏆 **Leaderboard** | — | Lihat ranking pemain global |
| ➕ **Pet Baru** | — | Buat pet baru |
| ◀ ▶ **Navigasi** | — | Ganti-ganti pet |

**Interaksi Lain:**
- **Seret (drag)** pet → kasih happiness + hati 💕
- **Klik** pet 2D → pet bersuara + ekspresi senang

## 🖥️ Requirements

| Komponen | Versi |
|----------|-------|
| Java JDK | 21+ |
| JavaFX SDK | 23+ (gluonhq.com) |
| MySQL | 8.0+ (via Laragon/XAMPP) |
| MySQL Connector/J | 9.x (dev.mysql.com) |

## 📥 Instalasi

### 1. Download Dependencies

- **JavaFX SDK**: https://gluonhq.com/products/javafx/
  - Download versi Windows (x64)
  - Extract ke `D:\openjfx-26.0.1_windows-x64_bin-sdk\javafx-sdk-26.0.1`

- **MySQL Connector/J**: https://dev.mysql.com/downloads/connector/j/
  - Pilih "Platform Independent" → download ZIP
  - Extract, ambil `mysql-connector-j-9.x.x.jar`
  - Taruh di `lib/mysql-connector-j-9.x.x.jar`

### 2. Setup Database

Database dibuat **otomatis** saat pertama jalan — cukup nyalakan MySQL:
```bash
mysql -u root -p
```
Atau jalankan script:
```bash
mysql -u root < init.sql
```

### 3. Edit run.bat

Sesuaikan `run.bat` dengan path JavaFX SDK dan MySQL Connector di komputer kamu:
```
set JAVAFX_PATH=D:\openjfx-26.0.1_windows-x64_bin-sdk\javafx-sdk-26.0.1
set MYSQL_JAR=lib\mysql-connector-j-9.7.0.jar
```

### 4. Sound

**Sudah include!** 10 file `.wav` ada di folder `sound/` — tidak perlu download apa-apa.

## 🚀 Menjalankan

```bash
cd D:\MyProject\project-PBO-PET
run.bat
```

Script akan compile semua file `.java` lalu menjalankan game.

## 📁 Struktur Project

```
project-PBO-PET/
├── src/pet/
│   ├── Main.java              # Entry point
│   ├── GameGUI.java           # GUI utama (JavaFX)
│   ├── Pet.java               # Abstract class Pet
│   ├── Cat.java               # Kucing
│   ├── Dog.java               # Anjing
│   ├── Bird.java              # Burung
│   ├── Pet2D.java             # Karakter 2D (shapes, eye tracking, breathing)
│   ├── Food.java              # Abstract food
│   ├── DryFood.java           # Makanan kering
│   ├── WetFood.java           # Makanan basah
│   ├── Treat.java             # Snack
│   ├── Careable.java          # Interface: groom() + giveVitamin()
│   ├── DatabaseManager.java   # JDBC singleton (auto-create DB/table)
│   ├── SoundManager.java      # AudioClip manager singleton
│   └── FileSaveManager.java   # File backup save/load
├── lib/
│   └── mysql-connector-j-9.x.x.jar
├── styles.css                 # Glassmorphism dark theme
├── init.sql                   # Schema reference
├── run.bat                    # Compile & run script
├── sound/                     # 10 generated WAV files
├── saves/                     # Auto-generated backup folder
└── README.md                  # File ini
```

## 🧠 Konsep OOP

| Konsep | Implementasi |
|--------|-------------|
| **Inheritance** | `Pet` → `Cat`, `Dog`, `Bird` |
| **Polymorphism** | `play()`, `makeSound()`, `timePasses()` di override tiap species |
| **Abstraction** | Abstract class `Pet` + `Food` dengan abstract method |
| **Interface** | `Careable` → diimplement `Cat`, `Dog`, `Bird` |
| **Encapsulation** | Field `protected` + setter validasi |
| **Singleton** | `DatabaseManager`, `SoundManager` |
| **DAO Pattern** | `DatabaseManager` untuk CRUD pet_save |
| **MVC** | Model (`Pet`), View (`GameGUI`), Controller (`GameGUI`) |
| **Polimorfisme Makanan** | `Food` abstract → 3 subclass berbeda efek |
| **Serialization** | `FileSaveManager` untuk backup file (.dat) |

## 🎨 GUI Features

- **Dark Theme** — gradient pastel + glassmorphism panel
- **Dynamic Progress Bars** — warna berubah berdasarkan nilai (≥70 hijau, 30-69 kuning, <30 merah)
- **Smooth Animation** — 250ms Timeline transition untuk semua bar
- **Floating Indicators** — stat change (+5 happiness, -3 hunger) melayang naik + fade
- **Toast Notifications** — max 3, auto-fade, muncul di depan (layer tertinggi)
- **Speech Bubble** — cooldown system, ekspresi pet berubah sesuai mood
- **Keyboard Shortcuts** — `1-5` untuk action buttons + tooltip hover
- **Responsive Layout** — pet 2D centering otomatis saat resize window
- **Feature Buttons** — Shop, Mini Game, Leaderboard, Pet Baru

## 🔄 Simulasi Logic

- **Tick**: setiap 2 detik lewat Timeline game loop
- **Decay**: hunger, happiness, energy turun gradual
- **Health penalty**: hanya ketika hunger ≥90
- **Sleep**: energy regen +8 per 4 tick, wake bonus +10
- **Sick**: health ≤0 → action terbatas → vitamin sembuhkan
- **Food cycle**: Makan → Dry → Wet → Treat → Dry → ...
- **Level**: `1 + (totalFeeds + totalPlays) / 10` (hanya kalau health > 0)
- **Age**: Baby (0-49 ticks) → Adult (50-199) → Senior (200+)
- **Coins**: +1 per feed, +2 per play, reward dari minigame

## 🐾 Species Comparison

| Trait | Kucing | Anjing | Burung |
|-------|--------|--------|--------|
| Lapar/tick (normal) | +2 | +3 | +2 |
| Lapar/tick (≥80%) | +5 | +10 | +5 |
| Energi/tick | -3 | -4 | -5 |
| Senang/tick | -4 | -5 | -4 |
| Energy cost main | -10 | -20 | -15 |
| Senang gain main | +15 | +20 | +10 |
| Min energy main | 10 | 20 | 15 |
| Senang gain grooming | +5 | +15 | +10 |

## 👨‍💻 Credits

Dibuat untuk tugas mata kuliah **PBO (Pemrograman Berorientasi Objek)**
Teknik Informatika — 2026
