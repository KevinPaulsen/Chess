package chess.view;

import chess.model.pieces.Piece;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ChessPieceView extends ImageView {

    private static final double GHOST_OPACITY = 0.5;
    private static final double NORMAL_OPACITY = 1;

    private Piece piece;

    public ChessPieceView(Piece piece, ReadOnlyDoubleProperty widthProperty) {
        //Image image = new Image(new File("images/WPawn.png").toURI().toString());
        Image image = getImage(piece);
        this.setImage(image);
        this.setPreserveRatio(true);
        this.fitWidthProperty().bind(widthProperty);

        this.piece = piece;
    }

    /**
     * Returns the ImageIcon corresponding to the given ChessPiece. A blank
     * ImageIcon is returned if the Piece is null.
     *
     * @param piece the piece to create an ImageIcon for.
     * @return the ImageIcon for the given piece.
     */
    private static Image getImage(Piece piece) {
        String path = "file:images/";

        if (piece == null) {
            return null;
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
        return new Image(path);
    }

    public Piece getPiece() {
        return piece;
    }

    public void promote(Piece promotedPiece) {
        this.piece = promotedPiece;
        this.setImage(getImage(piece));
    }

    public void makeGhost() {
        setOpacity(GHOST_OPACITY);
    }

    public void makeNotGhost() {
        setOpacity(NORMAL_OPACITY);
    }
}
