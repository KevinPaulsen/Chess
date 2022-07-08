package dataextractor;

import chess.ChessCoordinate;
import chess.model.moves.CastlingMove;
import chess.model.moves.Movable;
import chess.model.GameModel;
import chess.model.MoveList;
import chess.model.moves.PromotionMove;
import chess.model.pieces.Piece;

import java.util.Map;

public class GameProcessor {

    private static final int MIN_MOVE = 3;
    private static final int MAX_MOVE = 60;
    private static final byte PIECE = 0;
    private static final byte START_FILE = 1;
    private static final byte START_RANK = 2;
    private static final byte END_FILE = 3;
    private static final byte END_RANK = 4;
    private static final byte EQUALS = 5;
    private static final byte PROMOTION = 6;

    public static void processGame(String moves, Map<Long, PositionData> posToWins) {
        GameModel game = new GameModel(false);
        String[] moveStrings = moves.split("\t");
        String winString = moveStrings[moveStrings.length - 1];

        if (winString.length() > 1) {
            int winner = Integer.parseInt(winString);
            int moveNum = 0;
            for (String stringMove : moveStrings) {
                if (stringMove.length() == 1) {
                    break;
                }
                
                if (game.move(getMove(game, stringMove))) moveNum++;

                if (MIN_MOVE < moveNum && moveNum < MAX_MOVE) {
                    byte[] byteRep = game.getRep();
                    int[] winData = winner == 0 ? new int[]{1, 0} : new int[]{0, 1};
                    PositionData data = new PositionData(byteRep, winData);
                    posToWins.merge(game.getZobristHash(), data, (oldScore, newScore) -> oldScore.addTo(winner));
                }
            }
        }

        while (game.getLastMove() != null) {
            game.undoLastMove();
        }
    }

    private static Movable getMove(GameModel game, String stringMove) {
        MoveList legalMoves = game.getLegalMoves();

        boolean isCastleMove = stringMove.contains("-");
        char[] charRep = isCastleMove ? new char[0] : makeCharRep(stringMove);

        for (Movable move : legalMoves) {
            ChessCoordinate endCoord = move.getEndCoordinate();
            Piece movingPiece = move.getMovingPiece();

            if (isCastleMove) {
                if ((move instanceof CastlingMove)
                        && ((endCoord.getFile() == 6 && stringMove.length() < 5)
                        || (endCoord.getFile() == 2 && stringMove.length() >= 5))) {
                    return move;
                } else {
                    continue;
                }
            }

            if (!movingPiece.isPawn() && charRep[PIECE] != movingPiece.getStringRep().toUpperCase().charAt(0)) {
                continue;
            }

            // Ensure that the end coordinate is correct.
            if (charRep[END_FILE] != endCoord.getCharFile() || charRep[END_RANK] - '0' != endCoord.getCharRank()) {
                continue;
            }

            ChessCoordinate startingCoordinate = move.getStartCoordinate();

            if (charRep[START_FILE] != 0 && charRep[START_FILE] != startingCoordinate.getCharFile()) {
                continue;
            }

            if (charRep[START_RANK] != 0 && charRep[START_RANK] - '0' != startingCoordinate.getCharRank()) {
                continue;
            }

            if (charRep[EQUALS] == '=' && ((PromotionMove) move).getPromotedPiece().getStringRep().toUpperCase().charAt(0) != charRep[PROMOTION]) {
                continue;
            }

            return move;
        }
        return null;
    }

    private static char[] makeCharRep(String stringMove) {
        // [PIECE, START_FILE, START_RANK, END_FILE, END_RANK, EQUALS, PROMOTION_PIECE]
        char[] charRep = new char[7];

        char possibleCheckChar = stringMove.charAt(stringMove.length() - 1);
        int endIdx = stringMove.length() - (possibleCheckChar == '+' || possibleCheckChar == '#' ? 2 : 1);
        if (stringMove.charAt(endIdx - 1) == '=') {
            charRep[EQUALS] = '=';
            charRep[PROMOTION] = stringMove.charAt(endIdx--);
            endIdx--;
        }

        charRep[END_RANK] = stringMove.charAt(endIdx--); // Rank
        charRep[END_FILE] = stringMove.charAt(endIdx--); // File

        if (endIdx >= 0) {
            boolean isPawn = false;
            char currentChar = stringMove.charAt(endIdx);
            if (currentChar == 'x') {
                currentChar = stringMove.charAt(--endIdx);
                isPawn = true;
            }
            if (currentChar <= '8') {
                charRep[START_RANK] = currentChar;
                currentChar = stringMove.charAt(--endIdx);
            }
            if ((isPawn || endIdx > 0) && currentChar >= 'a') {
                charRep[START_FILE] = currentChar;
                endIdx--;
            }
            if (endIdx == 0) {
                charRep[PIECE] = stringMove.charAt(endIdx);
            }
        }

        return charRep;
    }

    record PositionData(byte[] byteRep, int[] score) {
        synchronized PositionData addTo(int winner) {
            score[winner]++;
            return this;
        }
    }
}
