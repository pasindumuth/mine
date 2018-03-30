import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PatternMiner {
    private int currentStackLevel;
    private Map<Integer, SequenceContainer> sequenceForLevel;
    private Map<Integer, Pattern> patterns;

    public PatternMiner(Map<Integer, Pattern> patterns) {
        this.currentStackLevel = 0;
        this.sequenceForLevel = new HashMap<>();
        this.patterns = patterns;
    }

    public void processEvent(int functionID, int stackLevel, long startTime, long endTime) {
        if (stackLevel > currentStackLevel) {
            Sequence sequence = new Sequence();
            sequence.add(new SequenceElement(functionID));
            SequenceContainer container = new SequenceContainer(sequence, startTime, endTime);
            sequenceForLevel.put(stackLevel, container);
        
        } else if (stackLevel == currentStackLevel) {
            SequenceContainer container = sequenceForLevel.get(stackLevel);
            Sequence sequence = container.getSequence();
            sequence.add(new SequenceElement(functionID));
            sequence.compressVeryLossy();
            container.updateEndTime(endTime);
            
        } else {
            if (currentStackLevel - stackLevel > 1)
                System.out.println("Warning: jumping down more than one stack level.");
            
            Pattern pattern = updatePatterns(currentStackLevel);
            int patternID = pattern.getPatternID();

            if (!sequenceForLevel.containsKey(stackLevel)) {
                SequenceContainer container = new SequenceContainer(
                    new Sequence(), startTime, startTime);
                sequenceForLevel.put(stackLevel, container);
            }
        
            SequenceContainer container = sequenceForLevel.get(stackLevel);
            Sequence sequence = container.getSequence();
            sequence.add(new SequenceElement(functionID));

            ArrayList<Integer> subPatterns = new ArrayList<>();
            subPatterns.add(patternID);
            sequence.add(new SequenceElement(subPatterns));

            container.updateEndTime(endTime);
        }

        currentStackLevel = stackLevel;
    }

    public void finish() {
        for (int stackLevel = 0; stackLevel <= currentStackLevel; stackLevel++)
            if (sequenceForLevel.containsKey(stackLevel))
                updatePatterns(stackLevel);
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
    private Pattern updatePatterns(int stackLevel) {
        SequenceContainer oldContainer = sequenceForLevel.remove(currentStackLevel);
        Sequence sequence = oldContainer.getSequence();
        int sequenceHash = sequence.hash();
        if (!patterns.containsKey(sequenceHash)) {
            int newPatternID = Constants.PATTERN_BASE + patterns.size();
            Pattern pattern = new Pattern(sequence, newPatternID);
            patterns.put(sequenceHash, pattern);
        }
        Pattern pattern = patterns.get(sequenceHash);
        pattern.addPosition(oldContainer.getStartTime(), oldContainer.getEndTime());
        return pattern;
    }
}
