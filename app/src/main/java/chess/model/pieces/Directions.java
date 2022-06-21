package chess.model.pieces;

import java.util.Iterator;
import java.util.Set;

import static chess.model.pieces.Direction.*;

public enum Directions implements Iterable<Direction> {

    DIAGONALS(Set.of(
            UP_LEFT,
            UP_RIGHT,
            DOWN_LEFT,
            DOWN_RIGHT
    )),
    STRAIGHTS(Set.of(
            LEFT,
            RIGHT,
            UP,
            DOWN
    )),
    KNIGHTS(Set.of(
            UP_UP_RIGHT,
            UP_UP_LEFT,
            UP_LEFT_LEFT,
            DOWN_LEFT_LEFT,
            DOWN_DOWN_LEFT,
            DOWN_DOWN_RIGHT,
            DOWN_RIGHT_RIGHT,
            UP_RIGHT_RIGHT
    )),
    VERTICAL(Set.of(
            UP,
            DOWN
    )),
    LATERAL(Set.of(
            RIGHT,
            LEFT
    )),
    ALL_DIRECTIONS(Set.of(
            UP,
            UP_LEFT,
            LEFT,
            DOWN_LEFT,
            DOWN,
            DOWN_RIGHT,
            RIGHT,
            UP_RIGHT
    ));

    public final Set<Direction> directions;

    Directions(Set<Direction> directions) {
        this.directions = directions;
    }

    @Override
    public Iterator<Direction> iterator() {
        return directions.iterator();
    }
}
