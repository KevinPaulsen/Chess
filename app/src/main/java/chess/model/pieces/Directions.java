package chess.model.pieces;

import java.util.Set;

public enum Directions {

    DIAGONALS(Set.of(
            new Direction(1, 1),
            new Direction(1, -1),
            new Direction(-1, 1),
            new Direction(-1, -1)
    )),
    STRAIGHTS(Set.of(
            new Direction(1, 0),
            new Direction(-1, 0),
            new Direction(0, 1),
            new Direction(0, -1)
    )),
    KNIGHTS(Set.of(
            new Direction(2, 1),
            new Direction(2, -1),
            new Direction(1, 2),
            new Direction(1, -2),
            new Direction(-1, 2),
            new Direction(-1, -2),
            new Direction(-2, 1),
            new Direction(-2, -1)
    )),
    VERTICAL(Set.of(
            new Direction(1, 0),
            new Direction(-1, 0)
    )),
    LATERAL(Set.of(
            new Direction(0, 1),
            new Direction(0, -1)
    )),
    ALL_DIRECTIONS(Set.of(
            new Direction(1, 1),
            new Direction(1, -1),
            new Direction(-1, 1),
            new Direction(-1, -1),
            new Direction(1, 0),
            new Direction(-1, 0),
            new Direction(0, 1),
            new Direction(0, -1)
    ));

    public static final Direction UP = new Direction(1, 0);
    public static final Direction DOWN = new Direction(-1, 0);
    public static final Direction LEFT = new Direction(0, -1);
    public static final Direction RIGHT = new Direction(0, 1);
    public static final Direction UP_RIGHT = new Direction(1, 1);
    public static final Direction UP_LEFT = new Direction(1, -1);
    public static final Direction DOWN_RIGHT = new Direction(-1, 1);
    public static final Direction DOWN_LEFT = new Direction(-1, -1);
    public final Set<Direction> directions;

    Directions(Set<Direction> directions) {
        this.directions = directions;
    }
}
