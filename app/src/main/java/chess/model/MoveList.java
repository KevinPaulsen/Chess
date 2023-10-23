package chess.model;

import chess.ChessCoordinate;
import chess.model.moves.EnPassantMove;
import chess.model.moves.Movable;
import chess.model.moves.NormalMove;
import chess.model.moves.PromotionMove;
import chess.model.pieces.Piece;
import chess.util.BitIterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static chess.model.moves.CastlingMove.*;
import static chess.model.pieces.Direction.*;
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

    public void add(Piece movingPiece, ChessCoordinate coordinate, long moveMap, Status status) {
        moveData.add(new MoveData(movingPiece, coordinate, moveMap, status));
    }

    @Override
    public Iterator<Movable> iterator() {
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

    private class MoveIterator implements Iterator<Movable> {

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
        public Movable next() {
            if (!hasNext()) throw new NoSuchElementException();

            if (potentialPromotions.isEmpty()) {
                Movable move = createMove(bitIterator.next());

                if (!bitIterator.hasNext() && index < moveData.size()) {
                    currentMoveData = moveData.get(index++);
                    bitIterator = new BitIterator(currentMoveData.moveMap);
                }
                return move;
            } else {
                return potentialPromotions.pop();
            }
        }

        private Movable createMove(ChessCoordinate end) {
            Piece moving = currentMoveData.movingPiece;
            ChessCoordinate start = currentMoveData.coordinate;
            Status status = currentMoveData.status;

            return switch (status) {
                case NORMAL -> new NormalMove(moving, start, end);
                case CASTLING -> switch (end) {
                    case G1 -> WHITE_KING_SIDE_CASTLE;
                    case G8 -> BLACK_KING_SIDE_CASTLE;
                    case C1 -> WHITE_QUEEN_SIDE_CASTLE;
                    case C8 -> BLACK_QUEEN_SIDE_CASTLE;
                    default -> throw new IllegalArgumentException("Ending coordinate is not castling end coordinate");
                };
                case PAWN_TAKE_LEFT -> switch (moving) {
                    case WHITE_PAWN -> new NormalMove(WHITE_PAWN, DOWN_RIGHT.next(end), end);
                    case BLACK_PAWN -> new NormalMove(BLACK_PAWN, UP_RIGHT.next(end), end);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_TAKE_LEFT");
                };
                case PAWN_TAKE_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> new NormalMove(WHITE_PAWN, DOWN_LEFT.next(end), end);
                    case BLACK_PAWN -> new NormalMove(BLACK_PAWN, UP_LEFT.next(end), end);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_TAKE_RIGHT");
                };
                case PAWN_FORWARD -> switch (moving) {
                    case WHITE_PAWN -> new NormalMove(WHITE_PAWN, DOWN.next(end), end);
                    case BLACK_PAWN -> new NormalMove(BLACK_PAWN, UP.next(end), end);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_FORWARD");
                };
                case PAWN_PUSH -> switch (moving) {
                    case WHITE_PAWN -> new NormalMove(WHITE_PAWN, DOWN.next(end, 2), end);
                    case BLACK_PAWN -> new NormalMove(BLACK_PAWN, UP.next(end, 2), end);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_PUSH");
                };
                case PAWN_PROMOTE_LEFT -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(DOWN_RIGHT.next(end), end, WHITE_PAWN, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(UP_RIGHT.next(end), end, BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_PROMOTE_LEFT");
                };
                case PAWN_PROMOTE_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(DOWN_LEFT.next(end), end, WHITE_PAWN, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(UP_LEFT.next(end), end, BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_PROMOTE_RIGHT");
                };
                case PAWN_PROMOTE -> switch (moving) {
                    case WHITE_PAWN -> makePromotions(DOWN.next(end), end, WHITE_PAWN, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN);
                    case BLACK_PAWN -> makePromotions(UP.next(end), end, BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN);
                    default -> throw new IllegalArgumentException("Pawn not passed in with status PAWN_PROMOTE");
                };
                case EN_PASSANT_LEFT -> switch (moving) {
                    case WHITE_PAWN -> new EnPassantMove(WHITE_PAWN, BLACK_PAWN, DOWN_RIGHT.next(end), end, DOWN.next(end));
                    case BLACK_PAWN -> new EnPassantMove(BLACK_PAWN, WHITE_PAWN, UP_RIGHT.next(end), end, UP.next(end));
                    default -> throw new IllegalArgumentException("Pawn not passed in with status EN_PASSANT_LEFT");
                };
                case EN_PASSANT_RIGHT -> switch (moving) {
                    case WHITE_PAWN -> new EnPassantMove(WHITE_PAWN, BLACK_PAWN, DOWN_LEFT.next(end), end, DOWN.next(end));
                    case BLACK_PAWN -> new EnPassantMove(BLACK_PAWN, WHITE_PAWN, UP_LEFT.next(end), end, UP.next(end));
                    default -> throw new IllegalArgumentException("Pawn not passed in with status EN_PASSANT_RIGHT");
                };
            };
        }

        private PromotionMove makePromotions(ChessCoordinate start, ChessCoordinate end, Piece pawn,
                                             Piece knight, Piece bishop, Piece rook, Piece queen) {
            potentialPromotions.offer(new PromotionMove(pawn, knight, start, end));
            potentialPromotions.offer(new PromotionMove(pawn, bishop, start, end));
            potentialPromotions.offer(new PromotionMove(pawn, rook, start, end));
            return new PromotionMove(pawn, queen, start, end);
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
