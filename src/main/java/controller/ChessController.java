package main.java.controller;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.GameModel;
import main.java.model.chessai.ChessAI;
import main.java.model.chessai.PieceValueEvaluator;
import main.java.view.ChessPieceView;
import main.java.view.ChessView;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * This class controls both the model and the view for the
 * Chess game.
 */
public class ChessController implements MouseListener, MouseMotionListener, KeyListener {

    private static final boolean AI_ON = false;

    private final GameModel gameModel;
    private final ChessView view;
    private final ChessAI chessAI;

    private final Set<Integer> pressedKeyCodes;
    private ChessCoordinate startCoordinate;
    private int xOnSquare = 0;
    private int yOnSquare = 0;

    private ChessController() {
        gameModel = new GameModel();
        view = new ChessView(gameModel.getBoard().getPieceArray(), this, this);
        chessAI = new ChessAI(new PieceValueEvaluator());
        pressedKeyCodes = new HashSet<>();
    }

    public static void main(String[] args) {
        new ChessController();
    }

    /**
     * Attempts to make a move given two coordinates.
     *
     * @param startCoordinate the starting coordinate
     * @param endCoordinate the ending coordinate
     */
    private void makeMove(ChessCoordinate startCoordinate, ChessCoordinate endCoordinate) {
        if (gameModel.move(startCoordinate, endCoordinate)) {
            view.updateScreen(gameModel.getLastMove());
            view.pack();
            if (AI_ON && gameModel.move(chessAI.getBestMove(gameModel))) {
                view.updateScreen(gameModel.getLastMove());
            }
        }
        view.pack();
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(MouseEvent e) {
        Component component = e.getComponent();
        if (component instanceof ChessPieceView && ((ChessPieceView) component).isOnBoard()) {
            xOnSquare = e.getX();
            yOnSquare = e.getY();
            startCoordinate = view.getCoordinateOf(component, xOnSquare, yOnSquare);
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
        makeMove(startCoordinate, endCoordinate);
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
        int xOnWindow = e.getX() - xOnSquare + e.getComponent().getX();
        int yOnWindow = e.getY() - yOnSquare + e.getComponent().getY();
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
        pressedKeyCodes.add(e.getKeyCode());
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
        pressedKeyCodes.remove(e.getKeyCode());
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseClicked(MouseEvent e) {}

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseExited(MouseEvent e) {}

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseMoved(MouseEvent e) {}


    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(KeyEvent e) {}
}

