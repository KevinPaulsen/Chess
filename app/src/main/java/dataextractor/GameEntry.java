package dataextractor;

import chess.util.BigFastMap;

import java.nio.ByteBuffer;

public class GameEntry {
    private final BigFastMap oneHotBoard;
    private int timesReached;
    private int whiteWins;
    private int blackWins;

    public GameEntry(BigFastMap oneHotBoard, int timesReached, int whiteWins, int blackWins) {
        this.oneHotBoard = oneHotBoard;
        this.whiteWins = whiteWins;
        this.blackWins = blackWins;
        this.timesReached = timesReached;
    }

    public static byte[] toByteArray(GameEntry entry) {
        int size = entry.oneHotBoard.numBytes() + (3 * Integer.BYTES);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(entry.timesReached);
        buffer.putInt(entry.whiteWins);
        buffer.putInt(entry.blackWins);
        for (long boardPart : entry.oneHotBoard) {
            buffer.putLong(boardPart);
        }
        return buffer.array();
    }

    public static GameEntry toGameEntry(byte[] buf) {
        ByteBuffer buffer = ByteBuffer.allocate(buf.length);
        buffer.put(buf);
        buffer.flip();
        int timesReached = buffer.getInt();
        int whiteWins = buffer.getInt();
        int blackWins = buffer.getInt();
        long[] map = new long[buffer.remaining() / Long.BYTES];
        int idx = 0;
        while (buffer.hasRemaining()) {
            map[idx++] = buffer.getLong();
        }
        BigFastMap oneHotBoard = new BigFastMap(map);
        return new GameEntry(oneHotBoard, timesReached, whiteWins, blackWins);
    }

    public static byte[] updateValues(byte[] buf, int whiteWins, int blackWins) {
        ByteBuffer buffer = ByteBuffer.allocate(buf.length);
        buffer.put(buf);
        buffer.flip();
        int newTimesReached = buffer.getInt() + 1;
        int newWhiteWins = buffer.getInt() + whiteWins;
        int newBlackWins = buffer.getInt() + blackWins;
        buffer.position(0);
        buffer.putInt(newTimesReached);
        buffer.putInt(newWhiteWins);
        buffer.putInt(newBlackWins);
        return buffer.array();
    }

    public BigFastMap getOneHotBoard() {
        return oneHotBoard;
    }

    public int getWhiteWins() {
        return whiteWins;
    }

    public int getBlackWins() {
        return blackWins;
    }

    public int getTimesReached() {
        return timesReached;
    }

    public void addScore(int whiteWins, int blackWins) {
        this.whiteWins += whiteWins;
        this.blackWins += blackWins;
    }

    public void addTimesReached() {
        this.timesReached++;
    }
}
