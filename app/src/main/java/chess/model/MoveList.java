package chess.model;

import chess.ChessCoordinate;
import chess.model.moves.EnPassantMove;
import chess.model.moves.Movable;
import chess.model.moves.NormalMove;
import chess.model.moves.PromotionMove;
import chess.model.pieces.Piece;
import chess.util.BitIterator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import static chess.model.moves.CastlingMove.*;
import static chess.model.pieces.Piece.*;

public class MoveList implements Iterable<Movable> {

    private final List<MoveData> moveData;
    private final BoardModel board;
    private final Deque<PromotionMove> potentialPromotions;

    public MoveList(BoardModel board) {
        this.moveData = new ArrayList<>();
        this.board = board;
        this.potentialPromotions = new ArrayDeque<>(4);
    }

    public MoveList(MoveList moveList) {
        this.moveData = new ArrayList<>(moveList.moveData);
        this.board = moveList.board;
        this.potentialPromotions = new ArrayDeque<>(moveList.potentialPromotions);
    }

    public void add(Piece movingPiece, long coordinate, long moveMap, Status status) {
        moveData.add(new MoveData(movingPiece, coordinate, moveMap, status));
    }

    @Override
    public @NonNull Iterator<Movable> iterator() {
        return new MoveIterator();
    }

    public boolean isEmpty() {
        return moveData.isEmpty();
    }

    public List<Movable> toList() {
        List<Movable> moves = new ArrayList<>();
        this.forEach(moves::add);
        return moves;
    }

    public int size() {
        int numMoves = 0;

        for (MoveData data : moveData) {
            if (data.status == Status.PAWN_PROMOTE || data.status == Status.PAWN_PROMOTE_RIGHT ||
                    data.status == Status.PAWN_PROMOTE_LEFT) {
                numMoves += 4;
            } else {
                numMoves += Long.bitCount(data.moveMap);
            }
        }

        return numMoves;
    }

    public enum Status {
        NORMAL,
        CASTLING,
        PAWN_FORWARD,
        PAWN_PUSH,
        PAWN_TAKE_LEFT,
        PAWN_TAKE_RIGHT,
        PAWN_PROMOTE,
        PAWN_PROMOTE_LEFT,
        PAWN_PROMOTE_RIGHT,
        EN_PASSANT_RIGHT,
        EN_PASSANT_LEFT
    }

    private record MoveData(Piece movingPiece, long coordinate, long moveMap, Status status) {}

    private class MoveIterator implements Iterator<Movable> {

        private int index;
        private MoveData currentMoveData;
        private BitIterator bitIterator;

        public MoveIterator() {
            if (moveData.isEmpty())
                return;

            index = 0;
            currentMoveData = moveData.get(index++);
            bitIterator = new BitIterator(currentMoveData.moveMap);
        }

        @Override
        public boolean hasNext() {
            return bitIterator != null && (bitIterator.hasNext() || !potentialPromotions.isEmpty());
        }

        @Override
        public Movable next() {
            if (potentialPromotions.isEmpty()) {
                Movable move = createMove(1L << bitIterator.next());

                if (!bitIterator.hasNext() && index < moveData.size()) {
                    currentMoveData = moveData.get(index++);
                    bitIterator = new BitIterator(currentMoveData.moveMap);
                }
                return move;
            } else {
                return potentialPromotions.pop();
            }
        }

        private Movable createMove(long end) {
            Piece moving = currentMoveData.movingPiece;
            Status status = currentMoveData.status;

            return switch (status) {
                case NORMAL -> new NormalMove(moving, currentMoveData.coordinate, end);
                case CASTLING -> switch (ChessCoordinate.getChessCoordinate(end)) {
                    case G1 -> WHITE_KING_SIDE_CASTLE;
                    case G8 -> BLACK_KING_SIDE_CASTLE;
                    case C1 -> WHITE_QUEEN_SIDE_CASTLE;
                    case C8 -> BLACK_QUEEN_SIDE_CASTLE;
                    default -> throw new IllegalArgumentException(
                            "Ending coordinate is not castling end coordinate");
                };
                case PAWN_TAKE_LEFT -> switch (moving) {
                    case WHITE_PAWN -> new NormalMove(WHITE_PAWN, end >>> 7, end);
                    case BLACK_PAWN -> new NormalMove(BLACK_PAWN, end << 9, end);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status PAWN_TAKE_LEFT");
                };
                case PAWN_TAKE_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> new NormalMove(WHITE_PAWN, end >>> 9, end);
                    case BLACK_PAWN -> new NormalMove(BLACK_PAWN, end << 7, end);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status PAWN_TAKE_RIGHT");
                };
                case PAWN_FORWARD -> switch (moving) {
                    case WHITE_PAWN -> new NormalMove(WHITE_PAWN, end >>> 8, end);
                    case BLACK_PAWN -> new NormalMove(BLACK_PAWN, end << 8, end);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status PAWN_FORWARD");
                };
                case PAWN_PUSH -> switch (moving) {
                    case WHITE_PAWN -> new NormalMove(WHITE_PAWN, end >>> 16, end);
                    case BLACK_PAWN -> new NormalMove(BLACK_PAWN, end << 16, end);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status PAWN_PUSH");
                };
                case PAWN_PROMOTE_LEFT -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(end >>> 7, end, WHITE_PAWN, WHITE_KNIGHT,
                                                      WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(end << 9, end, BLACK_PAWN, BLACK_KNIGHT,
                                                      BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status PAWN_PROMOTE_LEFT");
                };
                case PAWN_PROMOTE_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(end >>> 9, end, WHITE_PAWN, WHITE_KNIGHT,
                                                      WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(end << 7, end, BLACK_PAWN, BLACK_KNIGHT,
                                                      BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status PAWN_PROMOTE_RIGHT");
                };
                case PAWN_PROMOTE -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(end >>> 8, end, WHITE_PAWN, WHITE_KNIGHT,
                                                      WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(end << 8, end, BLACK_PAWN, BLACK_KNIGHT,
                                                      BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status PAWN_PROMOTE");
                };
                case EN_PASSANT_LEFT -> switch (moving) {
                    case WHITE_PAWN -> new EnPassantMove(WHITE_PAWN, BLACK_PAWN, end >>> 7, end,
                                                         end >>> 8);
                    case BLACK_PAWN -> new EnPassantMove(BLACK_PAWN, WHITE_PAWN, end << 9, end,
                                                         end << 8);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status EN_PASSANT_LEFT");
                };
                case EN_PASSANT_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> new EnPassantMove(WHITE_PAWN, BLACK_PAWN, end >>> 9, end,
                                                         end >>> 8);
                    case BLACK_PAWN -> new EnPassantMove(BLACK_PAWN, WHITE_PAWN, end << 7, end,
                                                         end << 8);
                    default -> throw new IllegalArgumentException(
                            "Pawn not passed in with status EN_PASSANT_RIGHT");
                };
            };
        }

        private PromotionMove makePromotions(long start, long end, Piece pawn, Piece knight,
                                             Piece bishop, Piece rook, Piece queen) {
            potentialPromotions.offer(new PromotionMove(pawn, knight, start, end));
            potentialPromotions.offer(new PromotionMove(pawn, bishop, start, end));
            potentialPromotions.offer(new PromotionMove(pawn, rook, start, end));
            return new PromotionMove(pawn, queen, start, end);
        }
    }
}
