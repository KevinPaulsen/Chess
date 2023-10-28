package chess.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public void mark(byte bitNum) {
        map |= 0b1L << bitNum;
    }

    public void unmark(byte bitNum) {
        map &= ~(0b1L << bitNum);
    }

    public Collection<Byte> markedIndices() {
        long mapCopy = map;
        List<Byte> indices = new ArrayList<>(8);

        byte count = 0;

        for (int i = Long.numberOfTrailingZeros(mapCopy); mapCopy != 0;
             mapCopy = (mapCopy >>> i) >>> 1, i = Long.numberOfTrailingZeros(mapCopy), count++) {
            indices.add(count += (byte) i);
        }

        return indices;
    }

    public boolean isMarked(int squareNum) {
        return ((map >> squareNum) & 0b1L) == 1;
    }

    public long getMap() {
        return map;
    }

    public void clear() {
        map = 0;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FastMap fastMap))
            return false;
        return map == fastMap.map;
    }

    @Override
    public String toString() {
        return Long.toBinaryString(map);
    }

    public byte getLowestSet() {
        return (byte) Long.numberOfTrailingZeros(map);
    }
}
