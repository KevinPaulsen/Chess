package main.java.view;

import main.java.model.pieces.Bishop;
import main.java.model.pieces.King;
import main.java.model.pieces.Knight;
import main.java.model.pieces.Pawn;
import main.java.model.pieces.Piece;
import main.java.model.pieces.Queen;
import main.java.model.pieces.Rook;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class PieceView extends JLabel {

    public PieceView(Piece piece) {
        super(PieceView.getImage(piece));

        this.setSize(100, 100);
        this.setOpaque(false);
    }

    private static ImageIcon getImage(Piece piece) {
        String path = "";

        if (piece instanceof Pawn) {
            path = (piece.getColor() == (byte) 0) ? "/Users/kevinpaulsen/dev/chess/images/WPawn.png" : "/Users/kevinpaulsen/dev/chess/images/BPawn.png";
        } else if (piece instanceof Knight) {
            path = (piece.getColor() == (byte) 0) ? "/Users/kevinpaulsen/dev/chess/images/WKnight.png" : "/Users/kevinpaulsen/dev/chess/images/BKnight.png";
        } else if (piece instanceof Bishop) {
            path = (piece.getColor() == (byte) 0) ? "/Users/kevinpaulsen/dev/chess/images/WBishop.png" : "/Users/kevinpaulsen/dev/chess/images/BBishop.png";
        } else if (piece instanceof Rook) {
            path = (piece.getColor() == (byte) 0) ? "/Users/kevinpaulsen/dev/chess/images/WRook.png" : "/Users/kevinpaulsen/dev/chess/images/BRook.png";
        } else if (piece instanceof Queen) {
            path = (piece.getColor() == (byte) 0) ? "/Users/kevinpaulsen/dev/chess/images/WQueen.png" : "/Users/kevinpaulsen/dev/chess/images/BQueen.png";
        } else if (piece instanceof King) {
            path = (piece.getColor() == (byte) 0) ? "/Users/kevinpaulsen/dev/chess/images/WKing.png" : "/Users/kevinpaulsen/dev/chess/images/BKing.png";
        }
        return new ImageIcon(path);
    }
}

