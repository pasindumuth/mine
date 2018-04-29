import java.util.HashMap;
import java.util.Map;

public class PatternMiner {
    private int currentStackLevel;
    private Map<Integer, Sequence> sequenceForLevel;
    private Map<Integer, Pattern> patterns;

    public PatternMiner(Map<Integer, Pattern> patterns) {
        this.currentStackLevel = 0;
        this.sequenceForLevel = new HashMap<>();
        this.patterns = patterns;
    }

    /**
     * This function takes in the exit events of a trace in order, and mines patterns with efficient
     * memory usage.
     * @param functionID the preprocessed function ID; an integer that maps uniques to a function
     * @param stackLevel the stack level of the exist event. More precisely, the total number of
     *                   function calls until and including this point that have entered but not exited.
     * @param startTime the start time of the particular function call
     * @param endTime the end time of the particular function call; the time of the exit event
     */
    public void processEvent(int functionID, int stackLevel, long startTime, long endTime) {

        /**
         * At any point, `sequenceForLevel` will have an entry for all stack levels >= 0 
         * and <= `currentStackLevel` that have had a function execute, but still haven't 
         * had the base function exit yet. Thus, `sequenceForLevel` keeps track of the 
         * incomplete pattern instances in the trace.
         */

        if (stackLevel > currentStackLevel) {
            Sequence sequence = new Sequence();
            sequence.setFunction(functionID);
            Pattern pattern = updatePatterns(sequence, startTime, endTime);

            Sequence base = new Sequence();
            base.addPatternID(pattern.getPatternID());
            
            sequenceForLevel.put(stackLevel, base);
        
        } else if (stackLevel == currentStackLevel) {

            /**
             * There is only one circumstance when there would not
             * already be SequenceContainer for this level. This is when
             * at the very start of the trace, the execution enters a function
             * and then immediately exits.
             */

            Sequence sequence = new Sequence();
            sequence.setFunction(functionID);
            Pattern pattern = updatePatterns(sequence, startTime, endTime);

            if (!sequenceForLevel.containsKey(stackLevel)) {
                Sequence base = new Sequence();
                sequenceForLevel.put(stackLevel, base);
            }

            Sequence base = sequenceForLevel.get(stackLevel);
            base.addPatternID(pattern.getPatternID());
            base.compressVeryLossy();
            
        } else {
            if (currentStackLevel - stackLevel > 1)
                System.out.println("Warning: jumping down more than one stack level.");
            
            Sequence base = sequenceForLevel.remove(currentStackLevel);
            base.setFunction(functionID);
            Pattern pattern = updatePatterns(base, startTime, endTime);

            int patternID = pattern.getPatternID();

            if (!sequenceForLevel.containsKey(stackLevel)) {
                Sequence nextBase = new Sequence();
                sequenceForLevel.put(stackLevel, nextBase);
            }
        
            Sequence nextBase = sequenceForLevel.get(stackLevel);
            nextBase.addPatternID(patternID);
            nextBase.compressVeryLossy();
        }

        currentStackLevel = stackLevel;
    }

    public Map<Integer, Pattern> getPatterns() {
        return patterns;
    }

    /**
     * Take sequence at stackLevel in sequenceForLevel, and either update the existing
     * pattern in `patterns` which wraps the sequence, or create a new pattern
     * which wraps the sequence, and add it into the `patterns` map.
     * @param stackLevel stack level to extract the sequence at
     * @return pattern that backs the sequence in sequenceForLevel at `stackLevel`
     */

    private Pattern updatePatterns(Sequence sequence, long startTime, long endTime) {
        int sequenceHash = sequence.hash();
        Pattern pattern;
        if (!patterns.containsKey(sequenceHash)) {
            int newPatternID = sequence.isSingleFunction() ? sequence.getFunction() : Pattern.nextTruePatternID();
            pattern = new Pattern(sequence, newPatternID);
            patterns.put(sequenceHash, pattern);
        } else {
            pattern = patterns.get(sequenceHash);
            pattern.updatePatternSequence(sequence);
        }

        pattern.addInstance(startTime, endTime);
        return pattern;
    }
}
