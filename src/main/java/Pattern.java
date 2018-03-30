import java.util.ArrayList;
import java.util.List;

public class Pattern {
    
    private Sequence sequence;
    private int patternID;
    private List<Long> tracePositions;
    private List<Long> durations;

    public Pattern(Sequence sequence, int patternID) {
        this.sequence = sequence;
        this.tracePositions = new ArrayList<>();
        this.durations = new ArrayList<>();
        this.patternID = patternID;
    }

    public int getPatternID() {
        return patternID;
    }

    public List<Long> getTracePosition() {
        return tracePositions;
    }

    public List<Long> getDurations() {
        return durations;
    }

    public void addPosition(long startTime, long endTime) {
        tracePositions.add(startTime);
        durations.add(endTime - startTime);
    }

    public int sequenceHash() {
        return this.sequence.hash();
    }

    @Override
    public String toString() {
        return this.sequence.toString();
    }
}