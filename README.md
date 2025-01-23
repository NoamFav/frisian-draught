# 🌟 Frisian Draughts Game 🌟

Welcome to **Frisian Draughts**, an exciting and modern take on the classic board game, built with **JavaFX** for a sleek and interactive experience. Featuring custom UI elements, strategic move tracking, dynamic sound effects, and intuitive gameplay – Frisian Draughts brings the traditional game to life like never before!

---

## 🛠️ Prerequisites

Ensure you have the following installed to run Frisian Draughts smoothly:

- **Java 22 or later**  
- **Maven** ([Installation Guide](https://maven.apache.org/install.html))  
- **JavaFX 22**, already included as a dependency in `pom.xml`  

---

## 🌱 Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/NoamFav/frisian-draught.git
   cd frisian-draught
   ```

2. **Run the game:**
   ```bash
   mvn clean javafx:run
   ```

---

## 🎉 Running the Game

### 💻 Using Jpackage (Recommended for Standalone Use)

Easily package the game for your OS and enjoy Frisian Draughts as a standalone application.

**For Windows:**
```bash
mvn clean package -Djpackage.type=exe
```

**For macOS:**
```bash
mvn clean package -Djpackage.type=dmg
```

**For Linux:**
```bash
mvn clean package -Djpackage.type=deb
mvn clean package -Djpackage.type=rpm
```

The game will be installed in your system's Applications folder, and save files will be stored under `FrisianDraughtsExports` in your home directory.

---

### 📚 Running in an IDE

You can also run the game from your favorite IDE (IntelliJ IDEA, Eclipse, etc.) by executing the `Launcher` class.

---

## 📺 Game Features

- **Intuitive UI:**
  - Elegant board design with responsive move tracking.
  - Easy-to-navigate menus and fluid animations.
  - Light and dark mode themes.

- **Dynamic Sound Effects:**
  - Enjoy immersive audio feedback for every move.
  - Background music with adjustable volume settings.

- **Advanced AI:**
  - Play against different AI difficulty levels.
  - **Bot vs Bot mode**, with the ability to pick your preferred bot.

- **Multiplayer Mode (Proof of Concept):**
  - A functional prototype of online multiplayer (not production-ready).

- **Tutorial Mode:**
  - A step-by-step tutorial with **5 introductory lessons** to learn the game.

---

## 🛠️ Future Improvements

We are working on exciting new features, including:

- 🎮 **Enhanced Multiplayer Mode** – Fully functional with online matchmaking.
- 👨‍🎨 **Visual Enhancements** – Additional animations and effects.
- 🎤 **Live Watch Mode** – Spectate live games.
- 🌟 **Extended Tutorial** – More in-depth lessons to master strategies.

---

## 📚 Frisian Draughts Rules

**1. Board Setup:**  
  - Played on a **10x10 board** using dark squares only.  
  - Each player starts with **20 pieces**, and white moves first.

**2. Basic Moves:**  
  - Pawns move diagonally forward one square.  
  - Kings move diagonally or orthogonally any number of squares.

**3. Capturing:**  
  - Capturing is **mandatory**, prioritizing the largest number of opponent pieces.  
  - Kings can capture horizontally, vertically, and diagonally.

**4. Win Conditions:**  
  - The game ends when a player has no legal moves or no pieces left.  
  - A draw is possible after repetition or mutual agreement.

---

## 🎧 Sound & Music

- **Move Sound Effect:** [Creative Commons 0](https://freesound.org/s/371352/)
- **Background Music:** [Uppbeat](https://uppbeat.io/t/pecan-pie/important-to-you)
  - License code: `0V6UVBKUHDBDI1XW`

---

## 🌟 Thank You!

We appreciate your support in trying Frisian Draughts. Stay tuned for updates and new features! Have feedback? Submit an issue or suggestion via our [GitHub Issues page](https://github.com/NoamFav/frisian-draught/issues).
