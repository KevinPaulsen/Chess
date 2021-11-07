package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class BoardModel {

    private static final ChessCoordinate[] CHESS_COORDINATES = createChessCoordinates();

    // The array that holds all the pieces. They are stored in the format [file][rank]
    private final Piece[] pieceArray;

    private ChessCoordinate whiteKingCoord;
    private ChessCoordinate blackKingCoord;


    public BoardModel(Piece[] pieceArray) {
        this.pieceArray = pieceArray;

        initPieces();
    }

    /**
     * Gets the chess coordinate at the given file and rank. If the requested
     * coordinate does not exist, null is returned.
     *
     * @param file the file of the chess coordinate.
     * @param rank the rank of the chess coordinate.
     * @return the coordinate with the requested file and rank.
     */
    public static ChessCoordinate getChessCoordinate(int file, int rank) {
        return ChessCoordinate.isInBounds(file, rank) ? CHESS_COORDINATES[rank * 8 + file] : null;
    }

    public static ChessCoordinate getChessCoordinate(int oneDimIndex) {
        return ChessCoordinate.isInBounds(oneDimIndex) ? CHESS_COORDINATES[oneDimIndex] : null;
    }

    /**
     * Create a 2D map of the chess coordinates.
     *
     * @return the 2D array of chess coordinates.
     */
    private static ChessCoordinate[] createChessCoordinates() {
        ChessCoordinate[] chessCoordinates = new ChessCoordinate[64];
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                chessCoordinates[rank * 8 + file] = new ChessCoordinate(file, rank);
            }
        }
        return chessCoordinates;
    }

    /**
     * Makes the given move. All pieces will be updated and moved
     * according the information in move. Move is expected to be
     * legal.
     *
     * @param move the move to make. Cannot be null.
     */
    public void move(Move move) {
        if (move != null) {
            if (getPieceOn(move.getEndingCoordinate()) != null
                    && (move.getInteractingPieceStart() == null
                    || !move.getInteractingPieceStart().equals(move.getEndingCoordinate()))) {
                throw new IllegalStateException("This move cannot exist");
            }

            movePiece(move.getInteractingPiece(), move.getInteractingPieceStart(), move.getInteractingPieceEnd());

            if (move.doesPromote()) {
                removePiece(move.getMovingPiece(), move.getStartingCoordinate());
                addPiece(move.getPromotedPiece(), move.getEndingCoordinate());
            } else {
                movePiece(move.getMovingPiece(), move.getStartingCoordinate(), move.getEndingCoordinate());
            }
        }
    }

    /**
     * Undoes the given move.
     *
     * @param move the move to undo.
     */
    public void undoMove(Move move) {
        if (move != null) {
            if (move.doesPromote()) {
                removePiece(move.getPromotedPiece(), move.getEndingCoordinate());
                addPiece(move.getMovingPiece(), move.getStartingCoordinate());
            } else {
                movePiece(move.getMovingPiece(), move.getEndingCoordinate(), move.getStartingCoordinate());
            }

            if (move.getInteractingPiece() != null) {
                if (move.getInteractingPieceEnd() == null) {
                    addPiece(move.getInteractingPiece(), move.getInteractingPieceStart());
                } else {
                    movePiece(move.getInteractingPiece(), move.getInteractingPieceEnd(),
                            move.getInteractingPieceStart());
                }
            }
        }
    }

    /**
     * Gets the piece on the given ChessCoordinate.
     *
     * @param coordinate the coordinate of the requested piece is at.
     * @return the piece on the given coordinate.
     */
    public Piece getPieceOn(ChessCoordinate coordinate) {
        return coordinate == null ? null : pieceArray[coordinate.getOndDimIndex()];
    }

    /**
     * Add piece to the board, and add the given moves to add to the piece.
     *
     * @param piece      the piece to add.
     * @param coordinate the coordinate to put the piece.
     */
    private void addPiece(Piece piece, ChessCoordinate coordinate) {
        if (piece != null && coordinate != null) {
            pieceArray[coordinate.getOndDimIndex()] = piece;
        }
    }

    /**
     * Removes the given piece from the board.
     *
     * @param piece the piece to remove.
     * @param coordinate the coordinate the piece is on.
     */
    private void removePiece(Piece piece, ChessCoordinate coordinate) {
        if (getPieceOn(coordinate) == null) {
            throw new IllegalStateException("Piece Data is out of sync.");
        }
        if (piece != null) {
            pieceArray[coordinate.getOndDimIndex()] = null;
        }
    }

    /**
     * Moves the piece to the given coordinate, and adds the given number of moves
     * to the piece.
     *
     * @param piece         the piece to move.
     * @param startCoord the starting coordinate of the piece.
     * @param endCoord the ending coordinate of the piece.
     */
    public void movePiece(Piece piece, ChessCoordinate startCoord, ChessCoordinate endCoord) {
        if (piece != null) {
            if (endCoord == null) {
                removePiece(piece, startCoord);
            } else {
                if (piece == Piece.WHITE_KING) {
                    whiteKingCoord = endCoord;
                } else if (piece == Piece.BLACK_KING) {
                    blackKingCoord = endCoord;
                }
                pieceArray[startCoord.getOndDimIndex()] = null;
                pieceArray[endCoord.getOndDimIndex()] = piece;
            }
        }
    }

    /**
     * Initialize the pieces, and local references to the relevant pieces.
     */
    private void initPieces() {
        for (int pieceIdx = 0; pieceIdx < pieceArray.length; pieceIdx++) {
            Piece piece = pieceArray[pieceIdx];
            ChessCoordinate coordinate = getChessCoordinate(pieceIdx);
            if (piece != null) {
                if (piece.getColor() == 'w') {
                    if (piece == Piece.WHITE_KING) whiteKingCoord = coordinate;
                } else {
                    if (piece == Piece.BLACK_KING) blackKingCoord = coordinate;
                }
            }
        }
    }

    /**
     * @return the reference to the white king.
     */
    public ChessCoordinate getWhiteKingCoord() {
        return whiteKingCoord;
    }

    /**
     * @return the reference to the black king.
     */
    public ChessCoordinate getBlackKingCoord() {
        return blackKingCoord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardModel)) return false;
        BoardModel that = (BoardModel) o;
        return Arrays.equals(pieceArray, that.pieceArray)
                && Objects.equals(whiteKingCoord, that.whiteKingCoord)
                && Objects.equals(blackKingCoord, that.blackKingCoord);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(whiteKingCoord, blackKingCoord);
        result = 31 * result + Arrays.hashCode(pieceArray);
        return result;
    }

    /**
     * TODO: Remove this function
     *
     * @return the 2D array of pieces.
     */
    public Piece[] getPieceArray() {
        return pieceArray.clone();
    }

    public static Iterator<ChessCoordinate> getCoordinateIterator() {
        return Arrays.stream(CHESS_COORDINATES).iterator();
    }

    /**
     * Prints the board to the console. Used only for debug purposes.
     */
    public void printBoard() {
        for (Piece piece : pieceArray) {
            if (piece == null) {
                System.out.print("  ");
            } else {
                if (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN) {
                    System.out.print("P ");
                } else {
                    System.out.print(piece + " ");
                }
            }
        }
        System.out.println();
    }

}
