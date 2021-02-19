package chess.model.pieces;

import java.util.Set;

public enum Directions {

    DIAGONALS (Set.of(
            new Direction(1, 1),
            new Direction(1, -1),
            new Direction(-1, 1),
            new Direction(-1, -1)
    )),
    STRAIGHTS (Set.of(
            new Direction(1, 0),
            new Direction(-1, 0),
            new Direction(0, 1),
            new Direction(0, -1)
    )),
    KNIGHTS (Set.of(
            new Direction(2, 1),
            new Direction(2, -1),
            new Direction(1, 2),
            new Direction(1, -2),
            new Direction(-1, 2),
            new Direction(-1, -2),
            new Direction(-2, 1),
            new Direction(-2, -1)
    ));

    public final Set<Direction> directions;

    Directions(Set<Direction> directions) {
        this.directions = directions;
    }
}
