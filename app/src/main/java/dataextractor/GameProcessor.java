package dataextractor;

import chess.ChessCoordinate;
import chess.Move;
import chess.model.GameModel;
import chess.model.pieces.Piece;
import chess.util.BigFastMap;

import java.util.List;
import java.util.Map;

public class GameProcessor {

    private static final GameModel GAME_MODEL = new GameModel();
    private static final int MIN_MOVE = 3;
    private static final int MAX_MOVE = 60;

    public static void processGame(String moves,
                                   Map<BigFastMap, int[]> posToWins) {
        GameModel game = new GameModel(GAME_MODEL);
        String[] moveStrings = moves.split("\t");
        String winString = moveStrings[moveStrings.length - 1];

        if (!winString.equals("1/1/2")) {
            int winner = winString.length() == 1 ? Integer.parseInt(winString) : -1;
            int moveNum = 0;
            for (String stringMove : moveStrings) {
                if (stringMove.length() == 1) {
                    break;
                }
                Move move = getMove(game, stringMove);
                if (game.move(move)) moveNum++;
                if (MIN_MOVE < moveNum && moveNum < MAX_MOVE) {
                    BigFastMap key = game.getRep();
                    if (posToWins.containsKey(key)) {
                        if (winner != -1) {
                            posToWins.get(key)[winner]++;
                        }
                    } else {
                        int[] winData = winner == 0 ? new int[]{1, 0} : winner == 1 ? new int[]{0, 1} : new int[]{0, 0};
                        posToWins.put(key, winData);
                    }
                }
            }
        }
    }

    private static Move getMove(GameModel game, String stringMove) {
        List<Move> legalMoves = game.getLegalMoves();

        boolean isCastleMove = stringMove.contains("-");
        char[] charRep = isCastleMove ? new char[0] : makeCharRep(stringMove);

        for (Move move : legalMoves) {
            ChessCoordinate endCoord = move.getEndingCoordinate();
            Piece movingPiece = move.getMovingPiece();
            if (isCastleMove) {
                if (move.doesCastle()
                        && ((endCoord.getFile() == 6 && stringMove.length() < 5)
                        || (endCoord.getFile() == 2 && stringMove.length() >= 5))) {
                    return move;
                } else {
                    continue;
                }
            }
            boolean isPawnMove = movingPiece == Piece.WHITE_PAWN || movingPiece == Piece.BLACK_PAWN;

            if (isPawnMove || charRep[0] == movingPiece.getStringRep().toUpperCase().charAt(0)) {
                if (charRep[3] != endCoord.getCharFile()
                        || charRep[4] - 48 != endCoord.getCharRank()) {
                    continue;
                }

                if (charRep[1] != 0) {
                    ChessCoordinate startingCoordinate = move.getStartingCoordinate();
                    if (charRep[1] >= 97) {
                        if (charRep[1] != startingCoordinate.getCharFile()) {
                            continue;
                        }
                    } else {
                        if (charRep[1] - 48 != startingCoordinate.getCharRank()) {
                            continue;
                        }
                    }
                } else if (charRep[5] == '=' && move.getPromotedPiece().getStringRep().toUpperCase().charAt(0)
                        != charRep[6]) {
                    continue;
                }

                return move;
            }
        }
        return null;
    }

    private static char[] makeCharRep(String stringMove) {
        char[] charRep = new char[7];

        char possibleCheckChar = stringMove.charAt(stringMove.length() - 1);
        int endIdx = stringMove.length() - (possibleCheckChar == '+' || possibleCheckChar == '#' ? 2 : 1);
        if (stringMove.charAt(endIdx - 1) == '=') {
            charRep[5] = '=';
            charRep[6] = stringMove.charAt(endIdx--);
            endIdx--;
        }

        charRep[4] = stringMove.charAt(endIdx--); // Rank
        charRep[3] = stringMove.charAt(endIdx--); // File

        if (endIdx >= 0) {
            char currentChar = stringMove.charAt(endIdx);
            if (currentChar == 'x') {
                charRep[2] = currentChar;
                endIdx--;
                currentChar = stringMove.charAt(endIdx);
            }
            if (currentChar >= 97 || currentChar < 57) {
                charRep[1] = currentChar;
                endIdx--;
            }
            if (endIdx == 0) {
                charRep[0] = stringMove.charAt(endIdx);
            }
        }

        return charRep;
    }
}
