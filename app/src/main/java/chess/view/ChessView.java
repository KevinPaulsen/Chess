package chess.view;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.Square;
import chess.model.pieces.Piece;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This Class is capable of displaying a Chess Game in a
 * JFrame window.
 */
public class ChessView extends JFrame {

    // The starting size of the screen (Can be adjusted after creation)
    private static final int STARTING_SIZE = 500;

    private final ChessBoardView boardView;
    private final ChessTurnView turnView;

    /**
     * Creates a new ChessView that displays the given board. The
     * Displayed pieces will use the given MouseListener and MouseMotionListener
     * to drag the pieces.
     *
     * @param board the board to display.
     * @param mouseListener the MouseListener for the PieceViews to use.
     * @param motionListener the MouseMotionListener for the PieceViews to use.
     */
    public ChessView(Square[][] board, MouseListener mouseListener,
                     MouseMotionListener motionListener, KeyListener keyListener) {
        boardView = new ChessBoardView(board, mouseListener, motionListener);
        turnView = new ChessTurnView('w');
        init();
        this.add(boardView, BorderLayout.CENTER);
        this.add(turnView, BorderLayout.SOUTH);
        this.addKeyListener(keyListener);
    }

    public void slowUpdate(Square[][] board, MouseListener mouseListener, MouseMotionListener motionListener, char turn) {
        boardView.slowUpdateBoard(board, mouseListener, motionListener);
        turnView.setTurn(turn);
    }

    /**
     * Initializes this JFrame.
     */
    private void init() {
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setPreferredSize(new Dimension(STARTING_SIZE, STARTING_SIZE));
        pack();
    }

    public void updateScreen(Move move) {
        if (move != null) {
            boardView.updateBoard(move);
            turnView.switchTurns();
        }
        this.pack();
    }

    public ChessCoordinate getCoordinateOf(Component component, int mouseX, int mouseY) {
        int squareWidth = boardView.getWidth() / 8;
        int squareHeight = boardView.getHeight() / 8;
        int xCoordinate = (getXOnWindow(component) + mouseX) / (squareWidth);
        int yCoordinate = 7 - (getYOnWindow(component) + mouseY) / squareHeight;
        return BoardModel.getChessCoordinate(xCoordinate, yCoordinate);
    }

    private static int getYOnWindow(Component component) {
        if (component instanceof ChessBoardView) {
            return 0;
        } else {
            return getYOnWindow(component.getParent()) + component.getY();
        }
    }

    private static int getXOnWindow(Component component) {
        if (component instanceof ChessView) {
            return 0;
        } else {
            return getXOnWindow(component.getParent()) + component.getX();
        }
    }

}
