package main.java.model;

import main.java.ChessCoordinate;
import main.java.Move;
import main.java.model.pieces.King;
import main.java.model.pieces.Pawn;
import main.java.model.pieces.Piece;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BoardModel {

    private static final boolean DEBUG_MODE = false;

    // The array the hold all the pieces. They are stored in the format [file][rank]
    private final Piece[][] pieceArray;

    private final Set<Piece> whitePieces;
    private final Set<Piece> blackPieces;

    private King whiteKing;
    private King blackKing;

    private static final ChessCoordinate[][] chessCoordinates = createChessCoordinates();

    public BoardModel(Piece[][] pieceArray) {
        this.pieceArray = pieceArray;
        this.whitePieces = new HashSet<>();
        this.blackPieces = new HashSet<>();
        initPieces();
        checkRep();
    }

    public Piece[][] getPieceArray() {
        return pieceArray;
    }

    /**
     * Makes the given move. All pieces will be updated and moved
     * according the information in move. Move is expected to be
     * legal.
     *
     * @param move the move to make. Cannot be null.
     */
    public void move(Move move) {
        checkRep();
        if (move != null) {
            // Remove all relevant pieces
            setPiece(move.getStartingCoordinate(), null);
            setPiece(move.getInteractingPieceStart(), null);

            // put moving piece back
            if (move.doesPromote()) {
                if (blackPieces.remove(move.getMovingPiece())) {
                    blackPieces.add(move.getPromotedPiece());
                } else {
                    whitePieces.remove(move.getMovingPiece());
                    whitePieces.add(move.getPromotedPiece());
                }
                setPiece(move.getEndingCoordinate(), move.getPromotedPiece());
                move.getPromotedPiece().moveTo(move.getEndingCoordinate());
            } else {
                setPiece(move.getEndingCoordinate(), move.getMovingPiece());
                move.getMovingPiece().moveTo(move.getEndingCoordinate());
            }

            // Put interacting piece back
            if (move.getInteractingPiece() != null) {
                setPiece(move.getInteractingPieceEnd(), move.getInteractingPiece());
                move.getInteractingPiece().moveTo(move.getInteractingPieceEnd());
                if (move.getInteractingPieceEnd() == null) {
                    whitePieces.remove(move.getInteractingPiece());
                    blackPieces.remove(move.getInteractingPiece());
                }
            }
        }
        checkRep();
    }

    public void undoMove(Move move) {
        checkRep();
        if (move != null) {
            // Remove all relevant Pieces
            setPiece(move.getEndingCoordinate(), null);
            setPiece(move.getInteractingPieceEnd(), null);

            // Put moving piece back to original square
            if (move.doesPromote()) {
                if (whitePieces.remove(move.getPromotedPiece())) {
                    whitePieces.add(move.getMovingPiece());
                } else {
                    blackPieces.remove(move.getPromotedPiece());
                    blackPieces.add(move.getMovingPiece());
                }
            }
            setPiece(move.getStartingCoordinate(), move.getMovingPiece());
            move.getMovingPiece().moveBackTo(move.getStartingCoordinate());

            // Put interacting Piece back to original square
            if (move.getInteractingPiece() != null) {
                setPiece(move.getInteractingPieceStart(), move.getInteractingPiece());
                move.getInteractingPiece().moveBackTo(move.getInteractingPieceStart());
                if (move.getInteractingPieceEnd() == null) {
                    if (move.getInteractingPiece().getColor() == 'w') {
                        whitePieces.add(move.getInteractingPiece());
                    } else {
                        blackPieces.add(move.getInteractingPiece());
                    }
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
        return coordinate != null ? pieceArray[coordinate.getFile()][coordinate.getRank()] : null;
    }

    private void setPiece(ChessCoordinate coordinate, Piece piece) {
        if (coordinate != null) {
            pieceArray[coordinate.getFile()][coordinate.getRank()] = piece;
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
                        whitePieces.add(piece);
                    } else {
                        blackPieces.add(piece);
                    }
                }
            }
        }
    }

    public Set<Piece> getWhitePieces() {
        return whitePieces;
    }

    public Set<Piece> getBlackPieces() {
        return blackPieces;
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
                assert false : "Rep is incorrect";
                throw new RuntimeException("Representation is incorrect.");
            }
            for (Piece[] file : pieceArray) {
                for (Piece piece : file) {
                    if (piece != null) {
                        if (!whitePieces.contains(piece) && !blackPieces.contains(piece)) {
                            throw new RuntimeException("Representation is incorrect.");
                        }
                    }
                }
            }
        }
    }
}
