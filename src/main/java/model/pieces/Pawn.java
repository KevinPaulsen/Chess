package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.GameModel;
import main.java.model.Move;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;

public class Pawn extends Piece {

    public Pawn(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<Move> getPossibleMoves(GameModel gameModel) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        int direction = (color == 0) ? 1 : -1;

        Move oneForward = new Move(this, null, coordinate, new ChessCoordinate(coordinate.getColumn(),
                coordinate.getRow() + direction), Move.NORMAL_MOVE);

        // Check one and two spaces forward.
        if (oneForward.getEndingCoordinate().isInBounds()
                && (gameModel.getBoardModel().getPieceOnSquare(oneForward.getEndingCoordinate()) == null)) {
            if (oneForward.isLegal(gameModel.getBoardModel())) {
                possibleMoves.add(oneForward);
            }
            if (timesMoved == 0) {
                Move twoForward = new Move(this, null, coordinate, new ChessCoordinate(coordinate.getColumn(),
                        coordinate.getRow() + 2 * direction), Move.NORMAL_MOVE);
                if (twoForward.getEndingCoordinate().isInBounds()
                        && gameModel.getBoardModel().getPieceOnSquare(twoForward.getEndingCoordinate()) == null) {
                    if (twoForward.isLegal(gameModel.getBoardModel())) {
                        possibleMoves.add(twoForward);
                    }
                }
            }
        }
        // Check if can capture
        for (int relativeColumn = -1; relativeColumn <= 1; relativeColumn += 2) {
            ChessCoordinate endingCoordinate = new ChessCoordinate(coordinate.getColumn() + relativeColumn,
                    coordinate.getRow() + direction);
            Move possibleCapture = new Move(this,
                    gameModel.getBoardModel().getPieceOnSquare(endingCoordinate), coordinate, endingCoordinate,
                    Move.NORMAL_MOVE);
            if (possibleCapture.getEndingCoordinate().isInBounds()
                    && gameModel.getBoardModel().getPieceOnSquare(possibleCapture.getEndingCoordinate()) != null
                    && gameModel.getBoardModel().getPieceOnSquare(possibleCapture.getEndingCoordinate()).color != color) {
                if (possibleCapture.isLegal(gameModel.getBoardModel())) {
                    possibleMoves.add(possibleCapture);
                }
            }
        }
        // Check En passant
        ArrayList<Move> moves = gameModel.getMoves();
        if (moves.size() > 0 && moves.get(moves.size() - 1).getMovedPiece() instanceof Pawn) {
            Move lastMove = moves.get(moves.size() - 1);
            if (lastMove.getEndingCoordinate().getRow() == coordinate.getRow()
                    && Math.abs(lastMove.getEndingCoordinate().getColumn() - coordinate.getColumn()) == 1
                    && lastMove.getStartingCoordinate().getRow() == coordinate.getRow() + direction * 2) {
                ChessCoordinate endingCoordinate = new ChessCoordinate(lastMove.getStartingCoordinate().getColumn(),
                        coordinate.getRow() + direction);
                Move possibleEnPassant = new Move(this, gameModel.getBoardModel().getPieceOnSquare(
                        new ChessCoordinate(endingCoordinate.getColumn(), endingCoordinate.getRow() - direction)),
                        coordinate, endingCoordinate, Move.EN_PASSANT);
                if (possibleEnPassant.isLegal(gameModel.getBoardModel())) {
                    possibleMoves.add(possibleEnPassant);
                }
            }
        }

        return possibleMoves;
    }

    @Override
    public String getShortString() {
        return "P";
    }

    @Override
    public String toString() {
        return "Pawn{" +
                "color=" + color +
                ", timesMoved=" + timesMoved +
                ", coordinate=" + coordinate +
                '}';
    }
}
