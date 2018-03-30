import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PatternWriter {
    Map<Integer, Pattern> patterns;
    BufferedWriter writer;

    public PatternWriter(Map<Integer, Pattern> patterns, BufferedWriter writer) {
        this.patterns = patterns;
        this.writer = writer;
    }

    public void write() throws IOException {
        List<Pattern> patternList = new ArrayList<>(patterns.values());
        patternList.sort(new Comparator<Pattern>() {
            public int compare(Pattern p1, Pattern p2) {
                return p1.getPatternID() - p2.getPatternID();
            }
        });
        
        for (Pattern pattern : patternList) {
            writer.write("#############\n");
            writer.write(String.valueOf(pattern.getPatternID()) + ":\n");
            writer.write(pattern.toString() + "\n");

            List<Long> tracePositions = pattern.getTracePosition();
            List<Long> durations = pattern.getDurations();

            writer.write(String.valueOf(tracePositions.size()) + " OCCURRENCES.\n");
            for (int i = 0; i < tracePositions.size(); i++) {
                writer.write(String.valueOf(tracePositions.get(i)));
                writer.write(" : ");
                writer.write(String.valueOf(durations.get(i)));
                writer.write("\n");
            }
        }
    }
}
