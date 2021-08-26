package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.King;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BoardModel {

    private static final boolean DEBUG_MODE = false;

    // The array the hold all the pieces. They are stored in the format [file][rank]
    private final Square[][] squareArray;

    private final Map<String, PieceHolder> whitePieces;
    private final Map<String, PieceHolder> blackPieces;

    private final Set<Square> squaresToUpdate;

    private King whiteKing;
    private King blackKing;

    private static final ChessCoordinate[][] CHESS_COORDINATES = createChessCoordinates();

    public BoardModel(Piece[][] pieceArray) {
        this.squareArray = makeSquareArray(pieceArray);
        this.whitePieces = new HashMap<>();
        this.blackPieces = new HashMap<>();
        this.squaresToUpdate = new HashSet<>();
        initPieces();
        checkRep();
    }

    public BoardModel(BoardModel boardModel) {
        this.whitePieces = new HashMap<>();
        this.blackPieces = new HashMap<>();
        this.squareArray = boardModel.cloneArray();
        this.squaresToUpdate = new HashSet<>();
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

            if (pawnMovesTwice(move)) {
                updateEnPassant((Pawn) move.getMovingPiece(), move.getMovingPiece().getColor());
            }
        }
        checkRep();
    }

    private void updateEnPassant(Pawn pawn, char color) {
        ChessCoordinate coordinate1 = BoardModel.getChessCoordinate(pawn.getCoordinate().getFile() + 1, pawn.getCoordinate().getRank());
        ChessCoordinate coordinate2 = BoardModel.getChessCoordinate(pawn.getCoordinate().getFile() - 1, pawn.getCoordinate().getRank());
        Piece piece1 = getPieceOn(coordinate1);
        Piece piece2 = getPieceOn(coordinate2);

        if (piece1 instanceof Pawn && piece1.getColor() != color) {
            // FIXME
            //((Pawn) piece1).addEnPassant(pawn.getCoordinate().getFile(), pawn);
        }
    }

    private boolean pawnMovesTwice(Move move) {
        return move.getMovingPiece() instanceof Pawn
                && Math.abs(move.getStartingCoordinate().getRank()
                - move.getEndingCoordinate().getRank()) == 2;
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

    public Set<Square> getSquaresToUpdate() {
        return squaresToUpdate;
    }

    public void addAttacker(Piece piece, ChessCoordinate coordinate) {
        squareArray[coordinate.getFile()][coordinate.getRank()].addAttacker(piece);
    }

    public boolean kingInCheck(char color) {
        if (color == 'w') {
            return getSquare(whiteKing.getCoordinate()).numAttackers('b') == 0;
        } else {
            return getSquare(blackKing.getCoordinate()).numAttackers('w') == 0;
        }
    }

    public Piece getPieceOn(ChessCoordinate coordinate) {
        return coordinate == null ? null : squareArray[coordinate.getFile()][coordinate.getRank()].getPiece();
    }

    private void addPiece(Piece piece, ChessCoordinate coordinate, int movesToAdd) {
        // FIXME: UID dependency
        /*if (piece != null && coordinate != null) {
            squareArray[coordinate.getFile()][coordinate.getRank()].setPiece(piece);
            piece.moveTo(coordinate, movesToAdd);
            if (piece.getColor() == 'w') {
                whitePieces.get(Integer.toString(piece.getUniqueIdentifier())).setPiece(piece);
            } else {
                blackPieces.get(Integer.toString(piece.getUniqueIdentifier())).setPiece(piece);
            }
        }//*/
    }

    private void removePiece(Piece piece) {
        // FIXME: UID dependency
        /*if (piece != null) {
            if (piece.getCoordinate() == null || getPieceOn(piece.getCoordinate()) != piece) {
                throw new IllegalStateException("Piece Data is out of sync.");
            }

            squareArray[piece.getCoordinate().getFile()][piece.getCoordinate().getRank()].setPiece(null);
            if (piece.getColor() == 'w') {
                whitePieces.get(Integer.toString(piece.getUniqueIdentifier())).setPiece(null);
            } else {
                blackPieces.get(Integer.toString(piece.getUniqueIdentifier())).setPiece(null);
            }
            piece.moveTo(null, 0);
            // TODO: update pieces
        }//*/
    }

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
        // FIXME: UID dependency
        /*for (Square[] file : squareArray) {
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
                        whitePieces.put(Integer.toString(piece.getUniqueIdentifier()), new PieceHolder(piece));
                    } else {
                        blackPieces.put(Integer.toString(piece.getUniqueIdentifier()), new PieceHolder(piece));
                    }
                }
            }
        }//*/
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

    public static ChessCoordinate[][] getChessCoordinates() {
        return CHESS_COORDINATES;
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

    private void checkRep() {
        // FIXME: UID dependency
        /*if (DEBUG_MODE) {
            if (squareArray == null || whitePieces == null || blackPieces == null || whiteKing == null || blackKing == null) {
                throw new RuntimeException("Representation is incorrect.");
            }
            for (Square[] file : squareArray) {
                for (Square square : file) {
                    Piece piece = square.getPiece();
                    if (piece != null) {
                        if (!whitePieces.containsKey(Integer.toString(piece.getUniqueIdentifier()))
                                && !blackPieces.containsKey(Integer.toString(piece.getUniqueIdentifier()))) {
                            throw new RuntimeException("Representation is incorrect.");
                        }
                    }
                }
            }
        }//*/
    }

    private int countPieces() {
        int count = 0;
        for (Square[] file : squareArray) {
            for (Square square : file) {
                if (square.getPiece() != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public Square getSquare(ChessCoordinate coordinate) {
        return squareArray[coordinate.getFile()][coordinate.getRank()];
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
