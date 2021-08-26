package chess.model.pieces;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.BoardModel;

import java.util.Set;

/**
 * This is an implementation of Piece. This class hold the information
 * for how a King moves. This class can also calculate if a given chessCoordinate
 * is attacked by the opposite color.
 */
public class King extends Piece {

    /**
     * Reference to right direction
     */
    private static final Direction RIGHT = new Direction(0, 1);

    /**
     * Reference to right direction
     */
    private static final Direction LEFT = new Direction(0, -1);

    /**
     * Creates a new king with the given color and coordinate.
     *
     * @param color the given color
     * @param coordinate the given coordinate
     */
    public King(char color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    /**
     * Returns the set of all legal moves this piece can make.
     *
     * @param board    the board this piece is on.
     * @param lastMove the last made move.
     * @return the set of all legal moves this piece can make.
     */
    @Override
    public Set<Move> updateLegalMoves(BoardModel board, Move lastMove) {
        moves.clear();
        attackingCoords.clear();

        for (Direction direction : Directions.ALL_DIRECTIONS.directions) {
            ChessCoordinate coordinate = direction.next(getCoordinate());
            addMove(board, coordinate);
        }

        if (canCastle(board, RIGHT)) {
            ChessCoordinate kingEnd = BoardModel.getChessCoordinate(coordinate.getFile() + 2, coordinate.getRank());
            ChessCoordinate rookStart = BoardModel.getChessCoordinate(coordinate.getFile() + 3, coordinate.getRank());
            ChessCoordinate rookEnd = BoardModel.getChessCoordinate(coordinate.getFile() + 1, coordinate.getRank());
            moves.add(new Move(kingEnd, this, rookEnd, board.getPieceOn(rookStart)));
        }
        if (canCastle(board, LEFT)) {
            ChessCoordinate kingEnd = BoardModel.getChessCoordinate(coordinate.getFile() - 2, coordinate.getRank());
            ChessCoordinate rookStart = BoardModel.getChessCoordinate(coordinate.getFile() - 4, coordinate.getRank());
            ChessCoordinate rookEnd = BoardModel.getChessCoordinate(coordinate.getFile() - 1, coordinate.getRank());
            moves.add(new Move(kingEnd, this, rookEnd, board.getPieceOn(rookStart)));
        }

        return moves;
    }

    private boolean canCastle(BoardModel board, Direction direction) {
        ChessCoordinate searchCoord = direction.next(coordinate);
        for (int offset = 0; offset < (direction == RIGHT ? 2 : 3); offset++, searchCoord = direction.next(searchCoord)) {
            if (board.getPieceOn(searchCoord) != null && (offset == 2
                    || !board.getSquare(searchCoord).isAttackedBy(oppositeColor()))) {
                return false;
            }
        }
        Piece potentialRook = board.getPieceOn(searchCoord);
        return potentialRook instanceof Rook
                && potentialRook.color == color
                && potentialRook.timesMoved == 0
                && timesMoved == 0;
    }

    @Override
    public String toString() {
        return "K";
    }
}
