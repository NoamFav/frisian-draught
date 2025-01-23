# 🌟 Frisian Draughts Game 🌟

Welcome to **Frisian Draughts**, an exciting modern twist on the classic board game! Built with **JavaFX**, it offers immersive gameplay, smart AI opponents, multiplayer capabilities, and a beautifully designed interface. Whether you're a beginner or a draughts pro, Frisian Draughts provides a challenging and rewarding experience! 🎮🚀

---

## 🛠️ Prerequisites

Before jumping into the action, make sure your system is ready to run Frisian Draughts:

- 💻 **Java 22 or later**  
- 📚 **Maven** ([Installation Guide](https://maven.apache.org/install.html))  
- 📁 **JavaFX 22**, already included in project dependencies  

---

## 🌱 Installation Guide

Follow these simple steps to set up the game:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/NoamFav/frisian-draught.git
   cd frisian-draught
   ```

2. **Run the game:**
   ```bash
   mvn clean javafx:run
   ```

Enjoy the game in no time! 🌈

---

## 🎮 How to Play

### 💻 Package for Standalone Use

Easily create an installer and play Frisian Draughts natively on your system.

**Windows:**
```bash
mvn clean package -Djpackage.type=exe
```

**macOS:**
```bash
mvn clean package -Djpackage.type=dmg
```

**Linux:**
```bash
mvn clean package -Djpackage.type=deb
mvn clean package -Djpackage.type=rpm
```

Your game will be installed with a simple click! 💾

### 📚 Running in an IDE

You can also run the game from your favorite IDE (IntelliJ IDEA, Eclipse, etc.) by executing the `Launcher` class to jump straight into the action! 💪

---

## 📺 Key Features

- 🌈 **Beautiful UI:** Light/Dark mode themes for an enhanced experience.
- 🎤 **Dynamic Sound Effects:** Immerse yourself with exciting audio.
- 🤖 **Advanced AI:** Choose different bot types or watch bots battle it out!
- 👥 **Multiplayer Mode:** A proof-of-concept multiplayer experience to test your skills online.
- 🎓 **Interactive Tutorials:** Five detailed lessons to master the game mechanics.
- 🏡 **Customization:** Adjustable sound settings, theme preferences, and board styles.

---

## 🛠️ Planned Features

The Frisian Draughts project is constantly evolving! Upcoming features include:

- 💪 Fully functional multiplayer with online matchmaking.
- 🎮 Live Watch Mode to spectate ongoing games.
- 💨 Additional animations and visual enhancements.
- 🌀 Enhanced AI for tougher single-player challenges.
- 🎉 Expanded tutorial modes for a better learning experience.

---

## 🌟 Game Rules

**1. Board Setup:**  
  - Played on a **10x10 board**, dark squares only.  
  - Each player starts with **20 pieces**, and white moves first.

**2. Moves:**  
  - Pawns move diagonally forward one square.
  - Kings move diagonally or orthogonally across multiple squares.

**3. Capturing:**  
  - Mandatory captures with multi-jump sequences.
  - Can capture in any direction, vertically and horizontally as well

**4. Win Conditions:**  
  - Win by eliminating all opponent pieces or blocking their moves.

**5. Special Rules:**  
  - King promotion occurs upon reaching the opponent's back row.
  - Capture priority is given to moves with the highest piece value.

---

## 🎧 Sound & Music

- **Move Sound Effect:** [Creative Commons 0](https://freesound.org/s/371352/)
- **Background Music:** [Uppbeat](https://uppbeat.io/t/pecan-pie/important-to-you)
  - License code: `0V6UVBKUHDBDI1XW`

---

## 🌟 Thank You!

We appreciate your support and feedback! 🎉 Stay tuned for more updates and exciting features.

Have questions or suggestions? Feel free to submit an issue on our [GitHub Issues page](https://github.com/NoamFav/frisian-draught/issues).

Let the games begin! 🏆
