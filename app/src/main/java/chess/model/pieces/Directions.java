package chess.model.pieces;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static chess.model.pieces.Direction.*;

public enum Directions implements Iterable<Direction> {

    DIAGONALS(List.of(
            UP_LEFT,
            UP_RIGHT,
            DOWN_LEFT,
            DOWN_RIGHT
    )),
    DIAGONAL_COMPLEMENTS(List.of(
            UP_LEFT,
            UP_RIGHT
    )),
    STRAIGHT_COMPLEMENTS(List.of(
            UP,
            RIGHT
    )),
    STRAIGHTS(List.of(
            LEFT,
            RIGHT,
            UP,
            DOWN
    )),
    KNIGHTS(List.of(
            UP_UP_RIGHT,
            UP_UP_LEFT,
            UP_LEFT_LEFT,
            DOWN_LEFT_LEFT,
            DOWN_DOWN_LEFT,
            DOWN_DOWN_RIGHT,
            DOWN_RIGHT_RIGHT,
            UP_RIGHT_RIGHT
    )),
    VERTICAL(List.of(
            UP,
            DOWN
    )),
    LATERAL(List.of(
            RIGHT,
            LEFT
    )),
    ALL_DIRECTIONS(List.of(
            UP,
            UP_LEFT,
            LEFT,
            DOWN_LEFT,
            DOWN,
            DOWN_RIGHT,
            RIGHT,
            UP_RIGHT
    ));

    public final List<Direction> directions;

    Directions(List<Direction> directions) {
        this.directions = directions;
    }

    @Override
    public Iterator<Direction> iterator() {
        return directions.iterator();
    }
}
