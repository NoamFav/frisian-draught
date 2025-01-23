<div align="center">

# 🌟 Frisian Draughts Game 🌟

[![Java](https://img.shields.io/badge/Java-23-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-23-blue.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)

<img src="https://github.com/NoamFav/frisian-draught/raw/main/src/main/resources/icons/Icon-linux.png" width="250" alt="Frisian Draughts Logo"/>

A modern implementation of the classic Frisian Draughts board game with advanced AI, multiplayer capabilities, and a beautiful interface.

[Installation](#-installation-guide) • 
[Features](#-key-features) • 
[Game Rules](#-game-rules) • 
[How to Play](#-how-to-play)

</div>

---

## 🎮 Overview

Welcome to **Frisian Draughts**, an exciting modern twist on the classic board game! Built with **JavaFX**, it offers immersive gameplay, smart AI opponents, multiplayer capabilities, and a beautifully designed interface. Whether you're a beginner or a draughts pro, Frisian Draughts provides a challenging and rewarding experience!

<div align="center">
  <img src="https://github.com/NoamFav/frisian-draught/raw/main/src/main/resources/gameplay.png" width="600" alt="Frisian Draughts Gameplay"/>
</div>

---

## 🛠️ Prerequisites

Before jumping into the action, make sure your system is ready to run Frisian Draughts:

| Requirement | Version | Notes |
|-------------|---------|-------|
| **Java** | 23 or later | [Installation Guide](https://www.oracle.com/java/technologies/downloads/#java23) |
| **Maven** | 3.8+ | [Installation Guide](https://maven.apache.org/install.html) |
| **JavaFX** | 23 | Already included in project dependencies |
| **Graphics** | - | OpenGL 2.1+ compatible graphics card |
| **Storage** | - | 100MB free disk space |

---

## 🌱 Installation Guide

<details open>
<summary><b>Quick Start</b></summary>

Follow these simple steps to set up the game:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/NoamFav/frisian-draught.git
   cd frisian-draught
   ```

2. **Build and run the game:**
   ```bash
   mvn clean javafx:run
   ```

That's it! The game will launch automatically. 🚀

</details>

---

## 🎮 How to Play

<details>
<summary><b>Package for Standalone Use</b></summary>

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
# OR
mvn clean package -Djpackage.type=rpm
```

Your game will be installed with a simple click! 💾

</details>

<details>
<summary><b>Running in an IDE</b></summary>

You can also run the game from your favorite IDE (IntelliJ IDEA, Eclipse, etc.) by executing the `Launcher` class.

**IntelliJ IDEA:**
1. Open the project
2. Navigate to `src/main/java/com/frisiandraught/Launcher.java`
3. Right-click and select "Run Launcher.main()"

**Eclipse:**
1. Import the project as a Maven project
2. Find the Launcher class
3. Right-click and select "Run As > Java Application"

</details>

<details>
<summary><b>Game Controls</b></summary>

- **Mouse**: Click to select and move pieces
- **Esc**: Open game menu
- **M**: Toggle sound on/off
- **T**: Switch theme (Light/Dark)
- **R**: Restart current game
- **H**: Show hint (where available)
- **Space**: Pause AI thinking (in AI matches)

</details>

---

## 📺 Key Features

<div style="display: flex; flex-wrap: wrap;">

<div style="flex: 1; min-width: 250px; padding: 10px;">
<h3>🌈 Beautiful UI</h3>
<p>Choose between Light and Dark mode themes for an enhanced visual experience that's easy on the eyes. The interface adapts to your system preferences by default.</p>
</div>

<div style="flex: 1; min-width: 250px; padding: 10px;">
<h3>🎤 Dynamic Sound Effects</h3>
<p>Immerse yourself with exciting audio feedback for moves, captures, and game events. All sounds can be toggled on/off at any time.</p>
</div>

<div style="flex: 1; min-width: 250px; padding: 10px;">
<h3>🤖 Advanced AI</h3>
<p>Test your skills against multiple difficulty levels of AI opponents, or watch bots battle each other as you learn strategies!</p>
</div>

<div style="flex: 1; min-width: 250px; padding: 10px;">
<h3>👥 Multiplayer Mode</h3>
<p>Enjoy a proof-of-concept multiplayer experience to test your skills online against other players around the world.</p>
</div>

<div style="flex: 1; min-width: 250px; padding: 10px;">
<h3>🎓 Interactive Tutorials</h3>
<p>Master the game through five detailed interactive lessons that guide you through basic moves to advanced strategies.</p>
</div>

<div style="flex: 1; min-width: 250px; padding: 10px;">
<h3>🏡 Customization</h3>
<p>Adjust sound settings, theme preferences, and board styles to create your perfect playing environment.</p>
</div>

</div>

---

## 🛠️ Planned Features

<table>
<tr>
<td width="33%">
<h3>💪 Enhanced Multiplayer</h3>
<p>Fully functional online matchmaking with player rankings and tournaments.</p>
</td>
<td width="33%">
<h3>🎮 Live Watch Mode</h3>
<p>Spectate ongoing games between top players to learn advanced strategies.</p>
</td>
<td width="33%">
<h3>💨 Visual Improvements</h3>
<p>Additional animations and visual enhancements for a more immersive experience.</p>
</td>
</tr>
<tr>
<td width="33%">
<h3>🌀 Enhanced AI</h3>
<p>More sophisticated AI algorithms for even tougher single-player challenges.</p>
</td>
<td width="33%">
<h3>🎉 Expanded Tutorials</h3>
<p>More comprehensive learning resources for players of all skill levels.</p>
</td>
<td width="33%">
<h3>📱 Mobile Support</h3>
<p>Cross-platform support for mobile devices to play on the go.</p>
</td>
</tr>
</table>

---

## 🌟 Game Rules

<details open>
<summary><b>Basic Rules</b></summary>

**1. Board Setup:**
- Played on a **10x10 board**, dark squares only.
- Each player starts with **20 pieces**, and white moves first.

**2. Moves:**
- Pawns move diagonally forward one square.
- Kings move diagonally or orthogonally across multiple squares.

**3. Capturing:**
- Mandatory captures with multi-jump sequences.
- Can capture in any direction, vertically and horizontally as well.

**4. Win Conditions:**
- Win by eliminating all opponent pieces or blocking their moves.

**5. Special Rules:**
- King promotion occurs upon reaching the opponent's back row.
- Capture priority is given to moves with the highest piece value.

</details>

<div align="center">
  <img src="https://github.com/NoamFav/frisian-draught/raw/main/src/main/resources/images/board_setup.png" width="400" alt="Board Setup"/>
</div>

---

## 🎧 Sound & Music

- **Move Sound Effect:** [Creative Commons 0](https://freesound.org/s/371352/)
- **Background Music:** [Uppbeat](https://uppbeat.io/t/pecan-pie/important-to-you)
  - License code: `0V6UVBKUHDBDI1XW`

---

## 👨‍💻 Technical Details

<details>
<summary><b>Architecture</b></summary>

The game is built using a Model-View-Controller (MVC) architecture:
- **Model**: Game logic, board state, and AI algorithms
- **View**: JavaFX UI components and animations
- **Controller**: User input handling and game flow management

Key technologies:
- **JavaFX**: UI framework
- **FXML**: Layout definition
- **CSS**: Styling and theming
- **Minimax Algorithm**: Core of the AI decision-making process

</details>

<details>
<summary><b>Performance Optimizations</b></summary>

- **Alpha-beta pruning**: Enhances AI decision speed
- **Move caching**: Reduces redundant calculations
- **Lazy loading**: Improves startup time
- **Responsive design**: Adapts to various screen sizes

</details>

---

## 🤝 Contributing

We welcome contributions to make Frisian Draughts even better! Whether it's adding new features, fixing bugs, or improving documentation, your help is appreciated.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please make sure to update tests as appropriate and adhere to the code style guidelines.

---

## 🌟 Thank You!

We appreciate your support and feedback! 🎉 Stay tuned for more updates and exciting features.

Have questions or suggestions? Feel free to submit an issue on our [GitHub Issues page](https://github.com/NoamFav/frisian-draught/issues).

<div align="center">

### Let the games begin! 🏆

</div>
