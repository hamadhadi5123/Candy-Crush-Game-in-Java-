# 🍭 Candy Crush Saga (JavaFX & MySQL OOP Project)

An interactive, feature-rich **Candy Crush Saga** desktop game built using **Object-Oriented Programming (OOP)** principles in **JavaFX** with a **MySQL / MariaDB** backend.

---

## ✨ Features & Highlights

1. **🎨 AAA Candy Crush Visuals & Themed Screens:**
   - Authentic HD Candy Crush backgrounds across all screens (Login, Signup, Main Menu, Instructions, Game Board, Game Over/Win).
   - Dedicated dark chocolate board container tray with 64 rounded pocket tiles in a checkerboard pattern.
   - 3D-shaded candy gems that sit cleanly inside individual pockets.
   - Vector-rendered glowing selection frame: Gold when hovering, Electric Neon Cyan (`#00FFFF`) when selected.

2. **🎮 Dual Control Support (Mouse + Keyboard):**
   - **Mouse Controls:** Click on any candy to select it, then click an adjacent candy to swap!
   - **Keyboard Arrow Keys:** Use `[UP]`, `[DOWN]`, `[LEFT]`, `[RIGHT]` arrow keys to move the cursor, and `[ENTER]` or `[SPACE]` to select and swap.

3. **⏸️ In-Game Pause & Resume System:**
   - Press **`P`** or **`ESCAPE`** anytime during gameplay (or click the HUD **Pause** button).
   - Frosted glass pause menu with options to Resume, Restart level, Toggle Audio, or return to the Main Menu.

4. **🔊 Built-In Sound Synthesizer Engine (`SoundManager.java`):**
   - Pure, zero-dependency audio synthesis using Java's standard `javax.sound.sampled` API.
   - Distinct procedural sound effects:
     - UI button clicks
     - Candy selection pop
     - Candy swap whoosh
     - Candy match/crush magical chime
     - Multi-match combo cascade
     - Victory fanfare & Game Over tone
     - Looping cheerful ambient background music (BGM)
   - One-click mute/unmute toggle (`🔊` / `🔇`) in HUD and menus.

5. **📊 Real-Time In-Game HUD:**
   - **Current Score:** Big vibrant green score display.
   - **Target Goal & Progress Bar:** Dynamic progress bar filling up towards the level goal!
   - **High Score:** Persistent record tracking.
   - **Animated Countdown Timer:** 120-second timer turning alert-red in the final 15 seconds.

6. **🗄️ Database & Offline Resilience:**
   - Direct connection to **MySQL / MariaDB** (`lab9.login`) for authentication and score tracking.
   - **Offline Guest Fallback:** Seamlessly play offline as guest if the database server is not running.

---

## 🛠️ Project Structure & Architecture

* **`Candy01.java`**: Core JavaFX application, scene management, board matrix mechanics, and animation timers.
* **`SoundManager.java`**: Custom audio engine synthesizing procedural WAV waveforms and handling BGM loop.
* **`DriverShim.java`**: JDBC driver decorator ensuring modular classloader compatibility.
* **`DatabaseManager.java`**: DAO handling database queries, prepared statements, and fallbacks.
* **`Main.java`**: Application entry point.

---

## 🚀 How to Run

### Option 1: Direct Double-Click (Easiest)
Double-click **`run_game.bat`** in the project folder.

### Option 2: In Visual Studio Code
1. Open the project folder in VS Code.
2. Press **`F5`** (or select **"Candy Crush"** in the Run & Debug tab and click Play).

---

## 🔑 Login Credentials

* **Username:** `hamad514`
* **Password:** `Hamad@512`
*(Or click **"Guest Play"** for instant offline play)*
