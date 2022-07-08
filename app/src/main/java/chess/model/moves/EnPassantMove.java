package chess.model.moves;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import chess.model.Zobrist;
import chess.model.pieces.Piece;

public class EnPassantMove implements Movable {

    private final Piece moving;
    private final Piece captured;
    private final long start;
    private final long end;
    private final long captureStart;

    public EnPassantMove(Piece moving, Piece captured, ChessCoordinate start, ChessCoordinate end,
                         ChessCoordinate captureStart) {
        this.moving = moving;
        this.captured = captured;
        this.start = start.getBitMask();
        this.end = end.getBitMask();
        this.captureStart = captureStart.getBitMask();
    }

    @Override
    public BoardModel.BoardState nextState(BoardModel.BoardState state) {
        long[] pieceMaps = state.pieceMaps().clone();
        long moveMask = start | end;
        long white = state.white();
        long black = state.black();

        if (moving == Piece.WHITE_PAWN) {
            white ^= moveMask;
            black ^= captureStart;
        } else {
            black ^= moveMask;
            white ^= captureStart;
        }

        pieceMaps[moving.ordinal()] ^= moveMask;
        pieceMaps[captured.ordinal()] ^= captureStart;

        long deltaHash = 0x0L;
        deltaHash = Zobrist.flipPiece(moving, ChessCoordinate.getChessCoordinate(start), deltaHash);
        deltaHash = Zobrist.flipPiece(moving, ChessCoordinate.getChessCoordinate(end), deltaHash);
        deltaHash = Zobrist.flipPiece(captured, ChessCoordinate.getChessCoordinate(captureStart), deltaHash);

        return new BoardModel.BoardState(pieceMaps, white, black, white | black, deltaHash);
    }

    @Override
    public Piece getMovingPiece() {
        return moving;
    }

    @Override
    public ChessCoordinate getStartCoordinate() {
        return ChessCoordinate.getChessCoordinate(start);
    }

    @Override
    public ChessCoordinate getEndCoordinate() {
        return ChessCoordinate.getChessCoordinate(end);
    }

    @Override
    public String toString() {
        return ChessCoordinate.getChessCoordinate(start).toString()
                + ChessCoordinate.getChessCoordinate(end).toString();
    }
}
