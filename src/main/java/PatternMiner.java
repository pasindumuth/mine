import java.util.ArrayList;

public class PatternMiner {
    private ArrayList<SequenceContainer> sequenceForLevel;
    private int stackLevel;

    /**
     * We can imagine a fictitious function entrance right at the beginning, acting as the base function for the whole
     * trace. This function is at stack level 0. 
     */
    public PatternMiner() {
        this.sequenceForLevel = new ArrayList<>();
        this.sequenceForLevel.add(new SequenceContainer(new Sequence(), 0)); // dummy sequence to handle base functions
        this.stackLevel = 0;
    }

    public void processEvent(int functionID, int dir, long time) {
        if (dir == Constants.FUNCTION_ENTER) {
            Sequence newSequence = new Sequence();
            newSequence.setFunction(functionID);
            // We want to record the start and end times of a particular Sequence instance, 
            // hence why we use SequenceContainers.
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
            Sequence sequenceBelow = sequenceForLevel.get(stackLevel).getSequence();
            sequenceBelow.addPatternID(pattern.getPatternID());
            sequenceBelow.compressVeryLossy();
        }
    }
}
