package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;

import java.util.Arrays;
import java.util.Objects;

import static chess.model.pieces.Piece.*;

public class BoardModel {

    // The array that holds all the pieces. They are stored in the format [file][rank]
    private final Piece[] pieceArray;

    private ChessCoordinate whiteKingCoord;
    private ChessCoordinate blackKingCoord;

    private final Zobrist zobrist;

    public BoardModel(BoardModel boardModel, Zobrist zobrist) {
        this.pieceArray = Arrays.stream(boardModel.pieceArray)
                .toArray(Piece[]::new);
        whiteKingCoord = boardModel.whiteKingCoord;
        blackKingCoord = boardModel.blackKingCoord;
        this.zobrist = zobrist;
    }

    public BoardModel(String FEN, Zobrist zobrist) {
        this.pieceArray = new Piece[64];
        this.zobrist = zobrist;
        int pieceIdx = 63;
        for (char c : FEN.toCharArray()) {
            if (c == '/') {
                continue;
            }

            if (49 <= c && c <= 56) {
                pieceIdx -= c - 48;
                continue;
            }

            int translatedIdx = pieceIdx + (7 - 2 * (pieceIdx % 8));
            pieceArray[translatedIdx] = switch (c) {
                case 'K' -> WHITE_KING;
                case 'Q' -> WHITE_QUEEN;
                case 'R' -> WHITE_ROOK;
                case 'B' -> WHITE_BISHOP;
                case 'N' -> WHITE_KNIGHT;
                case 'P' -> WHITE_PAWN;
                case 'k' -> BLACK_KING;
                case 'q' -> BLACK_QUEEN;
                case 'r' -> BLACK_ROOK;
                case 'b' -> BLACK_BISHOP;
                case 'n' -> BLACK_KNIGHT;
                case 'p' -> BLACK_PAWN;
                default -> throw new IllegalStateException("Unexpected value: " + c);
            };
            pieceIdx--;
        }

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
            zobrist.addPiece(piece, coordinate);
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
            zobrist.removePiece(piece, coordinate);
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
                if (piece == WHITE_KING) {
                    whiteKingCoord = endCoord;
                } else if (piece == Piece.BLACK_KING) {
                    blackKingCoord = endCoord;
                }
                pieceArray[startCoord.getOndDimIndex()] = null;
                zobrist.removePiece(piece, startCoord);
                pieceArray[endCoord.getOndDimIndex()] = piece;
                zobrist.addPiece(piece, endCoord);
            }
        }
    }

    /**
     * Initialize the pieces, and local references to the relevant pieces.
     */
    private void initPieces() {
        for (int pieceIdx = 0; pieceIdx < pieceArray.length; pieceIdx++) {
            Piece piece = pieceArray[pieceIdx];
            ChessCoordinate coordinate = ChessCoordinate.getChessCoordinate(pieceIdx);
            if (piece != null) {
                if (piece.getColor() == 'w') {
                    if (piece == WHITE_KING) whiteKingCoord = coordinate;
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
        return (int) zobrist.getHashValue();
    }

    /**
     * TODO: Remove this function
     *
     * @return the 2D array of pieces.
     */
    public Piece[] getPieceArray() {
        return pieceArray.clone();
    }
}
