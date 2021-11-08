package chess.model.chessai;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.GameModel;
import chess.model.pieces.Piece;

import java.util.List;

import static chess.model.chessai.Constants.*;

public interface Evaluator {

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
     * @param game The game to get moves from.
     * @return the list of sorted legal moves.
     */
    List<Move> getSortedMoves(GameModel game);

    static int getValue(Piece piece) {
        int score = 0;

        switch (piece) {
            case WHITE_PAWN:
            case BLACK_PAWN:
                score = PAWN_SCORE;
                break;
            case WHITE_KNIGHT:
            case BLACK_KNIGHT:
                score = KNIGHT_SCORE;
                break;
            case WHITE_BISHOP:
            case BLACK_BISHOP:
                score = BISHOP_SCORE;
                break;
            case WHITE_ROOK:
            case BLACK_ROOK:
                score = ROOK_SCORE;
                break;
            case WHITE_QUEEN:
            case BLACK_QUEEN:
                score = QUEEN_SCORE;
                break;
        }

        return score;
    }
}
