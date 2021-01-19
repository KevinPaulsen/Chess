package main.java;

import java.util.Objects;

/**
 * This class contains both the integer and normal
 * coordinates for a chess board.
 */
public class ChessCoordinate {

    private char charFile; // Letter from a - h
    private int charRank; // Number from 1 - 8
    private int file; // number from 0 - 7
    private int rank; // number from 0 - 7

    public ChessCoordinate(int file, int rank) {
        setFile(file);
        setRank(rank);
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

    /**
     * Checks that column is between 0 and 7 inclusive, and sets both
     * the char column, and the integer column. If column is larger than
     * 7, charColumn is set to '0' and column is set to -1.
     *
     * @param file column coordinate ranging from 0 to 7
     */
    public void setFile(int file) {
        this.charFile = (0 <= file && file <= 7) ? (char) (file + 97) : '0';
        this.rank = (0 <= file && file <= 7) ? file : -1;
    }

    /**
     * Checks to make sure row is between 0 and 7 inclusive, and
     * sets row to that value if true. If false, sets it to -1.
     *
     * @param rank row coordinate value ranging from 0 to 7.
     */
    public void setRank(int rank) {
        this.rank = (0 <= rank && rank <= 7) ? rank : -1;
        this.charRank = (0 <= rank && rank <= 7) ? rank + 1 : 0;
    }

    public boolean isInBounds() {
        return rank != -1 && file != -1;
    }

    @Override
    public String toString() {
        return charFile + Integer.toString(charRank);
    }
}

