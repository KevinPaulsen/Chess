package chess.util;

public class FastMap {

    private long map;

    public FastMap() {
        this(0);
    }

    public FastMap(FastMap map) {
        this(map.map);
    }

    public FastMap(long map) {
        this.map = map;
    }

    public void merge(FastMap map) {
        this.map |= map.map;
    }

    public void markSquare(long mask) {
        map |= mask;
    }

    public boolean isMarked(int squareNum) {
        return ((map >> squareNum) & 1) == 1;
    }

    public void clear() {
        map = 0;
    }
}
