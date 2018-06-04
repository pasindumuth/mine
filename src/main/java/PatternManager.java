import java.util.List;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PatternManager {

    /**
     * Maps pairs of patternIDs to a distance value. We define this map so that
     * it forms a metric on the set of patternIDs. To be a metric, we must 
     * maintains the following properites:
     * 1. patternDistances[i][j] = patternDistances[j][i]
     * 2. patternDistances[i][i] = 0
     * 3. patternDistances[i][j] <= patternDistances[i][k] + patternDistances[k][j];
     */
    private static ArrayList<ArrayList<Double>> patternDistances = new ArrayList<>();

    /**
     * Null pattern has uses for calculating distance.
     */
    public static int NULL_PATTERN_ID = 0;

    /**
     * Maps patternIDs to shape of the original instance of the pattern. `patternDistances`
     * is calculated using the original shapes.
     */
    private static ArrayList<Sequence> originalPatternShapes = new ArrayList<>();

    /**
     * Holds onto patterns as they evolve.
     */
    private static ArrayList<Pattern> currentPatterns = new ArrayList<>();

    /**
     * @param p1 first patternID
     * @param p2 second patternID
     * @return distance between the patternIDs
     */
    public static double getDistance(int p1, int p2) {
        return patternDistances.get(p1).get(p2);
    }

    public static void initializeNullPattern() {
        Sequence nullPatternSequence = new Sequence();
        nullPatternSequence.setFunction(Constants.NULL_FUNCTION_ID);
        SequenceContainer container = new SequenceContainer(nullPatternSequence, 0);
        container.setEndTime(0);
        Pattern nullPattern = updatePatterns(container);
        NULL_PATTERN_ID = nullPattern.getPatternID();
    }

    /**
     * Checks whether the sequence is an instance of an existing pattern. If so,
     * record this sequence as an instance of that pattern. If not, create a 
     * new pattern, and record one instance.
     */
    public static Pattern updatePatterns(SequenceContainer container) {
        Sequence sequence = container.getSequence();
        for (int patternID = 0; patternID < currentPatterns.size(); patternID++) {
            Pattern pattern = currentPatterns.get(patternID);
            if (pattern.getSequence().canMerge(sequence)) {
                pattern.getSequence().merge(sequence);
                pattern.addInstance(container.getStartTime(), container.getEndTime());
                return pattern;                
            }
        }

        int patternID = originalPatternShapes.size();
        Sequence originalPatternShape = sequence.createEmptyClone();
        originalPatternShapes.add(originalPatternShape);
        updateDistances(originalPatternShape);

        Pattern pattern = new Pattern(sequence, patternID);
        currentPatterns.add(pattern);

        return pattern;
    }

    /**
     * Computes the distance between `newShape` to the existing pattern shapes,
     * and updates the distance map.
     * 
     * We use an edit distance based measure.
     */
    private static void updateDistances(Sequence newShape) {
        ArrayList<Double> newDistances = new ArrayList<>();
        for (int i = 0; i < patternDistances.size(); i++) {
            double distance = newShape.getDistance(originalPatternShapes.get(i));
            newDistances.add(distance);
            patternDistances.get(i).add(distance);
        }

        newDistances.add(0.0);
        patternDistances.add(newDistances);
    }

    /**
     * Writes the current patterns to a the writer provided, and resets the current
     * patterns.
     */
    public static void flushPatterns(BufferedWriter writer) throws IOException {
        Map<Integer, Integer> singleFunctions = new HashMap<>();
        for (Pattern pattern : currentPatterns) {
            Sequence sequence = pattern.getSequence();
            if (sequence.isSingleFunction()) {
                singleFunctions.put(pattern.getPatternID(), sequence.getFunction());
            }
        }
        
        for (Pattern pattern : currentPatterns) {
            Sequence sequence = pattern.getSequence();
            if (sequence.isSingleFunction()) continue;
            if (pattern.getStartTimes().size() == 0) continue;
            List<Long> startTimes = pattern.getStartTimes();
            List<Long> durations = pattern.getDurations();

            writer.write("#############\n");
            writer.write(String.valueOf(pattern.getPatternID() + Constants.PATTERN_BASE) + ":\n");
            writer.write(sequence.toString(singleFunctions) + "\n");

            writer.write(String.valueOf(startTimes.size()) + " OCCURRENCES.\n");
            for (int i = 0; i < startTimes.size(); i++) {
                writer.write(String.valueOf(startTimes.get(i)));
                writer.write(" : ");
                writer.write(String.valueOf(durations.get(i)));
                writer.write("\n");
            }

            pattern.reset();
        }
    }
}
