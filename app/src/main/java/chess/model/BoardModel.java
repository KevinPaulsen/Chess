package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.King;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BoardModel {

    private static final ChessCoordinate[][] CHESS_COORDINATES = createChessCoordinates();

    // The array that holds all the pieces. They are stored in the format [file][rank]
    private final Piece[][] pieceArray;

    private final Set<Piece> whitePieces;
    private final Set<Piece> blackPieces;

    private King whiteKing;
    private King blackKing;


    public BoardModel(Piece[][] pieceArray) {
        this.pieceArray = pieceArray;
        this.whitePieces = new HashSet<>();
        this.blackPieces = new HashSet<>();
        initPieces();
    }

    /**
     * TODO: FIX THIS
     * @param boardModel
     */
    public BoardModel(BoardModel boardModel) {
        this.whitePieces = new HashSet<>();
        this.blackPieces = new HashSet<>();
        this.pieceArray = new Piece[8][8];
        initPieces();
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

            movePiece(move.getInteractingPiece(), move.getInteractingPieceEnd(), 0);

            if (move.doesPromote()) {
                removePiece(move.getMovingPiece());
                addPiece(move.getPromotedPiece(), move.getEndingCoordinate(), 1);
            } else {
                movePiece(move.getMovingPiece(), move.getEndingCoordinate(), 1);
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
                removePiece(move.getPromotedPiece());
                addPiece(move.getMovingPiece(), move.getStartingCoordinate(), -1);
            } else {
                movePiece(move.getMovingPiece(), move.getStartingCoordinate(), -1);
            }

            if (move.getInteractingPiece() != null) {
                if (move.getInteractingPieceEnd() == null) {
                    addPiece(move.getInteractingPiece(), move.getInteractingPieceStart(), 0);
                } else {
                    movePiece(move.getInteractingPiece(), move.getInteractingPieceStart(), 0);
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
        return coordinate == null ? null : pieceArray[coordinate.getFile()][coordinate.getRank()];
    }

    /**
     * Add piece to the board, and add the given moves to add to the piece.
     *
     * @param piece the piece to add.
     * @param coordinate the coordinate to put the piece.
     * @param movesToAdd the number of moves to add to the piecce.
     */
    private void addPiece(Piece piece, ChessCoordinate coordinate, int movesToAdd) {
        if (piece != null && coordinate != null) {
            pieceArray[coordinate.getFile()][coordinate.getRank()] = piece;
            piece.moveTo(coordinate);
            if (piece.getColor() == 'w') {
                if (!whitePieces.add(piece)) {
                    throw new RuntimeException("This piece already exists on the board.");
                }
            } else {
                if (!blackPieces.add(piece)) {
                    throw new RuntimeException("This piece already exists on the board.");
                }
            }
        }
    }

    /**
     * Removes the given piece from the board.
     *
     * @param piece the piece to remove.
     */
    private void removePiece(Piece piece) {
        if (piece != null) {
            if (piece.getCoordinate() == null || getPieceOn(piece.getCoordinate()) != piece) {
                throw new IllegalStateException("Piece Data is out of sync.");
            }

            pieceArray[piece.getCoordinate().getFile()][piece.getCoordinate().getRank()] = null;
            if (piece.getColor() == 'w') {
                if (!whitePieces.remove(piece)) {
                    throw new IllegalStateException("Attempted to remove piece that was not held.");
                }
            } else {
                if (!blackPieces.remove(piece)) {
                    throw new IllegalStateException("Attempted to remove piece that was not held.");
                }
            }
            // FIXME: pieces probably not being removed from sets.
        }
    }

    /**
     * Moves the piece to the given coordinate, and adds the given number of moves
     * to the piece.
     *
     * @param piece the piece to move.
     * @param endCoordinate the end coordinate of the piece.
     * @param movesToAdd the number of moves to add to the piece.
     */
    public void movePiece(Piece piece, ChessCoordinate endCoordinate, int movesToAdd) {
        if (piece != null) {
            removePiece(piece);
            addPiece(piece, endCoordinate, movesToAdd);
        }
    }

    /**
     * Initialize the pieces, and local references to the relevant pieces.
     */
    private void initPieces() {
        for (Piece[] file : pieceArray) {
            for (Piece piece : file) {
                if (piece != null) {
                    if (piece instanceof King) {
                        if (piece.getColor() == 'w') {
                            whiteKing = (King) piece;
                        } else {
                            blackKing = (King) piece;
                        }
                    }
                    if (piece.getColor() == 'w') {
                        if (!whitePieces.add(piece)) {
                            throw new IllegalStateException("Adding piece that already exists on board.");
                        }
                    } else {
                        if (!blackPieces.add(piece)) {
                            throw new IllegalStateException("Adding piece that already exists on board.");
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the set of white pieces on this board.
     */
    public Set<Piece> getWhitePieces() {
        return whitePieces;
    }

    /**
     * @return the set of black pieces on this board.
     */
    public Set<Piece> getBlackPieces() {
        return blackPieces;
    }

    /**
     * @return the reference to the white king.
     */
    public King getWhiteKing() {
        return whiteKing;
    }

    /**
     * @return the reference to the black king.
     */
    public King getBlackKing() {
        return blackKing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardModel)) return false;
        BoardModel that = (BoardModel) o;
        return Arrays.equals(pieceArray, that.pieceArray)
                && Objects.equals(whiteKing, that.whiteKing)
                && Objects.equals(blackKing, that.blackKing);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(whiteKing, blackKing);
        result = 31 * result + Arrays.hashCode(pieceArray);
        return result;
    }

    /**
     * TODO: Remove this function
     *
     * @return the 2D array of pieces.
     */
    public Piece[][] getPieceArray() {
        return pieceArray.clone();
    }

    /**
     * Prints the board to the console. Used only for debug purposes.
     */
    public void printBoard() {
        for (Piece[] pieces : pieceArray) {
            for (Piece piece : pieces) {
                if (piece == null) {
                    System.out.print("  ");
                } else {
                    if (piece instanceof Pawn) {
                        System.out.print("P ");
                    } else {
                        System.out.print(piece.toString() + " ");
                    }
                }
            }
            System.out.println();
        }
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
        return ChessCoordinate.isInBounds(file, rank) ? CHESS_COORDINATES[file][rank] : null;
    }

    /**
     * Create a 2D map of the chess coordinates.
     *
     * @return the 2D array of chess coordinates.
     */
    private static ChessCoordinate[][] createChessCoordinates() {
        ChessCoordinate[][] chessCoordinates = new ChessCoordinate[8][8];
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                chessCoordinates[file][rank] = new ChessCoordinate(file, rank);
            }
        }
        return chessCoordinates;
    }

}
