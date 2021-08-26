package chess.model;

import chess.ChessCoordinate;
import chess.model.pieces.Piece;

import java.util.HashSet;
import java.util.Set;

public class Square {

    private Piece piece;
    private final Set<Piece> whiteAttackers;
    private final Set<Piece> blackAttackers;
    private final ChessCoordinate coordinate;

    public Square(Piece piece, ChessCoordinate coordinate) {
        this.piece = piece;
        this.coordinate = coordinate;
        this.whiteAttackers = new HashSet<>();
        this.blackAttackers = new HashSet<>();
    }

    public Square(Square square) {
        this.piece = square.piece;
        this.coordinate = square.coordinate;
        this.whiteAttackers = copyAttackers(square.whiteAttackers);
        this.blackAttackers = copyAttackers(square.blackAttackers);
    }

    private static Set<Piece> copyAttackers(Set<Piece> attackers) {
        Set<Piece> copiedAttackers = new HashSet<>();
        for (Piece piece : attackers) {
            // FIXME: Clone does not exits
            //copiedAttackers.add(Piece.clone(piece));
        }
        return copiedAttackers;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public void addAttacker(Piece piece) {
        if (piece.getColor() == 'w') {
            whiteAttackers.add(piece);
        } else {
            blackAttackers.add(piece);
        }
    }

    public boolean removeAttacker(Piece piece) {
        boolean didRemove = false;
        if (piece.getColor() == 'w') {
            didRemove = whiteAttackers.remove(piece);
        } else {
            didRemove = blackAttackers.remove(piece);
        }
        return didRemove;
    }

    public void update(GameModel game) {
        //FIXME: UpdateAttacking dependency
        /*
        if (piece != null) {
            piece.updateAttacking(game);
        }

        for (Piece piece : Set.copyOf(whiteAttackers)) {
            piece.updateAttacking(game);
        }
        for (Piece piece : Set.copyOf(blackAttackers)) {
            piece.updateAttacking(game);
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
