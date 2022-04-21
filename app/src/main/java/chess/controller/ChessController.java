package chess.controller;

import chess.ChessCoordinate;
import chess.model.GameModel;
import chess.model.chessai.ChessAI;
import chess.model.chessai.PositionEvaluator;
import chess.model.pieces.Piece;
import chess.view.ChessPieceView;
import chess.view.ChessView;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static chess.model.GameModel.*;

/**
 * This class controls both the model and the view for the
 * Chess game.
 */
public class ChessController implements MouseListener, MouseMotionListener, KeyListener {

    private static final boolean AI_ON = false;

    private static final int MINIMUM_DEPTH = 1;
    private static final int SEARCH_TIME = 1_000;

    private final GameModel gameModel;
    private final ChessView view;
    private final ChessAI chessAI;

    private final Executor aiExecutor;
    private final Executor finishGameExecutor;
    private final Set<Integer> pressedKeyCodes;
    private CompletableFuture<Void> futureAIMove;
    private ChessCoordinate startCoordinate;

    private ChessController() {
        gameModel = new GameModel("2k5/1ppppp2/8/1K1P3q/8/8/2P1PP1P/8 w - - 0 1");
        view = new ChessView(gameModel.getBoard().getPieceArray(), this, this, this);
        chessAI = new ChessAI(new PositionEvaluator(gameModel), gameModel, true, true);
        aiExecutor = Executors.newSingleThreadExecutor();
        finishGameExecutor = Executors.newSingleThreadExecutor();
        pressedKeyCodes = new HashSet<>();
    }

    public static void main(String[] args) {
        new ChessController();
    }

    /**
     * Attempts to make a move given two coordinates.
     *
     * @param startCoordinate the starting coordinate
     * @param endCoordinate   the ending coordinate
     */
    private void makeMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        if (gameModel.move(startCoordinate, endCoordinate, Piece.WHITE_QUEEN)) {
            updateScreen(false);

            if (AI_ON) {
                makeAIMove();
            } else {
                if (gameModel.getGameOverStatus() != IN_PROGRESS) {
                    System.out.println("GAME OVER");
                }
            }
        } else {
            view.pack();
        }
    }

    private void undoMove() {
        gameModel.undoLastMove();
        updateScreen(true);
    }

    private void updateScreen(boolean doSlowUpdate) {
        if (doSlowUpdate) {
            view.slowUpdate(gameModel.getBoard().getPieceArray(),
                    this, this, gameModel.getTurn());
        } else {
            view.updateScreen(gameModel.getLastMove());
            view.pack();
        }
    }

    private void makeAIMove() {
        if (futureAIMove == null || futureAIMove.isDone()) {
            futureAIMove = CompletableFuture
                    .runAsync(() -> gameModel.move(chessAI.getBestMove(MINIMUM_DEPTH, SEARCH_TIME)), aiExecutor)
                    .thenRun(this::printAndUpdate)
                    .exceptionally((ex) -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    private void printAndUpdate() {
        updateScreen(false);
        if (gameModel.getGameOverStatus() != IN_PROGRESS) {
            System.out.printf("GAME OVER (%s)\n", switch (gameModel.getGameOverStatus()) {
                case DRAW -> "Draw";
                case LOSER -> gameModel.getTurn() == WHITE ? "White lost" : "Black Lost";
                default -> "Error";
            });
        }
    }

    private void letAIFinishGame() {
        if (gameModel.getGameOverStatus() == IN_PROGRESS && !pressedKeyCodes.contains(KeyEvent.VK_S)) {
            CompletableFuture.runAsync(this::makeAIMove, finishGameExecutor)
                    .thenRunAsync(() -> {
                        futureAIMove.join();
                        letAIFinishGame();
                    }, finishGameExecutor)
                    .exceptionally((ex) -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(MouseEvent e) {
        Component component = e.getComponent();
        if (component instanceof ChessPieceView) {
            startCoordinate = view.getCoordinateOf(component, e.getX(), e.getY());
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        ChessCoordinate endCoordinate = view.getCoordinateOf(e.getComponent(), e.getX(), e.getY());
        if (futureAIMove == null || futureAIMove.isDone()) {
            makeMove(startCoordinate, endCoordinate);
        } else {
            view.pack();
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  {@code MOUSE_DRAGGED} events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p>
     * Due to platform-dependent Drag&amp;Drop implementations,
     * {@code MOUSE_DRAGGED} events may not be delivered during a native
     * Drag&amp;Drop operation.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        Component piece = e.getComponent();
        int xOnWindow = e.getX() + e.getComponent().getX() - piece.getWidth() / 2;
        int yOnWindow = e.getY() + e.getComponent().getY() - piece.getHeight() / 2;
        e.getComponent().setLocation(xOnWindow, yOnWindow);
    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key pressed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getExtendedKeyCode();
        pressedKeyCodes.add(keyCode);
    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link KeyEvent} for a definition of
     * a key released event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getExtendedKeyCode();
        pressedKeyCodes.remove(keyCode);

        switch (keyCode) {
            case KeyEvent.VK_RIGHT -> makeAIMove();
            case KeyEvent.VK_LEFT -> undoMove();
            case KeyEvent.VK_F -> letAIFinishGame();
            case KeyEvent.VK_P -> System.out.println(gameModel.getFEN());
        }
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseMoved(MouseEvent e) {
    }


    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }
}

