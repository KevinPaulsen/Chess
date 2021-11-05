package chess.model.chessai;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.pieces.Bishop;
import chess.model.pieces.BlackPawn;
import chess.model.pieces.Knight;
import chess.model.pieces.Pawn;
import chess.model.pieces.Piece;
import chess.model.pieces.Queen;
import chess.model.pieces.Rook;
import chess.model.pieces.WhitePawn;

import java.util.Comparator;
import java.util.List;

import static chess.model.chessai.Constants.*;

public class PieceValueEvaluator implements Evaluator {

    @Override
    public Evaluation evaluate(GameModel game) {
        int whiteScore = 0;
        int blackScore = 0;

        for (ChessCoordinate coordinate : game.getBoard().getWhitePieces()) {
            whiteScore += getValue(game.getBoard().getPieceOn(coordinate));
        }
        for (ChessCoordinate coordinate : game.getBoard().getBlackPieces()) {
            blackScore += getValue(game.getBoard().getPieceOn(coordinate));
        }
        return new Evaluation(whiteScore - blackScore, 0);
    }

    @Override
    public List<Move> getSortedMoves(GameModel game) {
        List<Move> result = game.getLegalMoves();
        result.sort(getMoveComparator(game.getBoard()));
        return result;
    }

    private Comparator<Move> getMoveComparator(BoardModel board) {
        return (move1, move2) -> Float.compare(getMoveVal(board, move1), getMoveVal(board, move2));
    }

    private float getMoveVal(BoardModel board, Move move) {
        float score = 1.0f;

        if (move.getInteractingPiece() != null) {
            if (move.getInteractingPieceEnd() == null) {
                score *= Math.abs(PieceValueEvaluator.getValue(move.getMovingPiece())
                        - PieceValueEvaluator.getValue(move.getInteractingPiece())) / 100f + 1f;
            }
            score *= 1.5;
        }//*/

        if (!(move.getMovingPiece() instanceof Pawn)
                && attackedByPawn(board, move.getEndingCoordinate(), move.getMovingPiece().getColor())) {
            score *= 0.5;
        }

        return score;
    }

    private boolean attackedByPawn(BoardModel board, ChessCoordinate coordinate, char color) {
        List<List<ChessCoordinate>> finalPawnCoords = color == 'w' ?
                WhitePawn.REACHABLE_COORDINATES_MAP[coordinate.getFile()][coordinate.getRank()] :
                BlackPawn.REACHABLE_COORDINATES_MAP[coordinate.getFile()][coordinate.getRank()];

        boolean attackedByPawn = false;
        for (int i = 1; i < 3; i++) {
            for (ChessCoordinate searchCoord : finalPawnCoords.get(i)) {
                if (board.getPieceOn(searchCoord) instanceof Pawn) {
                    attackedByPawn = true;
                    break;
                }
            }
        }

        return attackedByPawn;
    }

    public static int getValue(Piece piece) {
        int score = 0;

        if (piece instanceof Pawn) {
            score = PAWN_SCORE;
        } else if (piece instanceof Knight) {
            score = KNIGHT_SCORE;
        } else if (piece instanceof Bishop) {
            score = BISHOP_SCORE;
        } else if (piece instanceof Rook) {
            score = ROOK_SCORE;
        } else if (piece instanceof Queen) {
            score = QUEEN_SCORE;
        }

        return score;
    }
}
