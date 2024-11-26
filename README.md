# Frisian Draughts Game

This project is a Frisian Draughts game implemented using JavaFX. It features custom UI elements, move tracking, capture path logic, and sound effects. The game supports different types of pawns and kings, with hover states, and includes logic for non-capturing moves, multi-capture paths, and more.

## Prerequisites

- Java 22 or later.
- Maven installed. [Maven Installation Guide](https://maven.apache.org/install.html)
- JavaFX 22 or later, added to the project dependencies.

## Installation

1. Clone the repository:
    ```bash
    git clone <repository_url>
    ```
   
2. Ensure you have JavaFX and the necessary dependencies. If using Maven, the dependencies are already defined in the `pom.xml`.

3. Navigate to the project folder:
    ```bash
    cd <project-folder>
    ```

## Running the Game

### Using Maven

To run the game via Maven, use the following command:
```bash
mvn clean javafx:run
```

### Using an IDE

You can also run the game using an IDE like IntelliJ IDEA or Eclipse by running the `Launcher` class.

## User Interface (UI)

The game interface is built using JavaFX and includes the following key components:

### How to Use the UI

1. **Main Menu**:
   - From the **main menu**, players can start a new game, view settings, or exit the game.
   - Navigation is done through buttons like "Start Game", "Settings", and "Exit". Clicking "Start Game" will initialize the game board.

2. **In-Game Interface**:
   - Once a game is started, the **game board** is displayed with pawns already set up for both players. Players can interact with the game by clicking on their pawns and selecting valid moves.
   - The UI highlights available moves, and captures are indicated by red highlights. Click on a valid destination to move your piece.

3. **Move List**:
   - A **moves list** is available, showing the history of moves made during the game. This list is displayed on the right side of the screen for reference.

4. **Game Control Buttons**:
   - **Undo**: This button is currently non-functional, but a placeholder exists for future implementation.
   - **Draw**: Allows players to offer a draw. A confirmation popup will appear.
   - **Resign**: Clicking "Resign" will end the game and declare the other player the winner.
   - **Restart**: Restarts the game after confirming with the player that all progress will be lost.
   - **Settings**: This button is currently a placeholder for future settings adjustments.
   - **Exit**: Returns to the main menu or exits the game entirely.

5. **Exit Confirmation**:
   - When the player chooses to exit the game, a confirmation dialog will prompt the user to confirm if they want to quit. They can select "Yes" or "No" to continue or cancel.

### What's Done
- The main game UI is fully functional, including the game board, pawns, and control buttons for gameplay interactions.
- Basic game features like move highlighting, pawn movement, captures, and scoring are implemented.
- Game start, restart, resign, and exit functions work as expected.
- Draw offers are supported, and the game tracks the move history but it is only a random draw as no AI nor multiplayer mode is implemented.
- Sound effects are added for moves, and background music plays during the game.

### What's Pending
- **Undo Functionality**: While the button is in place, the undo feature is not yet implemented.
- **Multiplayer Mode**: The game currently supports single-player mode only. Multiplayer mode will be added in the future once network capabilities are implemented.
- **Live Watch Mode**: A live watch mode to observe games between two players is planned.
- **Chat Feature**: A chat feature for multiplayer games is planned to allow players to communicate during the game.
- **Tutorial Mode**: A tutorial mode to help players learn the game rules and mechanics is planned with interactive guides and hints.
- **Settings Page**: The settings button exists but is not functional.
- **Enhanced AI for Single Player**: This feature is pending, it will be implemented soon.
- **Additional Visual Effects**: Some polishing and potential animations could enhance the user experience further.


## Frisian Draughts Game Rules

Frisian Draughts is a variant of international draughts (checkers) played on a 10x10 board, but with some unique and complex capturing rules. Below are the official game rules in detail:

### 1. Board and Initial Setup
- The game is played on a **10x10 board**, using only the 50 dark squares.
- Each player starts with **20 pieces**: White starts first. The pawns are placed on the first four rows closest to each player.
- The game is managed through the `MainBoard` class, which handles board initialization, rendering, and interaction.

### 2. Basic Moves
- **Pawns** move diagonally forward one square.
- **Kings**, which are promoted pawns, can move diagonally any number of squares.
- Moves and interactions in this implementation are handled via mouse clicks on highlighted squares, with hover effects providing visual feedback.

### 3. Capturing Rules
Capturing is **mandatory**, and Frisian Draughts follows strict capture rules:
- Pawns capture by jumping over an adjacent opponent's piece to an empty square behind it.
- **Kings** can capture diagonally or **orthogonally** (left, right, up, or down) over any number of empty squares, but they must continue capturing if possible.
  
  In the game, the `captureCheck()` method recursively explores all possible capture paths from the current position of a pawn or king. Capture paths are highlighted in red.

#### 3.1. Multi-Capture
- A piece must capture the **maximum number** of opponent's pieces available. If there are multiple paths, the player must choose the path that captures the highest-value pieces, where kings are worth **1.5 pawns**.
- This logic is enforced through the `findPawnsWithMaxCaptures()` method, which identifies which pawns or kings are required to move based on the best available captures.

### 4. Special Frisian Draughts Capture Rules
Frisian Draughts introduces unique capture mechanics:
- **Orthogonal Capturing**: Kings can capture pieces horizontally or vertically as well as diagonally, which significantly increases the tactical depth of the game.
- The game checks for **capture continuation**, meaning if a piece can continue capturing after its first capture, it must do so.

### 5. Turn Switching and Scoring
- The turn system is managed by the `switchTurn()` method, which alternates between the players and checks for new captures.
- Each player's score is tracked based on the number of captured pieces, with points updated via `gameInfo.scorePlayerOne` and `gameInfo.scorePlayerTwo`.

### 6. Game End Conditions
- The game ends when one player cannot make a legal move or has no pieces left.
- A draw can occur if both players agree or if only two kings remain.
  
  The game also supports **threefold repetition**, where a draw is declared if the same board state occurs three times.

### 8. Promotion
- A pawn is **promoted to a king** when it reaches the opponent's back row.
- This is automatically handled in the game via the `promotePawnIfNeeded()` method, which updates the pawn's status and changes its image.

### 9. Animations
- Movement and capture animations are managed using JavaFX's **`TranslateTransition`** and **`SequentialTransition`**, providing smooth and visual feedback for pawn movement and captures.

### 10. Separation of classes
| Class File/ Directory | Relative Path              | Description                                                                                                                                                |
|-----------------------|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Launcher              | com.um_project_game        | Entry point of the application, initializes the main menu, handles scene switching, and theming.                                                           |
| Menu                  | com.um_project_game        | Manages the main menu of the application, including UI elements like buttons, recent games, and live games.                                                |
| Game                  | com.um_project_game        | Defines and manages UI-Elements of the game                                                                                                                |
| ViewManager           | com.um_project_game        | Manages active games and transitions between menu, gameplay and server                                                                                     |
| Settings              | com.um_project_game        | Manages Game Settings UI                                                                                                                                   |
| ExitGameConfirmation  | com.um_project_game        | Displays an exit confirmation when trying to exit a game                                                                                                   |
| /util/...             | com.um_project_game.util   | Manages utility classes, buttons, fileReaders, fileWriters, sound, ...                                                                                     |
| /server/...           | com.um_project_game.Server | Manages Server instance and client handling                                                                                                                |
| CapturePath           | ...um_project_game.board   | Instance of a capturePath, storing a single capture and its steps                                                                                          |
| GameInfo              | ...um_project_game.board   | Stores additional game information                                                                                                                         |
| GameState             | ...um_project_game.board   | Stores important game information regarding the current board position                                                                                     |
| MainBoard             | ...um_project_game.board   | Main game logic class. Stores information about the main board instance and the actual game logic.                                                         |
| Move                  | ...um_project_game.board   | Instance of a single move. Contains the initial and final position of a move in both vector and PDN format.                                                |
| MoveResult            | ...um_project_game.board   | Stores additional information after a taken move.                                                                                                          |
| MovesListManager      | ...um_project_game.board   | Manages the moves list table entries displayed in the game UI                                                                                              |
| Pawn                  | ...um_project_game.board   | Instance of a Pawn                                                                                                                                         |
| DQNModel              | ...um_project_game.AI      | Implements a Deep Q-Network for AI decision-making in games, handling Q-value prediction, training, and weight updates.                                    |
| Experience            | ...um_project_game.AI      | Represents a single learning step for reinforcement learning, containing state, action, reward, next state, and terminal status.                           |
| init.java             | ...um_project_game.AI      |                                                                                                                                                            |
| NeuralNetwork         | ...um_project_game.AI      | 	Implements a basic neural network with forward propagation, backpropagation, and weight updates for training.                                             |
| ReplayBuffer          | ...um_project_game.AI      | Manages a buffer of experiences, allowing the addition, sampling, and maintaining a fixed size for training in reinforcement learning.                     |
| functions             | ...um_project_game.AI.util | AI utility file. Contains various utility methods for Q-learning, training, action selection, and adversarial search in reinforcement learning for a game. |

### License

D9_pawn.mp3 by Iamgiorgio -- https://freesound.org/s/371352/ -- License: Creative Commons 0
piano background 5.wav by Nick_Simon-Adams -- https://freesound.org/s/647614/ -- License: Attribution 4.0
