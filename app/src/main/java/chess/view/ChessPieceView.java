package chess.view;

import chess.model.pieces.Piece;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

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
            case WHITE_QUEEN:
                path += "WQueen.png";
                break;
            case WHITE_ROOK:
                path += "WRook.png";
                break;
            case WHITE_BISHOP:
                path += "WBishop.png";
                break;
            case WHITE_KNIGHT:
                path += "WKnight.png";
                break;
            case WHITE_PAWN:
                path += "WPawn.png";
                break;
            case WHITE_KING:
                path += "WKing.png";
                break;
            case BLACK_QUEEN:
                path += "BQueen.png";
                break;
            case BLACK_ROOK:
                path += "BRook.png";
                break;
            case BLACK_BISHOP:
                path += "BBishop.png";
                break;
            case BLACK_KNIGHT:
                path += "BKnight.png";
                break;
            case BLACK_PAWN:
                path += "BPawn.png";
                break;
            case BLACK_KING:
                path += "BKing.png";
                break;
        }
        return new ImageIcon(path);
    }

    public void promoteTo(Piece promotedPiece) {
        setIcon(getImage(promotedPiece));
    }

    public boolean isOnBoard() {
        return isOnBoard;
    }

    public void capture() {
        setIcon(BLANK_IMAGE);
        isOnBoard = false;
    }
}
