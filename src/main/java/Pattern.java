import java.util.ArrayList;
import java.util.List;

public class Pattern {
    
    private Sequence sequence;
    private int patternID;
    private List<Long> startTimes;
    private List<Long> durations;

    public Pattern(Sequence sequence, int patternID) {
        this.sequence = sequence;
        this.startTimes = new ArrayList<>();
        this.durations = new ArrayList<>();
        this.patternID = patternID;
    }

    public int getPatternID() {
        return patternID;
    }

    public List<Long> getTracePosition() {
        return startTimes;
    }

    public List<Long> getDurations() {
        return durations;
    }

    public void addInstance(long startTime, long endTime) {
        startTimes.add(startTime);
        durations.add(endTime - startTime);
    }

    public void clearInstances() {
        startTimes = new ArrayList<>();
        durations = new ArrayList<>();
    }

    public int sequenceHash() {
        return this.sequence.hash();
    }

    @Override
    public String toString() {
        return this.sequence.toString();
    }
}