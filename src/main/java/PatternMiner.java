import java.util.ArrayList;
import java.util.Map;

public class PatternMiner {
    private ArrayList<SequenceContainer> sequenceForLevel;
    private int stackLevel;
    private Map<Integer, Pattern> patterns;

    public PatternMiner(Map<Integer, Pattern> patterns) {
        this.sequenceForLevel = new ArrayList<>();
        this.sequenceForLevel.add(new SequenceContainer(new Sequence(), 0)); // dummy sequence to handle base functions
        this.stackLevel = 0;
        this.patterns = patterns;
    }

    public void processEvent(int functionID, int dir, long time) {
        if (dir == Constants.FUNCTION_ENTER) {
            Sequence newSequence = new Sequence();
            newSequence.setFunction(functionID);
            SequenceContainer container = new SequenceContainer(newSequence, time);
            stackLevel++;
            if (stackLevel < sequenceForLevel.size()) sequenceForLevel.set(stackLevel, container);
            else sequenceForLevel.add(container);
        } else {
            // The highest sequence is finished. Update the set of patterns.
            SequenceContainer finishedContainer = sequenceForLevel.get(stackLevel);
            stackLevel--;
            finishedContainer.setEndTime(time);
            Pattern pattern = updatePatterns(finishedContainer);
            
            // Update sequence below with the new pattern instance.
            Sequence nextSequence = sequenceForLevel.get(stackLevel).getSequence();
            nextSequence.addPatternID(pattern.getPatternID());
            nextSequence.compressVeryLossy();
        }
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

    private Pattern updatePatterns(SequenceContainer container) {
        Sequence sequence = container.getSequence();
        int sequenceHash = sequence.hash();
        Pattern pattern;
        if (!patterns.containsKey(sequenceHash)) {
            int newPatternID = Pattern.nextPatternID();
            pattern = new Pattern(sequence, newPatternID);
            patterns.put(sequenceHash, pattern);
        } else {
            pattern = patterns.get(sequenceHash);
            pattern.updatePatternSequence(sequence);
        }

        pattern.addInstance(container.getStartTime(), container.getEndTime());
        return pattern;
    }
}
