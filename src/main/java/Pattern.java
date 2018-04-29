import java.util.ArrayList;
import java.util.List;

public class Pattern {
    
    private Sequence sequence;
    private int patternID;
    private List<Long> startTimes;
    private List<Long> durations;

    private static int nextPatternID = 0;

    public static int nextPatternID() {
        return nextPatternID++;
    }

    public Pattern(Sequence sequence, int patternID) {
        this.sequence = sequence;
        this.startTimes = new ArrayList<>();
        this.durations = new ArrayList<>();
        this.patternID = patternID;
    }

    public void updatePatternSequence(Sequence newSequence) {
        sequence.mergeSequence(newSequence);
    }

    public int getPatternID() {
        return patternID;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public List<Long> getStartTimes() {
        return startTimes;
    }

    public List<Long> getDurations() {
        return durations;
    }

    public void addInstance(long startTime, long endTime) {
        startTimes.add(startTime);
        durations.add(endTime - startTime);
    }

    public void reset() {
        sequence = sequence.createEmptyClone();
        startTimes = new ArrayList<>();
        durations = new ArrayList<>();
    }

    public int sequenceHash() {
        return this.sequence.hash();
    }
}