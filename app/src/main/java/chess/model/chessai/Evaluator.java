package chess.model.chessai;

import chess.model.moves.Movable;
import chess.model.GameModel;
import chess.model.pieces.Piece;

import java.util.List;

import static chess.model.chessai.Constants.*;

public interface Evaluator {

    static int getValue(Piece piece) {
        return switch (piece) {
            case WHITE_PAWN, BLACK_PAWN -> PAWN_SCORE;
            case WHITE_KNIGHT, BLACK_KNIGHT -> KNIGHT_SCORE;
            case WHITE_BISHOP, BLACK_BISHOP -> BISHOP_SCORE;
            case WHITE_ROOK, BLACK_ROOK -> ROOK_SCORE;
            case WHITE_QUEEN, BLACK_QUEEN -> QUEEN_SCORE;
            default -> 0;
        };
    }

    /**
     * Evaluates the given game and returns an evaluation of this position. The
     * Evaluation will have a depth of 0, and the move will be null.
     *
     * @param game the game to evaluate.
     * @return the evaluation of this game.
     */
    Evaluation evaluate(GameModel game);

    /**
     * Returns a list of all the legal moves in this position, and they are sorted
     * into this evaluators best guess from most-likely to be the best move, to least
     * likely.
     *
     * @param game     The game to get moves from.
     * @param hashMove A previous found best move, null if none exists.
     * @return the list of sorted legal moves.
     */
    List<Movable> getSortedMoves(GameModel game, Movable hashMove);
}
