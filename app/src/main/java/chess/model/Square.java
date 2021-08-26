package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a square on a chess board. Each Square
 * holds information about the piece on the square, as well as which
 * pieces can move to this square and the pieces that can move to this
 * square.
 */
public class Square {

    /**
     * The set of white pieces attacking this square.
     */
    private final Set<Piece> whiteAttackers;
    /**
     * The set of black pieces attacking this square.
     */
    private final Set<Piece> blackAttackers;
    /**
     * The coordinate of this square relative to the board.
     */
    private final ChessCoordinate coordinate;
    /**
     * The piece on this square.
     */
    private Piece piece;

    /**
     * Constructs a new square with the given piece ath the given coordinate.
     *
     * @param piece      the piece on this square
     * @param coordinate the coordinate of this square relative to the board.
     */
    public Square(Piece piece, ChessCoordinate coordinate) {
        this.piece = piece;
        this.coordinate = coordinate;
        this.whiteAttackers = new HashSet<>();
        this.blackAttackers = new HashSet<>();
    }

    /**
     * TODO: FIX THIS
     *
     * @param square
     */
    public Square(Square square) {
        this.piece = square.piece;
        this.coordinate = square.coordinate;
        this.whiteAttackers = copyAttackers(square.whiteAttackers);
        this.blackAttackers = copyAttackers(square.blackAttackers);
    }

    private static Set<Piece> copyAttackers(Set<Piece> attackers) {
        return new HashSet<>(attackers);
    }

    /**
     * Adds the piece to the set of attackers.
     *
     * @param piece the piece that is attacking this square.
     */
    public void addAttacker(Piece piece) {
        if (piece.getColor() == 'w') {
            whiteAttackers.add(piece);
        } else {
            blackAttackers.add(piece);
        }
    }

    /**
     * Removes the piece from the set of attackers.
     *
     * @param piece the piece to remove.
     * @return returns wheather the piece was removed.
     */
    public boolean removeAttacker(Piece piece) {
        boolean didRemove;
        if (piece.getColor() == 'w') {
            didRemove = whiteAttackers.remove(piece);
        } else {
            didRemove = blackAttackers.remove(piece);
        }
        return didRemove;
    }

    /**
     * Updates the legal moves of the piece on this square. It also
     * updates the legal moves of all the pieces that are attacking this
     * square.
     *
     * @param board
     * @param lastMove
     */
    public void update(BoardModel board, Move lastMove) {
        if (piece != null) {
            piece.updateLegalMoves(board, lastMove);
        }

        for (Piece piece : whiteAttackers) {
            piece.updateLegalMoves(board, lastMove);
        }
        for (Piece piece : blackAttackers) {
            piece.updateLegalMoves(board, lastMove);
        }//*/
    }

    public int numAttackers(char color) {
        int isAttacked;
        if (color == 'w') {
            isAttacked = whiteAttackers.size();
        } else {
            isAttacked = blackAttackers.size();
        }
        return isAttacked;
    }

    public Piece getPiece() {
        return piece;
    }

    /**
     * Sets the piece of this square.
     *
     * @param piece the piece to be set on this square.
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * If there is a piece attacking this square that is the opposite color of 'color'
     * then an attacking piece is returned. If there is more than one attacker, an
     * arbitrary one will be returned.
     *
     * @param color the color that is being attacked
     * @return an attacking piece.
     */
    public Piece getAttacker(char color) {
        Piece attacker = null;
        Set<Piece> relevantAttackers = color == 'w' ? blackAttackers : whiteAttackers;
        if (!relevantAttackers.isEmpty()) {
            attacker = relevantAttackers.iterator().next();
        }
        return attacker;
    }

    public Set<Piece> getAttackers() {
        return new HashSet<>() {{
            addAll(whiteAttackers);
            addAll(blackAttackers);
        }};
    }

    /**
     * Returns true if the given color is attacking this square at least once.
     *
     * @param color the color of potential attacker
     * @return if there exists at least one attacker of the given color
     */
    public boolean isAttackedBy(char color) {
        return color == 'w' ? whiteAttackers.size() > 0 : blackAttackers.size() > 0;
    }

    public boolean isAttacking(Piece piece) {
        if (piece.getColor() == 'w') {
            return whiteAttackers.contains(piece);
        } else {
            return blackAttackers.contains(piece);
        }
    }

    public ChessCoordinate getCoordinate() {
        return coordinate;
    }
}
