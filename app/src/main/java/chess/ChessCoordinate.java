package chess;

/**
 * This class contains both the integer and normal
 * coordinates for a chess board.
 */
public enum ChessCoordinate {

    A1(0, 0),
    A2(0, 1),
    A3(0, 2),
    A4(0, 3),
    A5(0, 4),
    A6(0, 5),
    A7(0, 6),
    A8(0, 7),
    B1(1, 0),
    B2(1, 1),
    B3(1, 2),
    B4(1, 3),
    B5(1, 4),
    B6(1, 5),
    B7(1, 6),
    B8(1, 7),
    C1(2, 0),
    C2(2, 1),
    C3(2, 2),
    C4(2, 3),
    C5(2, 4),
    C6(2, 5),
    C7(2, 6),
    C8(2, 7),
    D1(3, 0),
    D2(3, 1),
    D3(3, 2),
    D4(3, 3),
    D5(3, 4),
    D6(3, 5),
    D7(3, 6),
    D8(3, 7),
    E1(4, 0),
    E2(4, 1),
    E3(4, 2),
    E4(4, 3),
    E5(4, 4),
    E6(4, 5),
    E7(4, 6),
    E8(4, 7),
    F1(5, 0),
    F2(5, 1),
    F3(5, 2),
    F4(5, 3),
    F5(5, 4),
    F6(5, 5),
    F7(5, 6),
    F8(5, 7),
    G1(6, 0),
    G2(6, 1),
    G3(6, 2),
    G4(6, 3),
    G5(6, 4),
    G6(6, 5),
    G7(6, 6),
    G8(6, 7),
    H1(7, 0),
    H2(7, 1),
    H3(7, 2),
    H4(7, 3),
    H5(7, 4),
    H6(7, 5),
    H7(7, 6),
    H8(7, 7);

    final private char charFile; // Letter from a - h
    final private int charRank; // Number from 1 - 8
    final private int file; // number from 0 - 7
    final private int rank; // number from 0 - 7

    /**
     * The index of this coordinate if a1 is 0, and h8 is 63
     */
    private final int ondDimIndex;

    /**
     * the bit associated with this coordinate
     */
    private final long bitMask;

    /**
     * Creates a Chess Coordinate at the given file and rank.
     *
     * @param file the file of this chessCoordinate 0 indexed (a=0, h=7)
     * @param rank the rank of this chessCoordinate 0 indexed (rank 1 = 0, rank 8 = 7)
     */
    ChessCoordinate(int file, int rank) {
        this.file = (0 <= file && file <= 7) ? file : -1;
        this.rank = (0 <= rank && rank <= 7) ? rank : -1;
        this.charFile = (0 <= file && file <= 7) ? (char) (file + 'a') : '0';
        this.charRank = (0 <= rank && rank <= 7) ? rank + 1 : 0;
        this.ondDimIndex = rank * 8 + file;
        this.bitMask = 1L << ondDimIndex;
    }

    public static boolean isInBounds(int file, int rank) {
        return 0 <= file && file <= 7 && 0 <= rank && rank <= 7;
    }

    public static ChessCoordinate getChessCoordinate(char file, int rank) {
        return getChessCoordinate(file - 'a', rank - 1);
    }

    public static ChessCoordinate getChessCoordinate(int file, int rank) {
        return getChessCoordinate(rank * 8 + file);
    }

    public static ChessCoordinate getChessCoordinate(int oneDimIdx) {
        return switch (oneDimIdx) {
            case 0 -> A1;
            case 1 -> B1;
            case 2 -> C1;
            case 3 -> D1;
            case 4 -> E1;
            case 5 -> F1;
            case 6 -> G1;
            case 7 -> H1;
            case 8 -> A2;
            case 9 -> B2;
            case 10 -> C2;
            case 11 -> D2;
            case 12 -> E2;
            case 13 -> F2;
            case 14 -> G2;
            case 15 -> H2;
            case 16 -> A3;
            case 17 -> B3;
            case 18 -> C3;
            case 19 -> D3;
            case 20 -> E3;
            case 21 -> F3;
            case 22 -> G3;
            case 23 -> H3;
            case 24 -> A4;
            case 25 -> B4;
            case 26 -> C4;
            case 27 -> D4;
            case 28 -> E4;
            case 29 -> F4;
            case 30 -> G4;
            case 31 -> H4;
            case 32 -> A5;
            case 33 -> B5;
            case 34 -> C5;
            case 35 -> D5;
            case 36 -> E5;
            case 37 -> F5;
            case 38 -> G5;
            case 39 -> H5;
            case 40 -> A6;
            case 41 -> B6;
            case 42 -> C6;
            case 43 -> D6;
            case 44 -> E6;
            case 45 -> F6;
            case 46 -> G6;
            case 47 -> H6;
            case 48 -> A7;
            case 49 -> B7;
            case 50 -> C7;
            case 51 -> D7;
            case 52 -> E7;
            case 53 -> F7;
            case 54 -> G7;
            case 55 -> H7;
            case 56 -> A8;
            case 57 -> B8;
            case 58 -> C8;
            case 59 -> D8;
            case 60 -> E8;
            case 61 -> F8;
            case 62 -> G8;
            case 63 -> H8;
            default -> throw new IllegalStateException("Unexpected value: " + oneDimIdx);
        };
    }

    public static ChessCoordinate getChessCoordinate(long bitMask) {
        return getChessCoordinate(Long.numberOfTrailingZeros(bitMask));
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

    public int getOndDimIndex() {
        return ondDimIndex;
    }

    public long getBitMask() {
        return bitMask;
    }

    @Override
    public String toString() {
        return charFile + Integer.toString(charRank);
    }
}

