package chess.model.moves;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import chess.model.Zobrist;
import chess.model.pieces.Piece;

import static chess.model.pieces.Piece.*;
import static chess.model.pieces.Piece.BLACK_KING;

public interface Movable {

    BoardModel.BoardState nextState(BoardModel.BoardState state);

    Piece getMovingPiece();

    ChessCoordinate getStartCoordinate();

    ChessCoordinate getEndCoordinate();

    static void pieceMapCopy(long[] pieceMaps, long[] nextPieceMaps, long remaining, boolean isWhite) {
        if (isWhite) {
            // Set remove ~end from black pieces
            nextPieceMaps[BLACK_PAWN.ordinal()] = pieceMaps[BLACK_PAWN.ordinal()] & remaining;
            nextPieceMaps[BLACK_KNIGHT.ordinal()] = pieceMaps[BLACK_KNIGHT.ordinal()] & remaining;
            nextPieceMaps[BLACK_BISHOP.ordinal()] = pieceMaps[BLACK_BISHOP.ordinal()] & remaining;
            nextPieceMaps[BLACK_ROOK.ordinal()] = pieceMaps[BLACK_ROOK.ordinal()] & remaining;
            nextPieceMaps[BLACK_QUEEN.ordinal()] = pieceMaps[BLACK_QUEEN.ordinal()] & remaining;

            // Copy over remaining white piece maps
            nextPieceMaps[WHITE_PAWN.ordinal()] = pieceMaps[WHITE_PAWN.ordinal()];
            nextPieceMaps[WHITE_KNIGHT.ordinal()] = pieceMaps[WHITE_KNIGHT.ordinal()];
            nextPieceMaps[WHITE_BISHOP.ordinal()] = pieceMaps[WHITE_BISHOP.ordinal()];
            nextPieceMaps[WHITE_ROOK.ordinal()] = pieceMaps[WHITE_ROOK.ordinal()];
            nextPieceMaps[WHITE_QUEEN.ordinal()] = pieceMaps[WHITE_QUEEN.ordinal()];
        } else {
            // Set remove ~end from white pieces
            nextPieceMaps[WHITE_PAWN.ordinal()] = pieceMaps[WHITE_PAWN.ordinal()] & remaining;
            nextPieceMaps[WHITE_KNIGHT.ordinal()] = pieceMaps[WHITE_KNIGHT.ordinal()] & remaining;
            nextPieceMaps[WHITE_BISHOP.ordinal()] = pieceMaps[WHITE_BISHOP.ordinal()] & remaining;
            nextPieceMaps[WHITE_ROOK.ordinal()] = pieceMaps[WHITE_ROOK.ordinal()] & remaining;
            nextPieceMaps[WHITE_QUEEN.ordinal()] = pieceMaps[WHITE_QUEEN.ordinal()] & remaining;

            // Copy over remaining black piece maps
            nextPieceMaps[BLACK_PAWN.ordinal()] = pieceMaps[BLACK_PAWN.ordinal()];
            nextPieceMaps[BLACK_KNIGHT.ordinal()] = pieceMaps[BLACK_KNIGHT.ordinal()];
            nextPieceMaps[BLACK_BISHOP.ordinal()] = pieceMaps[BLACK_BISHOP.ordinal()];
            nextPieceMaps[BLACK_ROOK.ordinal()] = pieceMaps[BLACK_ROOK.ordinal()];
            nextPieceMaps[BLACK_QUEEN.ordinal()] = pieceMaps[BLACK_QUEEN.ordinal()];
        }
        nextPieceMaps[WHITE_KING.ordinal()] = pieceMaps[WHITE_KING.ordinal()];
        nextPieceMaps[BLACK_KING.ordinal()] = pieceMaps[BLACK_KING.ordinal()];
    }
}
