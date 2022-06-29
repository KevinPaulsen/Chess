package chess.model.chessai;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;
import chess.model.GameModel;
import chess.model.pieces.Piece;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static chess.model.pieces.Piece.BLACK_PAWN;
import static chess.model.pieces.Piece.WHITE_PAWN;

public class PieceValueEvaluator implements Evaluator {

    @Override
    public Evaluation evaluate(GameModel game) {
        int whiteScore = 0;
        int blackScore = 0;

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                ChessCoordinate coordinate = ChessCoordinate.getChessCoordinate(file, rank);
                Piece piece = game.getBoard().getPieceOn(coordinate);

                if (piece != null) {
                    int value = Evaluator.getValue(piece);

                    if (piece.getColor() == 'w') {
                        whiteScore += value;
                    } else {
                        blackScore += value;
                    }
                }
            }
        }
        return new Evaluation(whiteScore - blackScore, 0);
    }

    @Override
    public List<Move> getSortedMoves(GameModel game, Move hashMove) {
        List<Move> legalMoves = new ArrayList<>();
        game.getLegalMoves().forEach(legalMoves::add);
        return legalMoves;
    }

    private Comparator<Move> getMoveComparator(BoardModel board) {
        return (move1, move2) -> Float.compare(getMoveVal(board, move1), getMoveVal(board, move2));
    }

    private float getMoveVal(BoardModel board, Move move) {
        float score = 1.0f;

        Piece movingPiece = move.getMovingPiece(board);

        if (move.getInteractingPiece(board) != null) {
            if (move.getInteractingPieceEnd() == null) {
                score *= Math.abs(Evaluator.getValue(movingPiece)
                        - Evaluator.getValue(move.getInteractingPiece(board))) / 100f + 1f;
            }
            score *= 1.5;
        }//*/

        if (!board.isPawn(move.getStartingCoordinate())
                && attackedByPawn(board, move.getEndingCoordinate(), movingPiece.getColor())) {
            score *= 0.5;
        }

        return score;
    }

    private boolean attackedByPawn(BoardModel board, ChessCoordinate coordinate, char color) {
        List<List<ChessCoordinate>> finalPawnCoords = color == 'w' ?
                WHITE_PAWN.getReachableCoordinateMapFrom(coordinate) :
                BLACK_PAWN.getReachableCoordinateMapFrom(coordinate);

        boolean attackedByPawn = false;
        for (int i = 1; i < 3; i++) {
            for (ChessCoordinate searchCoord : finalPawnCoords.get(i)) {
                if (board.getPieceOn(searchCoord) == WHITE_PAWN
                        || board.getPieceOn(searchCoord) == BLACK_PAWN) {
                    attackedByPawn = true;
                    break;
                }
            }
        }

        return attackedByPawn;
    }
}
