package chess.util;

import java.util.Arrays;
import java.util.Iterator;

public class BigFastMap implements Iterable<Long> {

    private static final int LONG_SIZE = 64;

    private long[] map;

    public BigFastMap() {
        this(0L, LONG_SIZE);
    }

    public BigFastMap(long initialValue, int startSize) {
        this.map = new long[calculateSize(startSize)];
    }

    public BigFastMap(long[] map) {
        this.map = map;
    }

    public void flipBit(int bitIdx) {
        while (calculateSize(bitIdx) > map.length) {
            grow();
        }
        int bucket = bitIdx / LONG_SIZE;
        int remainder = bitIdx - bucket * 64;
        map[bucket] ^= 1L << (remainder - 1);
    }

    private void grow() {
        map = Arrays.copyOf(map, map.length + 1);
    }

    private static int calculateSize(int size) {
        return size / LONG_SIZE + 1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (long map : map) {
            builder.append(Long.toBinaryString(map));
        }

        return builder.toString();
    }

    public String toShortString() {
        StringBuilder builder = new StringBuilder();
        for (long bucket : map) {
            builder.append(bucket);
            builder.append(" ");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BigFastMap)) return false;
        BigFastMap that = (BigFastMap) o;
        return Arrays.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(map);
    }

    public int numBytes() {
        return map.length * Long.BYTES;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Long> iterator() {
        return Arrays.stream(map).iterator();
    }
}
