import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class PatternMiner {
    private PatternManager manager;

    private ArrayList<SequenceContainer> sequenceForLevel;
    private int stackLevel;

    /**
     * We can imagine a fictitious function entrance right at the beginning, acting as the base function for the whole
     * trace. This function is at stack level 0. 
     */
    public PatternMiner(PatternManager manager) {
        this.manager = manager;
        this.sequenceForLevel = new ArrayList<>();
        this.sequenceForLevel.add(new SequenceContainer(new Sequence(manager), 0)); // dummy sequence to handle base functions
        this.stackLevel = 0;
    }

    /**
     * Reads every line from the thread and adds any pattern that it detects into the pattern manager.
     */ 
    public void mineThread(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        int count = 0;
        while (line != null) {
            if (count % 1000000 == 0) System.out.println(count);
            count++;

            String[] record = line.split("\t");
            
            int functionID = Integer.parseInt(record[0]);
            int dir = Integer.parseInt(record[1]);
            long time = Long.parseLong(record[2]);

            processEvent(functionID, dir, time);

            line = reader.readLine();
        }
    }

    /**
     * Fundamentally, this function defines how (complete) function call sequences map to patterns.
     */
    public void processEvent(int functionID, int dir, long time) {
        if (dir == Constants.FUNCTION_ENTER) {
            Sequence newSequence = new Sequence(manager);
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
            Pattern pattern = manager.updatePatterns(finishedContainer);
            
            // Update sequence below with the new pattern instance.
            Sequence sequenceBelow = sequenceForLevel.get(stackLevel).getSequence();
            sequenceBelow.addPatternID(pattern.getPatternID());
            sequenceBelow.compressVeryLossy();
        }
    }
}
