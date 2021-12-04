package dataextractor;

import chess.Move;
import chess.model.GameModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GameProcessor {

    private static final GameModel GAME_MODEL = new GameModel();

    public static void processGame(String moves,
                                   Map<Long, String> posToFEN,
                                   Map<Long, int[]> posToWins) {
        GameModel game = new GameModel(GAME_MODEL);
        String[] moveStrings = moves.split("\t");
        String winString = moveStrings[moveStrings.length - 1];
        int winner = winString.length() == 1 ? Integer.parseInt(winString) : -1;
        for (String stringMove : moveStrings) {
            if (stringMove.length() == 1) {
                break;
            }
            Move move = getMove(game, stringMove);
            game.move(move);
            if (posToFEN.containsKey(game.getZobristHash())) {
                if (winner != -1) {
                    posToWins.get(game.getZobristHash())[winner]++;
                }
            } else {
                posToFEN.put(game.getZobristHash(), Arrays.toString(game.getRep()));
                int[] winData = winner == 0 ? new int[]{1, 0} : winner == 1 ? new int[]{0, 1} : new int[]{0, 0};
                posToWins.put(game.getZobristHash(), winData);
            }
        }
    }

    private static Move getMove(GameModel game, String stringMove) {
        List<Move> legalMoves = game.getLegalMoves();

        boolean isCastleMove = stringMove.contains("-");
        char[] charRep = isCastleMove ? new char[0] : makeCharRep(stringMove);

        for (Move move : legalMoves) {
            String guessMoveString = move.toString();

            if (isCastleMove) {
                if (guessMoveString.equals(stringMove)) {
                    return move;
                } else {
                    continue;
                }
            } else if (charRep[0] == 0 && guessMoveString.equals(stringMove)) {
                return move;
            }

            if (charRep[0] == move.getMovingPiece().getStringRep().toUpperCase().charAt(0)) {
                if (charRep[3] != move.getEndingCoordinate().getCharFile()
                        || charRep[4] - 48 != move.getEndingCoordinate().getCharRank()) {
                    // TODO: Check that char rank is correct
                    continue;
                }

                if (charRep[1] != 0) {
                    if (charRep[1] >= 97) {
                        if (charRep[1] != move.getStartingCoordinate().getCharFile()) {
                            continue;
                        }
                    } else {
                        if (charRep[1] - 48 != move.getStartingCoordinate().getCharRank()) {
                            continue;
                        }
                    }
                }

                return move;
            }
        }
        return null;
    }

    private static char[] makeCharRep(String stringMove) {
        char[] charRep = new char[7];

        int endIdx = stringMove.length() - (stringMove.contains("+") ? 2 : 1);
        if (stringMove.contains("=")) {
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
            if (currentChar >= 97) {
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
