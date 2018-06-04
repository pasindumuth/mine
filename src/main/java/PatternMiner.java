import java.util.ArrayList;

public class PatternMiner {
    private ArrayList<SequenceContainer> sequenceForLevel;
    private int stackLevel;

    public PatternMiner() {
        this.sequenceForLevel = new ArrayList<>();
        this.sequenceForLevel.add(new SequenceContainer(new Sequence(), 0)); // dummy sequence to handle base functions
        this.stackLevel = 0;
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
            Pattern pattern = PatternManager.updatePatterns(finishedContainer);
            
            // Update sequence below with the new pattern instance.
            Sequence nextSequence = sequenceForLevel.get(stackLevel).getSequence();
            nextSequence.addPatternID(pattern.getPatternID());
            nextSequence.compressVeryLossy();
        }
    }
}
