package chess.view;

import chess.model.pieces.Piece;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class is capable of making a JLabel that displays a
 * ChessPiece.
 */
public class ChessPieceView extends JLabel {

    private static final ImageIcon BLANK_IMAGE = new ImageIcon("");

    private boolean isOnBoard;

    /**
     * Constructs a PieceView from the given chessPiece.
     *
     * @param piece any ChessPiece or null for a blank JLabel.
     */
    public ChessPieceView(Piece piece) {
        super();
        setHorizontalAlignment(JLabel.CENTER);
        ImageIcon imageIcon = getImage(piece);
        if (imageIcon.getDescription().length() == 7) {
            isOnBoard = false;
        } else {
            setIcon(imageIcon);
            isOnBoard = true;
        }
    }

    /**
     * Returns the ImageIcon corresponding to the given ChessPiece. A blank
     * ImageIcon is returned if the Piece is null.
     *
     * @param piece the piece to create an ImageIcon for.
     * @return the ImageIcon for the given piece.
     */
    private static ImageIcon getImage(Piece piece) {
        String path = "images/";

        if (piece == null) {
            return BLANK_IMAGE;
        }

        switch (piece) {
            case WHITE_QUEEN -> path += "WQueen.png";
            case WHITE_ROOK -> path += "WRook.png";
            case WHITE_BISHOP -> path += "WBishop.png";
            case WHITE_KNIGHT -> path += "WKnight.png";
            case WHITE_PAWN -> path += "WPawn.png";
            case WHITE_KING -> path += "WKing.png";
            case BLACK_QUEEN -> path += "BQueen.png";
            case BLACK_ROOK -> path += "BRook.png";
            case BLACK_BISHOP -> path += "BBishop.png";
            case BLACK_KNIGHT -> path += "BKnight.png";
            case BLACK_PAWN -> path += "BPawn.png";
            case BLACK_KING -> path += "BKing.png";
        }
        return new ImageIcon(path);
    }

    public void setImage(Piece promotedPiece) {
        setIcon(getImage(promotedPiece));
    }

    public boolean isOnBoard() {
        return isOnBoard;
    }

    public void capture() {
        setIcon(null);
        isOnBoard = false;
    }
}
