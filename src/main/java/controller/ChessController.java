package main.java.controller;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.view.ChessPieceView;
import main.java.view.ChessView;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This class controls both the model and the view for the
 * Chess game.
 */
public class ChessController implements MouseListener, MouseMotionListener {

    private final GameModel gameModel;
    private final ChessView view;

    private ChessCoordinate startCoordinate;
    private int xOnSquare = 0;
    private int yOnSquare = 0;

    private ChessController() {
        gameModel = new GameModel();
        view = new ChessView(gameModel.getBoard().getPieceArray(), this, this);
    }

    public static void main(String[] args) {
        new ChessController();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        e.getComponent().setLocation(e.getX() - xOnSquare + e.getComponent().getX(),
                e.getY() - yOnSquare + e.getComponent().getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Component component = e.getComponent();
        if (component instanceof ChessPieceView && ((ChessPieceView) component).isOnBoard()) {
            xOnSquare = e.getX();
            yOnSquare = e.getY();
            startCoordinate = view.getCoordinateOf(component, e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        ChessCoordinate endCoordinate = view.getCoordinateOf(e.getComponent(),
                e.getX(), e.getY());
        if (gameModel.move(startCoordinate, endCoordinate)) {
            view.updateScreen(gameModel.getLastMove());
        }
        view.pack();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            System.out.println();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}
}

