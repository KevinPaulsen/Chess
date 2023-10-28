package chess.model.moves;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import chess.model.Zobrist;
import chess.model.pieces.Piece;

import static chess.ChessCoordinate.*;
import static chess.model.pieces.Piece.*;

public enum CastlingMove implements Movable {
    WHITE_KING_SIDE_CASTLE(E1, G1, H1, F1, WHITE_KING, WHITE_ROOK), WHITE_QUEEN_SIDE_CASTLE(E1, C1,
            A1, D1, WHITE_KING, WHITE_ROOK), BLACK_KING_SIDE_CASTLE(E8, G8, H8, F8, BLACK_KING,
            BLACK_ROOK), BLACK_QUEEN_SIDE_CASTLE(E8, C8, A8, D8, BLACK_KING, BLACK_ROOK);

    private final Piece king;
    private final Piece rook;
    private final ChessCoordinate kingStart;
    private final ChessCoordinate kingEnd;
    private final ChessCoordinate rookStart;
    private final ChessCoordinate rookEnd;
    private final long kingMoveMask;
    private final long rookMoveMask;
    private final long occupancyMoveMask;

    CastlingMove(ChessCoordinate kingStart, ChessCoordinate kingEnd, ChessCoordinate rookStart,
                 ChessCoordinate rookEnd, Piece king, Piece rook) {
        this.king = king;
        this.rook = rook;
        this.kingStart = kingStart;
        this.kingEnd = kingEnd;
        this.rookStart = rookStart;
        this.rookEnd = rookEnd;
        this.kingMoveMask = kingStart.getBitMask() | kingEnd.getBitMask();
        this.rookMoveMask = rookStart.getBitMask() | rookEnd.getBitMask();
        this.occupancyMoveMask = kingMoveMask | rookMoveMask;
    }

    @Override
    public BoardModel.BoardState nextState(BoardModel.BoardState state) {
        long[] pieceMaps = state.pieceMaps().clone();
        long white = state.white();
        long black = state.black();
        long occupied = state.occupied();
        pieceMaps[king.ordinal()] ^= kingMoveMask;
        pieceMaps[rook.ordinal()] ^= rookMoveMask;

        long deltaHash = 0x0L;

        deltaHash = Zobrist.flipPiece(king, kingStart, deltaHash);
        deltaHash = Zobrist.flipPiece(king, kingEnd, deltaHash);
        deltaHash = Zobrist.flipPiece(rook, rookStart, deltaHash);
        deltaHash = Zobrist.flipPiece(rook, rookEnd, deltaHash);

        switch (this) {
            case WHITE_KING_SIDE_CASTLE, WHITE_QUEEN_SIDE_CASTLE -> white ^= occupancyMoveMask;
            case BLACK_KING_SIDE_CASTLE, BLACK_QUEEN_SIDE_CASTLE -> black ^= occupancyMoveMask;
        }

        return new BoardModel.BoardState(pieceMaps, white, black, occupied ^ occupancyMoveMask,
                deltaHash);
    }

    @Override
    public Piece getMovingPiece() {
        return king;
    }


    @Override
    public ChessCoordinate getStartCoordinate() {
        return kingStart;
    }

    @Override
    public ChessCoordinate getEndCoordinate() {
        return kingEnd;
    }

    public ChessCoordinate getRookStart() {
        return rookStart;
    }

    public ChessCoordinate getRookEnd() {
        return rookEnd;
    }

    @Override
    public String toString() {
        return switch (this) {
            case WHITE_KING_SIDE_CASTLE, BLACK_KING_SIDE_CASTLE -> "O-O";
            case WHITE_QUEEN_SIDE_CASTLE, BLACK_QUEEN_SIDE_CASTLE -> "O-O-O";
        };
    }
}
