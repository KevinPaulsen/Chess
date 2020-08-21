package main.java;

import java.util.Objects;

/**
 * This class contains both the integer and normal
 * coordinates for a chess board.
 */
public class ChessCoordinate {

    private char charColumn; // Letter from a - h
    private int charRow;
    private int column; // number from 0 - 7
    private int row; // number from 0 - 7

    public ChessCoordinate(int column, int row) {
        setColumn(column);
        setRow(row);
    }

    /**
     * @return the charColumn
     */
    public char getCharColumn() {
        return charColumn;
    }

    /**
     * @return the number column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Checks that column is between 0 and 7 inclusive, and sets both
     * the char column, and the integer column. If column is larger than
     * 7, charColumn is set to '0' and column is set to -1.
     *
     * @param column column coordinate ranging from 0 to 7
     */
    public void setColumn(int column) {
        this.charColumn = (0 <= column && column <= 7) ? (char) (column + 65) : '0';
        this.column = (0 <= column && column <= 7) ? column : -1;
    }

    /**
     * @return the row coordinate.
     */
    public int getRow() {
        return row;
    }

    /**
     * Checks to make sure row is between 0 and 7 inclusive, and
     * sets row to that value if true. If false, sets it to -1.
     * @param row row coordinate value ranging from 0 to 7.
     */
    public void setRow(int row) {
        this.row = (0 <= row && row <= 7) ? row : -1;
        this.charRow = (0 <= row && row <= 7) ? row + 1 : 0;
    }

    public boolean isInBounds() {
        return row != -1 && column != -1;
    }

    @Override
    public String toString() {
        return "(" + charColumn + ", " + charRow + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessCoordinate that = (ChessCoordinate) o;
        return charColumn == that.charColumn &&
                charRow == that.charRow &&
                column == that.column &&
                row == that.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(charColumn, charRow, column, row);
    }
}
