package main.java;

import java.util.Objects;

/**
 * This class contains both the integer and normal
 * coordinates for a chess board.
 */
public class ChessCoordinate {

    final private char charFile; // Letter from a - h
    final private int charRank; // Number from 1 - 8
    final private int file; // number from 0 - 7
    final private int rank; // number from 0 - 7

    public ChessCoordinate(int file, int rank) {
        this.file = (0 <= file && file <= 7) ? file : -1;
        this.rank = (0 <= rank && rank <= 7) ? rank : -1;
        this.charFile = (0 <= file && file <= 7) ? (char) (file + 97) : '0';
        this.charRank = (0 <= rank && rank <= 7) ? rank + 1 : 0;
    }

    public char getCharFile() {
        return charFile;
    }

    public int getCharRank() {
        return charRank;
    }

    public int getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    public static boolean isInBounds(int file, int rank) {
        return 0 <= file && file <= 7 && 0 <= rank && rank <= 7;
    }

    @Override
    public String toString() {
        return charFile + Integer.toString(charRank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessCoordinate)) return false;
        ChessCoordinate that = (ChessCoordinate) o;
        return charFile == that.charFile && charRank == that.charRank && file == that.file && rank == that.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, rank);
    }
}

