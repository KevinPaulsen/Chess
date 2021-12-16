package chess.model.chessai;

import java.util.HashMap;

/**
 * This is an implementation of a transposition table.
 */
public class TranspositionTable extends HashMap<Long, TranspositionTable.TranspositionTableValue> {

    public static final int EXACT = 0;
    public static final int UPPER = 1;
    public static final int LOWER = 2;

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param evaluation value to be associated with the specified key
     * @param identifier the identifier of this node
     * @return the previous value associated with {@code key}, or
     * {@code null} if there was no mapping for {@code key}.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with {@code key}.)
     */
    public TranspositionTableValue put(Long key, Evaluation evaluation, int identifier) {
        TranspositionTableValue value = new TranspositionTableValue(evaluation, identifier);
        return super.put(key, value);
    }

    /**
     * This class is responsible for containing the evaluation,
     * and weather this evaluation is an exact, upper, or lower
     * bound.
     */
    public static class TranspositionTableValue {

        /**
         * The evaluation for this node.
         */
        private final Evaluation evaluation;

        /**
         * The identifier specifying that this node is exact,
         * upper, or lower bound.
         */
        private final int identifier;

        /**
         * Creates a TranspositionTableValue with the given
         * evaluation and identifier.
         *
         * @param evaluation the evaluation for this node.
         * @param identifier the identifier for this node.
         */
        public TranspositionTableValue(Evaluation evaluation, int identifier) {
            this.evaluation = evaluation;
            this.identifier = identifier;
        }

        /**
         * @return the evaluation of this Node
         */
        public Evaluation getEvaluation() {
            return evaluation;
        }

        /**
         * @return if this node is an exact evaluation.
         */
        public boolean isExact() {
            return identifier == EXACT;
        }

        /**
         * @return if this node is an upper bound.
         */
        public boolean isUpper() {
            return identifier == UPPER;
        }

        /**
         * @return if this node is a lower bound.
         */
        public boolean isLower() {
            return identifier == LOWER;
        }
    }
}
