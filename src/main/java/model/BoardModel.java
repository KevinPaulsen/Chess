package main.java.model;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.pieces.King;
import main.java.model.pieces.Pawn;
import main.java.model.pieces.Piece;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BoardModel {

    private static final boolean DEBUG_MODE = false;

    // The array the hold all the pieces. They are stored in the format [file][rank]
    private final Piece[][] pieceArray;

    private final Map<String, PieceHolder> whitePieces;
    private final Map<String, PieceHolder> blackPieces;

    private King whiteKing;
    private King blackKing;

    private static final ChessCoordinate[][] chessCoordinates = createChessCoordinates();

    public BoardModel(Piece[][] pieceArray) {
        this.pieceArray = pieceArray.clone();
        this.whitePieces = new HashMap<>();
        this.blackPieces = new HashMap<>();
        initPieces();
        checkRep();
    }

    public BoardModel(BoardModel boardModel) {
        this.whitePieces = new HashMap<>();
        this.blackPieces = new HashMap<>();
        this.pieceArray = boardModel.cloneArray();
        initPieces();
    }

    private Piece[][] cloneArray() {
        Piece[][] pieceArray = new Piece[this.pieceArray.length][this.pieceArray.length];
        for (int file = 0; file < pieceArray.length; file++) {
            for (int rank = 0; rank < pieceArray.length; rank++) {
                Piece clonedPiece = Piece.clone(this.pieceArray[file][rank]);
                pieceArray[file][rank] = clonedPiece;
            }
        }
        return pieceArray;
    }

    public Piece[][] getPieceArray() {
        return pieceArray.clone();
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

            if (getPieceOn(move.getEndingCoordinate()) != null && !move.getInteractingPieceStart().equals(move.getEndingCoordinate())) {
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
        checkRep();
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
        checkRep();
    }

    public boolean kingInCheck(char color) {
        if (color == 'w') {
            return whiteKing.isAttacked(whiteKing.getCoordinate(), this);
        } else {
            return blackKing.isAttacked(blackKing.getCoordinate(), this);
        }
    }

    public Piece getPieceOn(ChessCoordinate coordinate) {
        return coordinate == null ? null : pieceArray[coordinate.getFile()][coordinate.getRank()];
    }

    private void addPiece(Piece piece, ChessCoordinate coordinate, int movesToAdd) {
        if (piece != null && coordinate != null) {
            if (getPieceOn(coordinate) != null) {
                throw new IllegalStateException("Adding a piece to square where piece already exists");
            }

            pieceArray[coordinate.getFile()][coordinate.getRank()] = piece;
            piece.moveTo(coordinate, movesToAdd);
            if (piece.getColor() == 'w') {
                whitePieces.get(Integer.toString(piece.getUniqueIdentifier())).setPiece(piece);
            } else {
                blackPieces.get(Integer.toString(piece.getUniqueIdentifier())).setPiece(piece);
            }
        }
    }

    private void removePiece(Piece piece) {
        if (piece != null) {
            if (piece.getCoordinate() == null || getPieceOn(piece.getCoordinate()) != piece) {
                throw new IllegalStateException("Piece Data is out of sync.");
            }

            pieceArray[piece.getCoordinate().getFile()][piece.getCoordinate().getRank()] = null;
            if (piece.getColor() == 'w') {
                whitePieces.get(Integer.toString(piece.getUniqueIdentifier())).setPiece(null);
            } else {
                blackPieces.get(Integer.toString(piece.getUniqueIdentifier())).setPiece(null);
            }
            piece.moveTo(null, 0);
        }
    }

    public void movePiece(Piece piece, ChessCoordinate endCoordinate, int movesToAdd) {
        if (piece != null) {
            removePiece(piece);
            addPiece(piece, endCoordinate, movesToAdd);
        }
    }

    public static ChessCoordinate getChessCoordinate(int file, int rank) {
        return ChessCoordinate.isInBounds(file, rank) ? chessCoordinates[file][rank] : null;
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
                        whitePieces.put(Integer.toString(piece.getUniqueIdentifier()), new PieceHolder(piece));
                    } else {
                        blackPieces.put(Integer.toString(piece.getUniqueIdentifier()), new PieceHolder(piece));
                    }
                }
            }
        }
    }

    public Map<String, PieceHolder> getWhitePieces() {
        return whitePieces;
    }

    public Map<String, PieceHolder> getBlackPieces() {
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
        return Arrays.equals(pieceArray, that.pieceArray) && Objects.equals(whiteKing, that.whiteKing) && Objects.equals(blackKing, that.blackKing);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(whiteKing, blackKing);
        result = 31 * result + Arrays.hashCode(pieceArray);
        return result;
    }

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

    private void checkRep() {
        if (DEBUG_MODE) {
            if (pieceArray == null || whitePieces == null || blackPieces == null || whiteKing == null || blackKing == null) {
                throw new RuntimeException("Representation is incorrect.");
            }
            for (Piece[] file : pieceArray) {
                for (Piece piece : file) {
                    if (piece != null) {
                        if (!whitePieces.containsKey(Integer.toString(piece.getUniqueIdentifier()))
                                && !blackPieces.containsKey(Integer.toString(piece.getUniqueIdentifier()))) {
                            throw new RuntimeException("Representation is incorrect.");
                        }
                    }
                }
            }
        }
    }

    private int countPieces() {
        int count = 0;
        for (Piece[] file : pieceArray) {
            for (Piece piece : file) {
                if (piece != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public static class PieceHolder {

        Piece piece;

        public PieceHolder(Piece piece) {
            this.piece = piece;
        }

        public Piece getPiece() {
            return piece;
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
        }
    }
}
