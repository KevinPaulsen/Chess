package chess.util;

import java.util.Iterator;

public class BitIterator implements Iterator<Integer> {

    private long bits;
    private int index;

    public BitIterator(long bits) {
        this.index = Long.numberOfTrailingZeros(bits) + 1;
        this.bits = bits >>> index;
    }

    @Override
    public boolean hasNext() {
        return index < 65;
    }

    @Override
    public Integer next() {
        int result = index - 1;
        int difference = Long.numberOfTrailingZeros(bits) + 1;
        index += difference;
        bits >>>= difference;
        return result;
    }
}
