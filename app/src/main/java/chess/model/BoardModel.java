package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.King;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BoardModel {

    // The array the hold all the pieces. They are stored in the format [file][rank]
    private final Square[][] squareArray;

    private final Set<Piece> whitePieces;
    private final Set<Piece> blackPieces;

    /**
     * This is the set of all possible moves white can make. These moves are
     * 'sudo-legal' because they do not account for king checks.
     */
    private final List<Move> whiteMoves;

    /**
     * This is the set of all possible moves black can make. These moves are
     * 'sudo-legal' because they do not account for king checks.
     */
    private final List<Move> blackMoves;

    private King whiteKing;
    private King blackKing;

    private static final ChessCoordinate[][] CHESS_COORDINATES = createChessCoordinates();

    public BoardModel(Piece[][] pieceArray, boolean whiteCastle, boolean blackCastle) {
        this.squareArray = makeSquareArray(pieceArray);
        this.whitePieces = new HashSet<>();
        this.blackPieces = new HashSet<>();
        this.whiteMoves = new ArrayList<>();
        this.blackMoves = new ArrayList<>();
        initPieces();

        if (!whiteCastle) {
            ChessCoordinate coordinate = whiteKing.getCoordinate();
            removePiece(whiteKing);
            addPiece(whiteKing, coordinate, 1);
        }
        if (!blackCastle) {
            ChessCoordinate coordinate = blackKing.getCoordinate();
            removePiece(blackKing);
            addPiece(blackKing, coordinate, 1);
        }
    }

    /**
     * TODO: FIX THIS
     * @param boardModel
     */
    public BoardModel(BoardModel boardModel) {
        this.whitePieces = new HashSet<>();
        this.blackPieces = new HashSet<>();
        this.squareArray = boardModel.cloneArray();
        this.whiteMoves = new ArrayList<>();
        this.blackMoves = new ArrayList<>();
        initPieces();
    }

    private Square[][] cloneArray() {
        Square[][] pieceArray = new Square[this.squareArray.length][this.squareArray.length];
        for (int file = 0; file < pieceArray.length; file++) {
            for (int rank = 0; rank < pieceArray.length; rank++) {
                pieceArray[file][rank] = new Square(this.squareArray[file][rank]);
            }
        }
        return pieceArray;
    }

    private Square[][] makeSquareArray(Piece[][] pieceArray) {
        Square[][] squareArray = new Square[pieceArray.length][pieceArray.length];
        for (int file = 0; file < squareArray.length; file++) {
            for (int rank = 0; rank < squareArray.length; rank++) {
                squareArray[file][rank] = new Square(pieceArray[file][rank], getChessCoordinate(file, rank));
            }
        }
        return squareArray;
    }

    public Square[][] getPieceArray() {
        return squareArray.clone();
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

    public Piece getPieceOn(ChessCoordinate coordinate) {
        return coordinate == null ? null : squareArray[coordinate.getFile()][coordinate.getRank()].getPiece();
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
            squareArray[coordinate.getFile()][coordinate.getRank()].setPiece(piece);
            piece.moveTo(coordinate, movesToAdd);
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
            }//*/

            squareArray[piece.getCoordinate().getFile()][piece.getCoordinate().getRank()].setPiece(null);
            if (piece.getColor() == 'w') {
                if (!whitePieces.remove(piece)) {
                    throw new IllegalStateException("Attempted to remove piece that was not held.");
                }
            } else {
                if (!blackPieces.remove(piece)) {
                    throw new IllegalStateException("Attempted to remove piece that was not held.");
                }
            }
            piece.removeFrom(this);
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

    public static ChessCoordinate getChessCoordinate(int file, int rank) {
        return ChessCoordinate.isInBounds(file, rank) ? CHESS_COORDINATES[file][rank] : null;
    }

    private static ChessCoordinate[][] createChessCoordinates() {
        ChessCoordinate[][] chessCoordinates = new ChessCoordinate[8][8];
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                chessCoordinates[file][rank] = new ChessCoordinate(file, rank);
            }
        }
        return chessCoordinates;
    }

    private void initPieces() {
        for (Square[] file : squareArray) {
            for (Square square : file) {
                Piece piece = square.getPiece();
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
        }//*/
    }

    public Set<Piece> getWhitePieces() {
        return whitePieces;
    }

    public Set<Piece> getBlackPieces() {
        return blackPieces;
    }

    public King getWhiteKing() {
        return whiteKing;
    }

    public King getBlackKing() {
        return blackKing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardModel)) return false;
        BoardModel that = (BoardModel) o;
        return Arrays.equals(squareArray, that.squareArray) && Objects.equals(whiteKing, that.whiteKing) && Objects.equals(blackKing, that.blackKing);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(whiteKing, blackKing);
        result = 31 * result + Arrays.hashCode(squareArray);
        return result;
    }

    public void printBoard() {
        for (Square[] pieces : squareArray) {
            for (Square square : pieces) {
                Piece piece = square.getPiece();
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

    public List<Move> getWhiteMoves() {
        return whiteMoves;
    }

    public List<Move> getBlackMoves() {
        return blackMoves;
    }

    public Square getSquare(ChessCoordinate coordinate) {
        return squareArray[coordinate.getFile()][coordinate.getRank()];
    }
}
