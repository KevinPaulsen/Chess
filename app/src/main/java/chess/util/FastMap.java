package chess.util;

/**
 * This Class represents a user-friendly bit mask.
 */
public class FastMap {

    /**
     * The map that holds the info
     */
    private long map;

    /**
     * Create a map with all values 0.
     */
    public FastMap() {
        this(0);
    }

    /**
     * Create a map that has the initial map.
     *
     * @param map the initial state of this map.
     */
    public FastMap(long map) {
        this.map = map;
    }

    /**
     * Merge the given map with this map. A logical or is
     * done with this map. This map will change its data, but
     * the given map will remain unchanged.
     *
     * @param map the map to merge with this one.
     */
    public void merge(FastMap map) {
        this.map |= map.map;
    }

    /**
     * Merge the given mask with this one. A logical or is
     * done.
     *
     * @param mask the mask to merge with this one.
     */
    public void mergeMask(long mask) {
        map |= mask;
    }

    /**
     * Flip a particular bit, the given mask should have only one
     * bit flipped.
     *
     * @param mask the mask which contains the bit to flip.
     */
    public void clearMask(long mask) {
        map &= ~mask;
    }

    public void flip(long mask) {
        map ^= mask;
    }

    public boolean isMarked(int squareNum) {
        return ((map >> squareNum) & 1) == 1;
    }

    public long getMap() {
        return map;
    }

    public void clear() {
        map = 0;
    }

    @Override
    public String toString() {
        return Long.toBinaryString(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FastMap)) return false;
        FastMap fastMap = (FastMap) o;
        return map == fastMap.map;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(map);
    }
}
