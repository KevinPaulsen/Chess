package chess.model.pieces;

/**
 * This class is an abstract representation of a Piece. Any piece can implement
 * this class. A piece contains information about how the piece moves, and its
 * color, and where it is on the board.
 */
public enum Piece {
    EMPTY('w', "", 0),

    WHITE_KING('w', "K", 1),
    WHITE_QUEEN('w', "Q", 2),
    WHITE_ROOK('w', "R", 3),
    WHITE_BISHOP('w', "B", 4),
    WHITE_KNIGHT('w', "N", 5),
    WHITE_PAWN('w', "P", 6),

    BLACK_KING('b', "k", 7),
    BLACK_QUEEN('b', "q", 8),
    BLACK_ROOK('b', "r", 9),
    BLACK_BISHOP('b', "b", 10),
    BLACK_KNIGHT('b', "n", 11),
    BLACK_PAWN('b', "p", 12);

    /**
     * The color of this piece
     */
    private final char color;

    /**
     * The string that this piece should be represented as in a FEN string.
     */
    private final String stringRep;

    /**
     * The index of this piece.
     */
    private final int uniqueIdx;

    /**
     * Construct a new piece with the given reachableCoordinate map.
     */
    Piece(char color, String stringRep, int uniqueIdx) {
        this.color = color;
        this.stringRep = stringRep;
        this.uniqueIdx = uniqueIdx;
    }

    /**
     * @return the color of this piece.
     */
    public char getColor() {
        return color;
    }

    /**
     * @return the string representation of this piece.
     */
    public String getStringRep() {
        return stringRep;
    }

    public int getUniqueIdx() {
        return uniqueIdx;
    }

    public boolean isPawn() {
        return this == WHITE_PAWN || this == BLACK_PAWN;
    }
}
