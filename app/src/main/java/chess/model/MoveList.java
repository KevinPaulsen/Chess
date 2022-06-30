package chess.model;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.pieces.Piece;
import chess.util.BitIterator;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static chess.Move.*;
import static chess.model.pieces.Direction.*;
import static chess.model.pieces.Piece.*;

public class MoveList implements Iterable<Move> {

    private final List<MoveData> moveData;
    private final BoardModel board;
    private final Deque<Move> potentialPromotions;

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

    public void add(Piece movingPiece, ChessCoordinate coordinate, long moveMap, Status status) {
        if (moveMap != 0) {
            moveData.add(new MoveData(movingPiece, coordinate, moveMap, status));
        }
    }

    @Override
    public Iterator<Move> iterator() {
        return new MoveIterator();
    }

    public boolean isEmpty() {
        return moveData.isEmpty();
    }

    private class MoveIterator implements Iterator<Move> {

        private int index;
        private MoveData currentMoveData;
        private BitIterator bitIterator;

        public MoveIterator() {
            if (moveData.isEmpty()) return;

            index = 0;
            currentMoveData = moveData.get(index++);
            bitIterator = new BitIterator(currentMoveData.moveMap);
        }

        @Override
        public boolean hasNext() {
            return bitIterator != null && (bitIterator.hasNext() || !potentialPromotions.isEmpty());
        }

        @Override
        public Move next() {
            if (!hasNext()) throw new NoSuchElementException();

            if (potentialPromotions.isEmpty()) {
                Move move = createMove(bitIterator.next());

                if (!bitIterator.hasNext() && index < moveData.size()) {
                    currentMoveData = moveData.get(index++);
                    bitIterator = new BitIterator(currentMoveData.moveMap);
                }
                return move;
            } else {
                return potentialPromotions.pop();
            }
        }

        private Move createMove(ChessCoordinate end) {
            Piece moving = currentMoveData.movingPiece;
            ChessCoordinate start = currentMoveData.coordinate;
            Status status = currentMoveData.status;

            return switch (status) {
                case NORMAL -> {
                    if (board.getPieceOn(end) == null) yield new Move(start, end, null, null);
                    else yield new Move(start, end, end, null);
                }
                case CASTLING -> switch (end) {
                    case G1 -> WHITE_CASTLE_KING_SIDE;
                    case G8 -> BLACK_CASTLE_KING_SIDE;
                    case C1 -> WHITE_CASTLE_QUEEN_SIDE;
                    case C8 -> BLACK_CASTLE_QUEEN_SIDE;
                    default -> throw new IllegalArgumentException("Ending coordinate is not castling end coordinate");
                };
                case PAWN_TAKE_LEFT -> switch (moving) {
                    case WHITE_PAWN -> new Move(DOWN_RIGHT.next(end), end, end, null);
                    case BLACK_PAWN -> new Move(UP_RIGHT.next(end), end, end, null);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_TAKE_LEFT");
                };
                case PAWN_TAKE_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> new Move(DOWN_LEFT.next(end), end, end, null);
                    case BLACK_PAWN -> new Move(UP_LEFT.next(end), end, end, null);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_TAKE_RIGHT");
                };
                case PAWN_FORWARD -> switch (moving) {
                    case WHITE_PAWN -> new Move(DOWN.next(end), end);
                    case BLACK_PAWN -> new Move(UP.next(end), end);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_FORWARD");
                };
                case PAWN_PUSH -> switch (moving) {
                    case WHITE_PAWN -> new Move(DOWN.next(end, 2), end);
                    case BLACK_PAWN -> new Move(UP.next(end, 2), end);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_PUSH");
                };
                case PAWN_PROMOTE_LEFT -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(DOWN_RIGHT.next(end), end, end, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(UP_RIGHT.next(end), end, end, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_PROMOTE_LEFT");
                };
                case PAWN_PROMOTE_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(DOWN_LEFT.next(end), end, end, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(UP_LEFT.next(end), end, end, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_PROMOTE_RIGHT");
                };
                case PAWN_PROMOTE -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(DOWN.next(end), end, null, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(UP.next(end), end, null, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_PROMOTE");
                };
                case EN_PASSANT_LEFT -> switch (moving) {
                    case WHITE_PAWN -> new Move(DOWN_RIGHT.next(end), end, DOWN.next(end), null);
                    case BLACK_PAWN -> new Move(UP_RIGHT.next(end), end, UP.next(end), null);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status EN_PASSANT_LEFT");
                };
                case EN_PASSANT_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> new Move(DOWN_LEFT.next(end), end, DOWN.next(end), null);
                    case BLACK_PAWN -> new Move(UP_LEFT.next(end), end, UP.next(end), null);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status EN_PASSANT_RIGHT");
                };
            };
        }

        private Move makePromotions(ChessCoordinate start, ChessCoordinate end, ChessCoordinate captureStart, Piece knight,
                                    Piece bishop, Piece rook, Piece queen) {
            potentialPromotions.offer(new Move(start, end, captureStart, null, knight));
            potentialPromotions.offer(new Move(start, end, captureStart, null, bishop));
            potentialPromotions.offer(new Move(start, end, captureStart, null, rook));
            return new Move(start, end, captureStart, null, queen);
        }
    }

    private record MoveData(Piece movingPiece, ChessCoordinate coordinate, Long moveMap, Status status) {}

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
}
