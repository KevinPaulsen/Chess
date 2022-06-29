package chess.util;

import chess.ChessCoordinate;

import java.util.Iterator;

public class BitIterator implements Iterator<ChessCoordinate> {

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
    public ChessCoordinate next() {
        ChessCoordinate nextCoordinate = ChessCoordinate.getChessCoordinate(index - 1);
        int difference = Long.numberOfTrailingZeros(bits) + 1;
        index += difference;
        bits >>>= difference;
        return nextCoordinate;
    }
}
