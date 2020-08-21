package main.java.controller;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.Move;
import main.java.model.SquareModel;
import main.java.model.pieces.Piece;
import main.java.view.BoardView;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This class controls both the model and the view for the
 * Chess game.
 */
public class ChessController implements MouseListener, MouseMotionListener {

    private static final boolean RIGHT_CLICK_DEBUG = true;

    private final GameModel gameModel;
    private final BoardView boardView;

    private int xOnSquare; // x Pos of mouse relative to the square it is in
    private int yOnSquare; // y Pos of mouse relative to the square it is in
    private int xOnScreen; // y Pos of mouse relative to the frame
    private int yOnScreen; // y Pos of mouse relative to the frame

    private ChessController() {
        gameModel = new GameModel();
        boardView = new BoardView(this);
    }

    public static void main(String[] args) {
        new ChessController();
    }

    /**
     * @return the model of the board.
     */
    public SquareModel[][] getBoard() {
        return gameModel.getBoardModel().getBoard();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        e.getComponent().setLocation(e.getX() + e.getComponent().getX() - xOnSquare,
                e.getY() + e.getComponent().getY() - yOnSquare);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        xOnSquare = e.getX();
        yOnSquare = e.getY();
        xOnScreen = e.getXOnScreen();
        yOnScreen = e.getYOnScreen() - boardView.getTopBarSize();

        if (RIGHT_CLICK_DEBUG && e.getButton() == MouseEvent.BUTTON3) {
            Piece piece = gameModel.getBoardModel().getPieceOnSquare(boardView.getSquareFromPixel(xOnScreen, yOnScreen));
            System.out.println(piece.toString());
            System.out.println("Moves: ");
            for (Move move : piece.getPossibleMoves(gameModel)) {
                System.out.println(move.toString() + ", ");
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int endX = e.getXOnScreen();
        int endY = e.getYOnScreen() - boardView.getTopBarSize();

        // get square piece is on
        ChessCoordinate startCoordinate = boardView.getSquareFromPixel(xOnScreen, yOnScreen);
        ChessCoordinate endCoordinate = boardView.getSquareFromPixel(endX, endY);

        // Check that start coordinate and end coordinate are different, and that there is a piece on start coordinate.
        if (!startCoordinate.equals(endCoordinate) && gameModel.getBoardModel().getPieceOnSquare(startCoordinate) != null) {
            // Check if it can move there
            gameModel.move(startCoordinate, endCoordinate);
        }
        boardView.updateScreen();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }
}

