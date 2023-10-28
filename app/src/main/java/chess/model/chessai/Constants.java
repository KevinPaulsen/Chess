package chess.model.chessai;

public class Constants {

    public static final int PAWN_SCORE = 100;
    public static final int KNIGHT_SCORE = 300;
    public static final int BISHOP_SCORE = 300;
    public static final int ROOK_SCORE = 500;
    public static final int QUEEN_SCORE = 900;

    public static final int CAPTURED_PIECE_VALUE_MULTIPLIER = 10;
    public static final int CAPTURE_BIAS = 50;

    private static final int TERRIBLE = -50;
    private static final int VERY_BAD = -40;
    private static final int PRET_BAD = -30;
    private static final int BAD_MULT = -20;
    private static final int NOT_GOOD = -10;
    private static final int ALMST_OK = -5;
    private static final int AVERAGE_ = 0;
    private static final int MEDIOCRE = 5;
    public static final int[] QUEEN_VALUE_MAP =
            {BAD_MULT, NOT_GOOD, NOT_GOOD, ALMST_OK, ALMST_OK, NOT_GOOD, NOT_GOOD, BAD_MULT,
                    NOT_GOOD, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, NOT_GOOD,
                    NOT_GOOD, AVERAGE_, MEDIOCRE, MEDIOCRE, MEDIOCRE, MEDIOCRE, AVERAGE_, NOT_GOOD,
                    ALMST_OK, AVERAGE_, MEDIOCRE, MEDIOCRE, MEDIOCRE, MEDIOCRE, AVERAGE_, ALMST_OK,
                    AVERAGE_, AVERAGE_, MEDIOCRE, MEDIOCRE, MEDIOCRE, MEDIOCRE, AVERAGE_, ALMST_OK,
                    NOT_GOOD, MEDIOCRE, MEDIOCRE, MEDIOCRE, MEDIOCRE, MEDIOCRE, AVERAGE_, NOT_GOOD,
                    NOT_GOOD, AVERAGE_, MEDIOCRE, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, NOT_GOOD,
                    BAD_MULT, NOT_GOOD, NOT_GOOD, ALMST_OK, ALMST_OK, NOT_GOOD, NOT_GOOD,
                    BAD_MULT,};
    private static final int OK_SCORE = 10;
    public static final int[] BISHOP_VALUE_MAP =
            {BAD_MULT, NOT_GOOD, NOT_GOOD, NOT_GOOD, NOT_GOOD, NOT_GOOD, NOT_GOOD, BAD_MULT,
                    NOT_GOOD, MEDIOCRE, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, MEDIOCRE, NOT_GOOD,
                    NOT_GOOD, OK_SCORE, OK_SCORE, OK_SCORE, OK_SCORE, OK_SCORE, OK_SCORE, NOT_GOOD,
                    NOT_GOOD, AVERAGE_, OK_SCORE, OK_SCORE, OK_SCORE, OK_SCORE, AVERAGE_, NOT_GOOD,
                    NOT_GOOD, MEDIOCRE, MEDIOCRE, OK_SCORE, OK_SCORE, MEDIOCRE, MEDIOCRE, NOT_GOOD,
                    NOT_GOOD, AVERAGE_, MEDIOCRE, OK_SCORE, OK_SCORE, MEDIOCRE, AVERAGE_, NOT_GOOD,
                    NOT_GOOD, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, NOT_GOOD,
                    BAD_MULT, NOT_GOOD, NOT_GOOD, NOT_GOOD, NOT_GOOD, NOT_GOOD, NOT_GOOD,
                    BAD_MULT,};
    public static final int[] ROOK_VALUE_MAP =
            {AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_,
                    ALMST_OK, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, ALMST_OK,
                    ALMST_OK, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, ALMST_OK,
                    ALMST_OK, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, ALMST_OK,
                    ALMST_OK, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, ALMST_OK,
                    ALMST_OK, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, ALMST_OK,
                    ALMST_OK, OK_SCORE, OK_SCORE, OK_SCORE, OK_SCORE, OK_SCORE, OK_SCORE, ALMST_OK,
                    AVERAGE_, AVERAGE_, AVERAGE_, MEDIOCRE, MEDIOCRE, AVERAGE_, AVERAGE_,
                    AVERAGE_,};
    private static final int GOOD_MLT = 20;
    private static final int VRY_GOOD = 25;
    public static final int[] KNIGHT_VALUE_MAP =
            {TERRIBLE, VERY_BAD, PRET_BAD, PRET_BAD, PRET_BAD, PRET_BAD, VERY_BAD, TERRIBLE,
                    VERY_BAD, BAD_MULT, AVERAGE_, MEDIOCRE, MEDIOCRE, AVERAGE_, BAD_MULT, VERY_BAD,
                    PRET_BAD, MEDIOCRE, OK_SCORE, GOOD_MLT, GOOD_MLT, OK_SCORE, MEDIOCRE, PRET_BAD,
                    PRET_BAD, AVERAGE_, GOOD_MLT, VRY_GOOD, VRY_GOOD, GOOD_MLT, AVERAGE_, PRET_BAD,
                    PRET_BAD, MEDIOCRE, GOOD_MLT, VRY_GOOD, VRY_GOOD, GOOD_MLT, MEDIOCRE, PRET_BAD,
                    PRET_BAD, AVERAGE_, OK_SCORE, GOOD_MLT, GOOD_MLT, OK_SCORE, AVERAGE_, PRET_BAD,
                    VERY_BAD, BAD_MULT, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, BAD_MULT, VERY_BAD,
                    TERRIBLE, VERY_BAD, PRET_BAD, PRET_BAD, PRET_BAD, PRET_BAD, VERY_BAD,
                    TERRIBLE,};
    private static final int GREAT_SR = 30;
    public static final int[] KING_VALUE_MAP =
            {GOOD_MLT, GREAT_SR, OK_SCORE, AVERAGE_, AVERAGE_, OK_SCORE, GREAT_SR, GOOD_MLT,
                    GOOD_MLT, GOOD_MLT, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, GOOD_MLT, GOOD_MLT,
                    NOT_GOOD, BAD_MULT, BAD_MULT, BAD_MULT, BAD_MULT, BAD_MULT, BAD_MULT, NOT_GOOD,
                    BAD_MULT, PRET_BAD, PRET_BAD, VERY_BAD, VERY_BAD, PRET_BAD, PRET_BAD, BAD_MULT,
                    PRET_BAD, VERY_BAD, VERY_BAD, TERRIBLE, TERRIBLE, VERY_BAD, VERY_BAD, PRET_BAD,
                    PRET_BAD, VERY_BAD, VERY_BAD, TERRIBLE, TERRIBLE, VERY_BAD, VERY_BAD, PRET_BAD,
                    PRET_BAD, VERY_BAD, VERY_BAD, TERRIBLE, TERRIBLE, VERY_BAD, VERY_BAD, PRET_BAD,
                    PRET_BAD, VERY_BAD, VERY_BAD, TERRIBLE, TERRIBLE, VERY_BAD, VERY_BAD,
                    PRET_BAD,};
    private static final int BEST_MLT = 50;
    public static final int[] PAWN_VALUE_MAP =
            {AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_, AVERAGE_,
                    MEDIOCRE, OK_SCORE, OK_SCORE, BAD_MULT, BAD_MULT, OK_SCORE, OK_SCORE, MEDIOCRE,
                    MEDIOCRE, ALMST_OK, NOT_GOOD, AVERAGE_, AVERAGE_, NOT_GOOD, ALMST_OK, MEDIOCRE,
                    MEDIOCRE, MEDIOCRE, MEDIOCRE, GOOD_MLT, GOOD_MLT, MEDIOCRE, MEDIOCRE, MEDIOCRE,
                    MEDIOCRE, MEDIOCRE, OK_SCORE, VRY_GOOD, VRY_GOOD, OK_SCORE, MEDIOCRE, MEDIOCRE,
                    OK_SCORE, OK_SCORE, GOOD_MLT, GREAT_SR, GREAT_SR, GOOD_MLT, OK_SCORE, OK_SCORE,
                    BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT,
                    BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT, BEST_MLT,
                    BEST_MLT,};
}
