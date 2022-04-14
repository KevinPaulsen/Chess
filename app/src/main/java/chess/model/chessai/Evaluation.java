package chess.model.chessai;

import chess.Move;

import static chess.model.GameModel.BLACK;
import static chess.model.GameModel.WHITE;

public class Evaluation implements Comparable<Evaluation> {

    public static final Evaluation MIN_EVALUATION = new Evaluation(Integer.MIN_VALUE, -1);
    public static final Evaluation MAX_EVALUATION = new Evaluation(Integer.MAX_VALUE, -1);

    public static final byte EXACT = 0;
    public static final byte UPPER = 1;
    public static final byte LOWER = 2;
    public static final char NO_LOSER = 'n';
    public static final char TIE = 'T';
    private static final byte LESS_THAN = -1;
    private static final byte EQUAL = 0;
    private static final byte GREATER_THAN = 1;
    private final Move move;
    private final int evaluation;
    private final char loser;
    private final int depth;
    private final byte bound;

    private final Evaluation next;

    public Evaluation(int evaluation, int depth) {
        this(null, evaluation, NO_LOSER, depth, EXACT, null);
    }

    public Evaluation(Evaluation evaluation, Move move, byte bound) {
        /*
        == == -> ==
        >= >= -> >=
        <= <= -> <=

        == >= -> >=
        == <= -> <=
        >= == -> >=
        <= == -> <=

        >= <= -> error
        <= >= -> error
         */
        if (bound == LESS_THAN && evaluation.bound == GREATER_THAN || bound == GREATER_THAN && evaluation.bound == LESS_THAN) {
            throw new IllegalArgumentException("Invalid bound selection");
        } else if (bound != evaluation.bound) {
            if (bound == EXACT) bound = evaluation.bound;
        }

        this.move = move;
        this.evaluation = evaluation.evaluation + Integer.compare(0, evaluation.evaluation);
        this.loser = evaluation.loser;
        this.depth = evaluation.depth + 1;
        this.bound = bound;
        this.next = evaluation;
    }

    public Evaluation(Move move, Evaluation evaluation, byte bound) {
        this(move, evaluation.evaluation + Integer.compare(0, evaluation.evaluation),
                evaluation.loser, evaluation.depth + 1, bound, evaluation);
    }

    public Evaluation(Move currentMove, int evaluation, char loser, int depth, byte bound, Evaluation next) {
        this.move = currentMove;
        this.evaluation = evaluation;
        this.loser = loser;
        this.depth = depth;
        this.bound = bound;
        this.next = next;
    }

    public static Evaluation min(Evaluation evaluation1, Evaluation evaluation2) {
        return evaluation1.compareTo(evaluation2) > 0 ? evaluation2 : evaluation1;
    }

    public static Evaluation max(Evaluation evaluation1, Evaluation evaluation2) {
        return evaluation1.compareTo(evaluation2) < 0 ? evaluation2 : evaluation1;
    }

    public Move getMove() {
        return move;
    }

    public double getEvaluation() {
        return evaluation;
    }

    public int getDepth() {
        return depth;
    }

    public char getLoser() {
        return loser;
    }

    public boolean isExact() {
        return bound == EXACT;
    }

    public boolean isLower() {
        return bound == LOWER;
    }

    public boolean isUpper() {
        return bound == UPPER;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Evaluation ");
        result.append(switch (bound) {
            case LOWER -> ">=";
            case GREATER_THAN -> "<=";
            default -> "==";
        });
        result.append(String.format(" %-7d", evaluation));
        result.append(String.format("Depth: %-2d ", depth));
        result.append(String.format("Moves: %-6s", move));
        Evaluation current = next;
        while (current != null && current.move != null) {
            result.append(String.format(" | %-6s", current.move));
            current = current.next;
        }
        return result.toString();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Evaluation o) {
        if (this == o) {
            return EQUAL;
        } else if (this == MIN_EVALUATION) {
            return LESS_THAN;
        } else if (this == MAX_EVALUATION) {
            return GREATER_THAN;
        } else if (this.loser == WHITE) {
            if (o.loser == WHITE) {
                return Integer.compare(this.depth, o.depth);
            } else {
                return LESS_THAN;
            }
        } else if (this.loser == BLACK) {
            if (o.loser == BLACK) {
                return Integer.compare(o.depth, this.depth);
            } else {
                return GREATER_THAN;
            }
        } else if (this.loser == TIE && o.loser == TIE) {
            return EQUAL;
        } else {
            return Integer.compare(this.evaluation, o.evaluation);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Evaluation)) return false;

        Evaluation that = (Evaluation) o;

        if (getEvaluation() != that.getEvaluation()) return false;
        if (getLoser() != that.getLoser()) return false;
        if (getDepth() != that.getDepth()) return false;
        if (bound != that.bound) return false;
        if (getMove() != null ? !getMove().equals(that.getMove()) : that.getMove() != null) return false;
        return next != null ? next.equals(that.next) : that.next == null;
    }

    @Override
    public int hashCode() {
        int result = getMove() != null ? getMove().hashCode() : 0;
        result = 31 * result + (int) getEvaluation();
        result = 31 * result + (int) getLoser();
        result = 31 * result + getDepth();
        result = 31 * result + (int) bound;
        result = 31 * result + (next != null ? next.hashCode() : 0);
        return result;
    }
}
