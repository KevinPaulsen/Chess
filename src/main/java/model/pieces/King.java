package main.java.model.pieces;

import main.java.ChessCoordinate;
import main.java.model.BoardModel;
import main.java.model.GameModel;
import main.java.model.moves.CastleMove;
import main.java.model.moves.Move;

import java.util.ArrayList;

public class King extends Piece {

    private boolean isAttacked = false;

    public King(byte color, ChessCoordinate coordinate) {
        super(color, coordinate);
    }

    @Override
    public ArrayList<Move> getPossibleMoves(GameModel gameModel) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        if (gameModel.getTurn() % 2 != color) {
            return possibleMoves;
        }

        // Loop through each direction
        for (int relativeRow = -1; relativeRow <= 1; relativeRow++) {
            for (int relativeCol = -1; relativeCol <= 1; relativeCol++) {
                if (relativeRow == 0 && relativeCol == 0) {
                    continue;
                }
                ChessCoordinate endingCoordinate = new ChessCoordinate(coordinate.getColumn() + relativeCol,
                        coordinate.getRow() + relativeRow);

                // Create possible move with this piece, this starting coordinate, and +1 in the current direction
                Move possibleMove = new Move(this,
                        gameModel.getBoardModel().getPieceOnSquare(endingCoordinate), coordinate, endingCoordinate);
                if (possibleMove.getEndingCoordinate().isInBounds()
                        && (gameModel.getBoardModel().getPieceOnSquare(possibleMove.getEndingCoordinate()) == null
                        || gameModel.getBoardModel().getPieceOnSquare(possibleMove.getEndingCoordinate()).getColor() != color)) {
                    if (possibleMove.isLegal(gameModel.getBoardModel())) {
                        possibleMoves.add(possibleMove);
                    }
                }
            }
        }
        // Check if can Castle
        if (timesMoved == 0) {
            BoardModel model = gameModel.getBoardModel();
            // Check Left
            if (model.getPieceOnSquare(new ChessCoordinate(0, coordinate.getRow())) != null
                    && model.getPieceOnSquare(new ChessCoordinate(0, coordinate.getRow())).timesMoved == 0
                    && model.getPieceOnSquare(new ChessCoordinate(1, coordinate.getRow())) == null
                    && model.getPieceOnSquare(new ChessCoordinate(2, coordinate.getRow())) == null
                    && model.getPieceOnSquare(new ChessCoordinate(3, coordinate.getRow())) == null) {
                if (!isInCheck(new ChessCoordinate(3, coordinate.getRow()), gameModel.getBoardModel())
                        && !isInCheck(new ChessCoordinate(4, coordinate.getRow()), gameModel.getBoardModel())) {
                    ChessCoordinate endingCoordinate = new ChessCoordinate(2, coordinate.getRow());
                    Move possibleMove = new CastleMove(this,
                            gameModel.getBoardModel().getPieceOnSquare(endingCoordinate), coordinate, endingCoordinate);
                    if (possibleMove.isLegal(gameModel.getBoardModel())) {
                        possibleMoves.add(possibleMove);
                    }
                }
            }
            // Check Right
            if (model.getPieceOnSquare(new ChessCoordinate(7, coordinate.getRow())) != null
                    && model.getPieceOnSquare(new ChessCoordinate(7, coordinate.getRow())).timesMoved == 0
                    && model.getPieceOnSquare(new ChessCoordinate(6, coordinate.getRow())) == null
                    && model.getPieceOnSquare(new ChessCoordinate(5, coordinate.getRow())) == null) {
                if (!isInCheck(new ChessCoordinate(4, coordinate.getRow()), gameModel.getBoardModel())
                        && !isInCheck(new ChessCoordinate(5, coordinate.getRow()), gameModel.getBoardModel())) {
                    ChessCoordinate endingCoordinate = new ChessCoordinate(6, coordinate.getRow());
                    Move possibleMove = new CastleMove(this,
                            gameModel.getBoardModel().getPieceOnSquare(endingCoordinate), coordinate, endingCoordinate);
                    if (possibleMove.isLegal(gameModel.getBoardModel())) {
                        possibleMoves.add(possibleMove);
                    }
                }
            }
        }

        return possibleMoves;
    }

    @Override
    public double getValue() {
        return 0;
    }

    public boolean isInCheck(ChessCoordinate possiblyAttackedCoordinate, BoardModel boardModel) {
        // Check if a King Queen Rook or Bishop is attacking coordinate
        for (int colDirection = -1; colDirection <= 1; colDirection++) {
            for (int rowDirection = -1; rowDirection <= 1; rowDirection++) {
                if (colDirection == 0 && rowDirection == 0) {
                    continue;
                }
                int distance = 1;

                ChessCoordinate possibleAttacker = new ChessCoordinate(
                        possiblyAttackedCoordinate.getColumn() + colDirection,
                        possiblyAttackedCoordinate.getRow() + rowDirection);
                while (possibleAttacker.isInBounds()) {
                    Piece attackingPiece = boardModel.getPieceOnSquare(possibleAttacker);
                    if (attackingPiece != null) {
                        if (attackingPiece.getColor() != color && (attackingPiece instanceof Queen
                                || (attackingPiece instanceof Rook && Math.abs(colDirection + rowDirection) == 1)
                                || (attackingPiece instanceof Bishop && Math.abs(colDirection + rowDirection) % 2 == 0)
                                || (distance == 1 && attackingPiece instanceof King))) {
                            return true;
                        } else {
                            break;
                        }
                    }
                    possibleAttacker = new ChessCoordinate(possibleAttacker.getColumn() + colDirection,
                            possibleAttacker.getRow() + rowDirection);
                    distance++;
                }
            }
        }

        // Check knight
        for (int relativeCol = -2; relativeCol <= 2; relativeCol++) {
            if (relativeCol == 0) {
                continue;
            }

            Piece possibleKnightUp = boardModel.getPieceOnSquare(new ChessCoordinate(
                    possiblyAttackedCoordinate.getColumn() + relativeCol,
                    possiblyAttackedCoordinate.getRow() + (3 - Math.abs(relativeCol))));
            Piece possibleKnightDown = boardModel.getPieceOnSquare(new ChessCoordinate(
                    possiblyAttackedCoordinate.getColumn() + relativeCol,
                    possiblyAttackedCoordinate.getRow() - (3 - Math.abs(relativeCol))));
            if ((possibleKnightUp != null && possibleKnightUp.getColor() != color && possibleKnightUp instanceof Knight)
                    || (possibleKnightDown != null && possibleKnightDown.getColor() != color
                    && possibleKnightDown instanceof  Knight)) {
                return true;
            }
        }

        // Check pawn
        int direction = (color == (byte) 0) ? 1 : -1;
        Piece pawnUpRight = boardModel.getPieceOnSquare(
                new ChessCoordinate(possiblyAttackedCoordinate.getColumn() - 1,
                        possiblyAttackedCoordinate.getRow() + direction));
        Piece pawnUpLeft = boardModel.getPieceOnSquare(
                new ChessCoordinate(possiblyAttackedCoordinate.getColumn() + 1,
                        possiblyAttackedCoordinate.getRow() + direction));
        return (pawnUpRight != null && pawnUpRight.getColor() != color && pawnUpRight instanceof Pawn)
                || (pawnUpLeft != null && pawnUpLeft.getColor() != color && pawnUpLeft instanceof Pawn);
    }

    public void updateAttacked(BoardModel boardModel, Move move) {
        isAttacked = isInCheck(coordinate, boardModel);
        boardModel.updateKingColorSquare(move, this);
    }

    public boolean isAttacked() {
        return isAttacked;
    }

    @Override
    public String getShortString() {
        return "K";
    }

    @Override
    public String toString() {
        return "King{" +
                "isAttacked=" + isAttacked +
                ", color=" + color +
                ", timesMoved=" + timesMoved +
                ", coordinate=" + coordinate +
                '}';
    }
}
