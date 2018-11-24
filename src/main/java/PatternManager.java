import java.util.List;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * At any given time, we have metric space of patternIDs containing the zeroth pattern, which then 
 * generates a metric space of SequenceElements, which finally generates a metric space of Sequences.
 * Importantly, if we extend the patternID metric space by one patternID such that the old distances
 * stay preserved, we note that the new SequenceElement and Sequences metric spaces are extension of
 * the old ones such that their old distances stay preserved. This is important because it means that
 * if we defined the patternID metric based on their shapes (which are Sequences), extending the patternID
 * metric space doesn't maintains it's relationship with the pattern shapes.
 * 
 * It's confusing that the patternID metric is defined in terms Sequences, because then it seems
 * like the definitions are cyclic. We emphasize that the fundamental metric space is over the pattern *IDs*,
 * and that the significance of the pattern's shape here is in how it provides a canonical means of extending
 * the metric over patternIDs when a new pattern get's added.
 * 
 * TO-DO: we care about the above because in the end, we end up with a patternID metric space, with shapes
 * assigned to the patterns over the course of the data processing such that the SequenceElement properties hold,
 * and shape distances of the patterns stay faithful to their defined distances. It the shapes we want at the end,
 * after all, and having these properties remain is the whole point.
 * 
 * TO-DO: refine. Null is really special. Go from PatternIDs (with null). Defines metric space over SequenceElements
 * over PatternIDs null, along with the null Sequence Element. Now, we can use Sequence Metric Space Theorem to show
 * sequences of non null SequenceElements form a metric space. 
 * 
 * Null pattern maps to empty sequence shape, and all other patterns have a Sequence which use only patternIDs prior 
 * (except for null, which never occur in Sequences anyways), where distance between patternIDs is faithful to 
 * the distance between shapes.
 * 
 * TO-DO: consider renaming this to "context"
 */
public class PatternManager {

    /**
     * Maps pairs of patternIDs to a distance value. We define this map so that
     * it forms a metric on the set of patternIDs. To be a metric, we must 
     * maintains the following properites:
     * 1. patternDistances[i][j] = patternDistances[j][i]
     * 2. patternDistances[i][i] = 0
     * 3. patternDistances[i][j] <= patternDistances[i][k] + patternDistances[k][j]
     */
    public static class PatternDistances {
        private List<List<Double>> patternDistances = new ArrayList<>();

        public PatternDistances(List<List<Double>> patternDistances) {
            this.patternDistances = patternDistances;
        }
        
        public double getDistance(int p1, int p2) {
            return patternDistances.get(p1).get(p2);
        }

        private List<List<Double>> get() {
            return patternDistances;
        }
    }

    private PatternDistances patternDistances;

    /**
     * TO-DO: don't call this null. Null has a history of breaking specification of what it represents.
     * This first pattern has all the properties of any other pattern. It's just the first, and has the 
     * role of being the filler pattern in {@link Sequence#getDistance}
     */
    public static int NULL_PATTERN_ID = 0;

    /**
     * Maps patternIDs to shape of the original instance of the pattern. {@link #patternDistances}
     * is calculated using the original shapes.
     */
    private List<Sequence> originalPatternShapes = new ArrayList<>();

    /**
     * Holds onto patterns as they evolve.
     */
    private List<Pattern> currentPatterns = new ArrayList<>();

    public PatternManager(PatternDistances patternDistances) {
        this.patternDistances = patternDistances;
        initializeNullPattern();
    }

    public ArrayList<Sequence> getNonNullShapes() {
        ArrayList<Sequence> shapes = new ArrayList<>();
        for (int i = 1; i < originalPatternShapes.size(); i++) {
            shapes.add(originalPatternShapes.get(i));
        }
        return shapes;
    }


    public List<Sequence> getShapes() {
        return originalPatternShapes;
    }

    public PatternDistances getPatternDistances() {
        return patternDistances;
    }

    /**
     * The null pattern is the first pattern in our set of patterns, and the first element
     * of the pattern metric space.
     */
    public void initializeNullPattern() {
        Sequence nullPatternSequence = new Sequence(patternDistances, Constants.NULL_FUNCTION_ID);
        SequenceContainer container = new SequenceContainer(nullPatternSequence, 0);
        container.setEndTime(0);
        updatePatterns(container);
    }

    /**
     * Checks whether the sequence is an instance of an existing pattern. If so,
     * record this sequence as an instance of that pattern. If not, create a 
     * new pattern, and record one instance.
     */
    public Pattern updatePatterns(SequenceContainer container) {
        Sequence sequence = container.getSequence();
        for (int patternID = 0; patternID < currentPatterns.size(); patternID++) {
            Pattern pattern = currentPatterns.get(patternID);
            if (pattern.getSequence().canMerge(sequence)) {
                pattern.getSequence().merge(sequence);
                pattern.addInstance(container.getStartTime(), container.getEndTime());
                return pattern;                
            }
        }

        // We have encountered a new pattern
        int patternID = updateDistances(sequence.createEmptyClone());
        Pattern pattern = new Pattern(sequence.createEmptyClone(), patternID);
        pattern.getSequence().merge(sequence);
        pattern.addInstance(container.getStartTime(), container.getEndTime());
        currentPatterns.add(pattern);

        return pattern;
    }

    /**
     * Computes the distance between `newShape` and the existing pattern shapes,
     * and updates the distance map.
     * 
     * We use an edit distance based measure.
     */
    private int updateDistances(Sequence newShape) {
        originalPatternShapes.add(newShape);
        ArrayList<Double> newDistances = new ArrayList<>();
        for (int i = 0; i < patternDistances.get().size(); i++) {
            double distance = newShape.getDistance(originalPatternShapes.get(i));
            newDistances.add(distance);
            patternDistances.get().get(i).add(distance);
        }

        newDistances.add(0.0);
        patternDistances.get().add(newDistances);
        return originalPatternShapes.size() - 1; // The current index of the newly added shape.
    }

    public void dumpPatternManager(BufferedWriter writer) throws IOException {
        dumpPatternManager(writer, true);
    }

    /**
     * Writes a dump of the pattern shapes, original shapes, and distance map kept track
     * by the pattern manager.
     */
    public void dumpPatternManager(BufferedWriter writer, boolean excludeSingeFunctions) throws IOException {
        Map<Integer, Integer> originalSingleFunctions = new HashMap<>();
        for (int i = 0; i < originalPatternShapes.size(); i++) {
            Sequence sequence = originalPatternShapes.get(i);
            if (sequence.isSingleFunction()) {
                originalSingleFunctions.put(i, sequence.getFunction());
            }
        }
        
        // We print the distances for nonSingleFunctions of the original pattern shapes
        StringBuilder patternIDSb = new StringBuilder();
        patternIDSb.append("\t\t\t");
        for (int i = 0; i < originalPatternShapes.size(); i++) {
            if (excludeSingeFunctions && originalSingleFunctions.containsKey(i)) continue;
            patternIDSb.append(String.valueOf(i + Constants.PATTERN_BASE) + ":\t\t");
        }
        writer.write(patternIDSb.toString() + "\n");

        for (int i = 0; i < patternDistances.get().size(); i++) {
            if (excludeSingeFunctions && originalSingleFunctions.containsKey(i)) continue;
            List<Double> distanceRow = patternDistances.get().get(i);
            StringBuilder distanceRowSb = new StringBuilder();
            distanceRowSb.append(String.valueOf(i + Constants.PATTERN_BASE) + ":\t\t");
            for (int j = 0; j < distanceRow.size(); j++) {
                if (excludeSingeFunctions && originalSingleFunctions.containsKey(j)) continue;
                distanceRowSb.append(String.format("%.2f", distanceRow.get(j)) + "\t\t");
            }
            writer.write(distanceRowSb.toString() + "\n");
        }

        // Print original pattern shapes
        writer.write("ORIGINAL SHAPES\n");
        for (int i = 0; i < originalPatternShapes.size(); i++) {
            Sequence sequence = originalPatternShapes.get(i);
            if (excludeSingeFunctions && sequence.isSingleFunction()) continue;
            writer.write("#############\n");
            writer.write(String.valueOf(i + Constants.PATTERN_BASE) + ":\n");
            writer.write(sequence.toString(originalSingleFunctions) + "\n");
        }

        // Print current pattern shapes. These can be extensions of the original after new 
        // instances of a given original pattern shape are found.
        writer.write("CURRENT PATTERNS\n");
        Map<Integer, Integer> patternSingleFunctions = new HashMap<>();
        for (int i = 0; i < currentPatterns.size(); i++) {
            Sequence sequence = currentPatterns.get(i).getSequence();
            if (sequence.isSingleFunction()) {
                patternSingleFunctions.put(i, sequence.getFunction());
            }
        }
        
        for (int i = 0; i < currentPatterns.size(); i++) {
            Sequence sequence = currentPatterns.get(i).getSequence();
            if (excludeSingeFunctions && sequence.isSingleFunction()) continue;
            writer.write("#############\n");
            writer.write(String.valueOf(i + Constants.PATTERN_BASE) + ":\n");
            writer.write(sequence.toString(patternSingleFunctions) + "\n");
        }
    }

    /**
     * Writes the current patterns to a the writer provided, and resets the current
     * patterns.
     */
    public void writePatterns(BufferedWriter writer) throws IOException {
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
            // Since the pattern manager contains pattern shapes from previous threads, the patterns
            // need to have any instances in this thread.
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
        }
    }

    public void resetPatterns() {
        for (Pattern pattern : currentPatterns) {
            pattern.reset();
        }
    }
}
