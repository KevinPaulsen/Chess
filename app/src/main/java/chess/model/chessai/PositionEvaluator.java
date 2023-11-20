package chess.model.chessai;

import chess.ChessCoordinate;
import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.moves.Movable;
import chess.model.moves.PromotionMove;
import chess.model.pieces.Piece;

import java.util.Collections;
import java.util.List;

import static chess.model.GameModel.WHITE;
import static chess.model.chessai.Constants.*;

public class PositionEvaluator implements Evaluator {

    private static final int WIN_SCORE = 1_000_000;
    private static final int SORT_MOVE = 5;

    public PositionEvaluator() {
    }

    /**
     * Evaluates the given game and returns an evaluation of this position. The
     * Evaluation will have a depth of 0, and the move will be null.
     *
     * @param game the game to evaluate.
     * @return the evaluation of this game.
     */
    @Override
    public Evaluation evaluate(GameModel game) {
        int whiteScore = 0;
        int blackScore = 0;

        game.getLegalMoves();
        if (game.getGameOverStatus() == GameModel.LOSER) {
            return new Evaluation(-WIN_SCORE + game.moveNum(), 0);
        } else if (game.getGameOverStatus() == GameModel.DRAW) {
            return new Evaluation(0, 0);
        }

        for (ChessCoordinate coordinate : ChessCoordinate.values()) {
            Piece piece = game.getBoard().getPieceOn(coordinate);

            if (piece != null) {
                double value = Evaluator.getValue(piece);
                value += readTable(piece, coordinate);

                if (piece.getColor() == 'w') {
                    whiteScore += (int) value;
                } else {
                    blackScore += (int) value;
                }
            }
        }
        return new Evaluation(
                game.getTurn() == WHITE ? whiteScore - blackScore : blackScore - whiteScore, 0);
    }

    private static double readTable(Piece piece, ChessCoordinate coordinate) {
        int[] table = switch (piece) {
            case WHITE_PAWN, BLACK_PAWN -> PAWN_VALUE_MAP;
            case WHITE_KNIGHT, BLACK_KNIGHT -> KNIGHT_VALUE_MAP;
            case WHITE_BISHOP, BLACK_BISHOP -> BISHOP_VALUE_MAP;
            case WHITE_ROOK, BLACK_ROOK -> ROOK_VALUE_MAP;
            case WHITE_QUEEN, BLACK_QUEEN -> QUEEN_VALUE_MAP;
            case WHITE_KING, BLACK_KING -> KING_VALUE_MAP;
            default -> throw new IllegalStateException("Piece is not of expected type");
        };

        int index = piece.getColor() == 'w' ? coordinate.getOndDimIndex() :
                (8 * (7 - coordinate.getRank()) + coordinate.getFile());

        return table[index];
    }

    /**
     * Returns a list of all the legal moves in this position, and they are sorted
     * into this evaluators best guess from most-likely to be the best move, to least
     * likely.
     *
     * @param game     The game to get moves from.
     * @param hashMove A previous found best move, null if none exists.
     * @return the list of sorted legal moves.
     */
    @Override
    public List<Movable> getSortedMoves(GameModel game, Movable hashMove) {
        List<Movable> legalMoves = game.getLegalMoves().toList();

        if (legalMoves.size() <= SORT_MOVE) {
            // If the list is small or equal to the required size, no sorting needed
            legalMoves.sort(((o1, o2) -> evaluateMove(o2, game) - evaluateMove(o1, game)));

            if (hashMove != null && legalMoves.remove(hashMove)) {
                legalMoves.add(0, hashMove);
            }//*/

            return legalMoves;
        }

        int[] moveEvaluations = new int[legalMoves.size()];
        for (int index = 0; index < moveEvaluations.length; index++) {
            moveEvaluations[index] = evaluateMove(legalMoves.get(index), game);
        }

        int index = 0;
        if (hashMove != null) {
            legalMoves.remove(hashMove);
            legalMoves.add(0, hashMove);
            index = 1;
        }

        for (; index < SORT_MOVE; index++) {
            int maxIndex = index;
            int maxEval = moveEvaluations[maxIndex];

            for (int searchIndex = index + 1; searchIndex < legalMoves.size(); searchIndex++) {
                int evalCurrent = moveEvaluations[searchIndex];

                if (evalCurrent > maxEval) {
                    maxIndex = searchIndex;
                    maxEval = evalCurrent;
                }
            }
            arraySwap(moveEvaluations, index, maxIndex);
            Collections.swap(legalMoves, index, maxIndex);
        }

        return legalMoves;
    }

    private int evaluateMove(Movable move, GameModel game) {
        int score = 0;
        BoardModel board = game.getBoard();

        Piece movingPiece = move.getMovingPiece();

        Piece capturedPiece = game.getBoard().getPieceOn(move.getEndCoordinate());

        // If the move captures weight moves that capture with lower value pieces higher
        if (capturedPiece != null) {
            score = CAPTURE_BIAS + CAPTURED_PIECE_VALUE_MULTIPLIER * (Evaluator.getValue(
                    capturedPiece)) - Evaluator.getValue(movingPiece);
        }

        score += getCheckAndPinScore(game, move);

        if (move instanceof PromotionMove) {
            switch (((PromotionMove) move).getPromotedPiece()) {
                case WHITE_QUEEN, BLACK_QUEEN -> score += QUEEN_SCORE;
                case WHITE_ROOK, BLACK_ROOK -> score += ROOK_SCORE;
                case WHITE_BISHOP, BLACK_BISHOP -> score += BISHOP_SCORE;
                case WHITE_KNIGHT, BLACK_KNIGHT -> score += KNIGHT_SCORE;
            }
        }

        score += (int) (readTable(movingPiece, move.getEndCoordinate()) - readTable(movingPiece,
                                                                                    move.getStartCoordinate()));

        return score;
    }

    private static void arraySwap(int[] moveEvaluations, int index, int maxIndex) {
        int temp = moveEvaluations[index];
        moveEvaluations[index] = moveEvaluations[maxIndex];
        moveEvaluations[maxIndex] = temp;
    }

    private int getCheckAndPinScore(GameModel game, Movable move) {
        if (game.causesCheck(move)) {
            return 10_000;
        } else if (game.alignsWithKing(move)) {
            return 400;
        } else {
            return 0;
        }
    }
}
