package chess.controller;

import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.chessai.ChessAI;
import chess.view.ChessBoardView;
import chess.view.ChessView;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class BoardEditor implements MouseListener, MouseMotionListener, KeyListener {

    private final GameModel gameModel;
    private final ChessView view;

    public BoardEditor() {
        gameModel = new GameModel("8/8/8/8/8/8/8/8 w - - 0 1");
        view = new ChessView(gameModel.getBoard().getPieceArray(), this, this, this);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public static void main(String[] args) {
        new BoardEditor();
    }
}
