package chess.view;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;

import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This class is capable of creating a JPanel that shows a
 * ChessBoard.
 */
public class ChessBoardView extends JPanel {

    // The panel that contains the squares (background)
    private final JPanel squaresPanel;
    // The panel that contains the pieces.
    private final JPanel piecesPanel;

    /**
     * Creates a new ChessBoardView from the given board,
     * MouseListener and MouseMotionListener.
     *
     * @param board the board to display
     * @param mouseListener the mouseListener for the PieceViews to use.
     * @param motionListener the MouseMotionListener for the PieceViews to use.
     */
    public ChessBoardView(Piece[] board, MouseListener mouseListener,
                          MouseMotionListener motionListener) {
        this.setLayout(new OverlayLayout(this));
        squaresPanel = new JPanel(new GridLayout(8, 8));
        piecesPanel = new JPanel(new GridLayout(8, 8));
        initBoardView(board, mouseListener, motionListener);
    }

    /**
     * Updates the BoardView from the given move.
     *
     * @param move the last move made.
     */
    public void updateBoard(Move move) {
        if (move != null) {

            if (move.getInteractingPieceStart() != null) {
                if (move.getInteractingPieceEnd() == null) {
                    // Capture
                    ((ChessPieceView) piecesPanel.getComponent(getZOrder(move.getInteractingPieceStart()))).capture();
                } else {
                    // Castle
                    swap(move.getInteractingPieceStart(), move.getInteractingPieceEnd());
                }
            }
            ((ChessPieceView) piecesPanel.getComponent(getZOrder(move.getStartingCoordinate()))).capture();
            if (move.doesPromote()) {
                ((ChessPieceView) piecesPanel.getComponent(getZOrder(move.getEndingCoordinate()))).promoteTo(move.getPromotedPiece());
            } else {
                ((ChessPieceView) piecesPanel.getComponent(getZOrder(move.getEndingCoordinate()))).promoteTo(move.getMovingPiece());
            }
        }
    }

    /**
     * Returns the ZOrder of the piece on the given ChessCoordinate.
     *
     * @param coordinate the chessCoordinate to get the ZOrder of
     * @return the ZOrder of the piece on the given coordinate
     */
    private static int getZOrder(ChessCoordinate coordinate) {
        return (7 - coordinate.getRank()) * 8 + coordinate.getFile();
    }

    private void swap(ChessCoordinate coordinate1, ChessCoordinate coordinate2) {
        int zOrder1 = getZOrder(coordinate1);
        int zOrder2 = getZOrder(coordinate2);

        if (zOrder1 > zOrder2) {
            int temp = zOrder1;
            zOrder1 = zOrder2;
            zOrder2 = temp;
        }

        Component first = piecesPanel.getComponent(zOrder1);
        Component second = piecesPanel.getComponent(zOrder2);

        piecesPanel.remove(first);
        piecesPanel.remove(second);

        piecesPanel.add(second, zOrder1);
        piecesPanel.add(first, zOrder2);
    }

    /**
     * Initializes this board View by setting up the piecesPanel and
     * the squaresPanel.
     *
     * @param board the board to display
     * @param mouseListener the MouseListener for the PieceViews to use
     * @param motionListener the MouseMotionListener for the PieceViews to use
     */
    private void initBoardView(Piece[] board, MouseListener mouseListener,
                               MouseMotionListener motionListener) {
        piecesPanel.setOpaque(false);
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                Piece piece = board[rank * 8 + file];
                piecesPanel.add(makePieceView(piece, mouseListener, motionListener));
                squaresPanel.add(makeSquare(rank, file));
            }
        }
        this.add(piecesPanel);
        this.add(squaresPanel);
    }

    public void slowUpdateBoard(Piece[] board, MouseListener mouseListener, MouseMotionListener motionListener) {
        piecesPanel.removeAll();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                Piece piece = board[rank * 8 + file];
                ChessPieceView pieceView = makePieceView(piece, mouseListener, motionListener);
                piecesPanel.add(pieceView);
            }
        }
    }

    /**
     * Creates a new Square from the given rank and file.
     *
     * @param rank the rank of this SquareView.
     * @param file the file of this SquareView.
     * @return the SquareView corresponding to the given rank and file.
     */
    private static JPanel makeSquare(int rank, int file) {
        JPanel panel = new JPanel();
        float[] darkHSB = Color.RGBtoHSB(180, 136, 99, null);
        float[] lightHSB = Color.RGBtoHSB(240, 217, 181, null);
        panel.setBackground((rank + file) % 2 == 0 ? Color.getHSBColor(darkHSB[0], darkHSB[1], darkHSB[2]) :
                Color.getHSBColor(lightHSB[0], lightHSB[1], lightHSB[2]));
        return panel;
    }

    /**
     * Creates a new Square from the given Piece, MouseListener
     * and MouseMotionListener.
     *
     * @param piece the piece on this square.
     * @param mouseListener the MouseListener for this pieceView to use.
     * @param motionListener the MouseMotionListener for this pieceView to use.
     * @return the PieceView representation of this Piece.
     */
    private static ChessPieceView makePieceView(Piece piece, MouseListener mouseListener,
                                        MouseMotionListener motionListener) {
        ChessPieceView pieceView = new ChessPieceView(piece);
        pieceView.addMouseListener(mouseListener);
        pieceView.addMouseMotionListener(motionListener);
        return pieceView;
    }
}
