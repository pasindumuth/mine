import java.util.ArrayList;

public class Pattern {
    
    private Sequence sequence;
    private ArrayList<Long> tracePositions;
    private ArrayList<Long> durations;

    public Pattern(Sequence sequence) {
        this.sequence = sequence;
        this.tracePositions = new ArrayList<>();
        this.durations = new ArrayList<>();
    }

    public void addPosition(long startTime, long endTime) {
        tracePositions.add(startTime);
        durations.add(endTime - startTime);
    }

    @Override
    public int hashCode() {
        return this.sequence.hashCode();
    }
}