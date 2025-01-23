package com.frisian_draught.board;

import com.frisian_draught.AI.DQNModel;
import com.frisian_draught.AI.ReplayBuffer;
import com.frisian_draught.Launcher;
import com.frisian_draught.Player;
import com.frisian_draught.Server.NetworkClient;
import com.frisian_draught.board.Bot.Bot;
import com.frisian_draught.util.SoundPlayer;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardState {
    // Constants
    private static final int BOARD_SIZE = 10;

    private static final int REPLAY_BUFFER_SIZE = 10000;
    // Game state variables
    public boolean isWhiteTurn = true; // White starts first
    private boolean isActive = true;
    private boolean boardInitialized = false;
    private boolean isAnimating = false;
    public boolean isMultiplayer = false;

    // Board-related fields
    private Vector2i boardSize = new Vector2i(BOARD_SIZE, BOARD_SIZE);
    private float tileSize;
    private GridPane board;
    private Node[][] boardTiles = new Node[BOARD_SIZE][BOARD_SIZE];
    private Pane root;
    // Pawn and move management
    private Pawn focusedPawn;

    private List<Pawn> allPawns = new ArrayList<>();
    private List<Pawn> pawns = new ArrayList<>();
    private List<Pawn> requiredPawns = new ArrayList<>();
    private List<Vector2i> possibleMoves = new ArrayList<>();
    private List<CapturePath> currentCapturePaths = new ArrayList<>();

    private List<Move> takenMoves = new ArrayList<>();

    private MovesListManager movesListManager;
    private List<GameState> pastStates = new ArrayList<>();
    private Map<Pawn, ImageView> pawnViews = new HashMap<>();
    private List<Node> highlightNodes = new ArrayList<>();
    // Sound and game info
    private SoundPlayer soundPlayer = Launcher.soundPlayer;
    private GameInfo gameInfo;
    private DQNModel botModel;
    public boolean isBotActive = false;
    public boolean isBotvsBot = false;
    private ReplayBuffer replayBuffer = new ReplayBuffer(REPLAY_BUFFER_SIZE);

    private final int BATCH_SIZE = 32;

    private final double GAMMA = 0.99;

    private final double CONFIDENCE_THRESHOLD = 0.2;

    private int episodeCounter = 0;

    private Bot botPlayer;

    private Bot BotvsBotWhite;

    private Bot BotvsBotBlack;

    private NetworkClient networkClient;

    private Player player;
    private Player opponent;

    private final Object lock = new Object();

    public Object getLock() {
        return lock;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public static int getReplayBufferSize() {
        return REPLAY_BUFFER_SIZE;
    }

    public static int getMainBoardSize() {
        return BOARD_SIZE;
    }

    public int getBATCH_SIZE() {
        return BATCH_SIZE;
    }

    public double getGAMMA() {
        return GAMMA;
    }

    public double getCONFIDENCE_THRESHOLD() {
        return CONFIDENCE_THRESHOLD;
    }

    public int getEpisodeCounter() {
        return episodeCounter;
    }

    public void setEpisodeCounter(int episodeCounter) {
        this.episodeCounter = episodeCounter;
    }

    public Bot getBotPlayer() {
        return botPlayer;
    }

    public void setBotPlayer(Bot botPlayer) {
        this.botPlayer = botPlayer;
    }

    public Bot getBotvsBotWhite() {
        return BotvsBotWhite;
    }

    public void setBotvsBotWhite(Bot botvsBotWhite) {
        BotvsBotWhite = botvsBotWhite;
    }

    public Bot getBotvsBotBlack() {
        return BotvsBotBlack;
    }

    public void setBotvsBotBlack(Bot botvsBotBlack) {
        BotvsBotBlack = botvsBotBlack;
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    public void setNetworkClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void setWhiteTurn(boolean isWhiteTurn) {
        this.isWhiteTurn = isWhiteTurn;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isBoardInitialized() {
        return boardInitialized;
    }

    public void setBoardInitialized(boolean boardInitialized) {
        this.boardInitialized = boardInitialized;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void setAnimating(boolean isAnimating) {
        this.isAnimating = isAnimating;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public void setMultiplayer(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
    }

    public Vector2i getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(Vector2i boardSize) {
        this.boardSize = boardSize;
    }

    public float getTileSize() {
        return tileSize;
    }

    public void setTileSize(float tileSize) {
        this.tileSize = tileSize;
    }

    public GridPane getBoard() {
        return board;
    }

    public void setBoard(GridPane board) {
        this.board = board;
    }

    public Node[][] getBoardTiles() {
        return boardTiles;
    }

    public void setBoardTiles(Node[][] boardTiles) {
        this.boardTiles = boardTiles;
    }

    public Pane getRoot() {
        return root;
    }

    public void setRoot(Pane root) {
        this.root = root;
    }

    public Pawn getFocusedPawn() {
        return focusedPawn;
    }

    public void setFocusedPawn(Pawn focusedPawn) {
        this.focusedPawn = focusedPawn;
    }

    public List<Pawn> getAllPawns() {
        return allPawns;
    }

    public void setAllPawns(List<Pawn> allPawns) {
        this.allPawns = allPawns;
    }

    public List<Pawn> getPawns() {
        return pawns;
    }

    public void setPawns(List<Pawn> pawns) {
        this.pawns = pawns;
    }

    public List<Pawn> getRequiredPawns() {
        return requiredPawns;
    }

    public void setRequiredPawns(List<Pawn> requiredPawns) {
        this.requiredPawns = requiredPawns;
    }

    public List<Vector2i> getPossibleMoves() {
        return possibleMoves;
    }

    public void setPossibleMoves(List<Vector2i> possibleMoves) {
        this.possibleMoves = possibleMoves;
    }

    public List<CapturePath> getCurrentCapturePaths() {
        return currentCapturePaths;
    }

    public void setCurrentCapturePaths(List<CapturePath> currentCapturePaths) {
        this.currentCapturePaths = currentCapturePaths;
    }

    public List<Move> getTakenMoves() {
        return takenMoves;
    }

    public void setTakenMoves(List<Move> takenMoves) {
        this.takenMoves = takenMoves;
    }

    public MovesListManager getMovesListManager() {
        return movesListManager;
    }

    public void setMovesListManager(MovesListManager movesListManager) {
        this.movesListManager = movesListManager;
    }

    public List<GameState> getPastStates() {
        return pastStates;
    }

    public void setPastStates(List<GameState> pastStates) {
        this.pastStates = pastStates;
    }

    public Map<Pawn, ImageView> getPawnViews() {
        return pawnViews;
    }

    public void setPawnViews(Map<Pawn, ImageView> pawnViews) {
        this.pawnViews = pawnViews;
    }

    public List<Node> getHighlightNodes() {
        return highlightNodes;
    }

    public void setHighlightNodes(List<Node> highlightNodes) {
        this.highlightNodes = highlightNodes;
    }

    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }

    public void setSoundPlayer(SoundPlayer soundPlayer) {
        this.soundPlayer = soundPlayer;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public DQNModel getBotModel() {
        return botModel;
    }

    public void setBotModel(DQNModel botModel) {
        this.botModel = botModel;
    }

    public boolean isBotActive() {
        return isBotActive;
    }

    public void setBotActive(boolean isBotActive) {
        this.isBotActive = isBotActive;
    }

    public boolean isBotvsBot() {
        return isBotvsBot;
    }

    public void setBotvsBot(boolean isBotvsBot) {
        this.isBotvsBot = isBotvsBot;
    }

    public ReplayBuffer getReplayBuffer() {
        return replayBuffer;
    }

    public void setReplayBuffer(ReplayBuffer replayBuffer) {
        this.replayBuffer = replayBuffer;
    }

    public Map<Vector2i, Pawn> getPawnPositionMap() {
        Map<Vector2i, Pawn> positionMap = new HashMap<>();
        for (Pawn pawn : pawns) { // 'pawns' is the list of active pawns
            positionMap.put(pawn.getPosition(), pawn);
        }
        return positionMap;
    }
}
