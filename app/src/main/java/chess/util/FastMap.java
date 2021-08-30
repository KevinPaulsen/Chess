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

    public void mergeMask(long mask) {
        map |= mask;
    }

    public void flip(long mask) {
        map = map & ~mask;
    }

    public boolean isMarked(int squareNum) {
        return ((map >> squareNum) & 1) == 1;
    }

    public void clear() {
        map = 0;
    }
}
