import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.mine.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SequenceTest {

    private static BufferedWriter writer;

    @BeforeClass
    public static void setup() throws IOException {
        // Write System.out.println messages to a file for easy debugging
        System.setOut(new PrintStream(new File(Constants.TEST_DATA_DIR + "debug"))); 
        writer = new BufferedWriter(new FileWriter(Constants.TEST_DATA_DIR + "output"));
    }

    @AfterClass
    public static void cleanup() throws IOException {
        writer.close();
    }
    
     /**
      * When two simple patterns have the same base function but have different level 1 function calls,
      * then define the two patterns as the same.
      */
    @Test
    public void test1() throws IOException {
        // Mine trace for patterns.
        PatternManager manager = new PatternManager(new PatternManager.PatternDistances(new ArrayList<>()));
        mine("test1", manager);

        // Assert output
        ArrayList<Sequence> nonSingleFunctions = getSingleFunctionsFilteredOut(manager.getShapes());
        manager.dumpPatternManager(writer, false);
        assertEquals(nonSingleFunctions.size(), 1);
    }

    @Test
    public void testSequenceElementDistance() {
        PatternManager.PatternDistances patternDistances = getBasicDistances();
        
        SequenceElement e1 = new SequenceElement(patternDistances, new int[][] {
            {1, 1}  
        });

        SequenceElement e2 = new SequenceElement(patternDistances, new int[][] {
            {2, 1}
        });

        SequenceElement e3 = new SequenceElement(patternDistances, new int[][] {
            {2, 1}, {1, 1}
        });

        SequenceElement eNull = SequenceElement.createNullSequenceElement(patternDistances);
        assertEquals(e1.getDistance(eNull), 0.5);
        assertEquals(e1.getDistance(e1), 0.0);
        assertEquals(e2.getDistance(e1), 1.0);
        assertEquals(e3.getDistance(e1), 1.0);
        assertEquals(e3.getDistance(e3), 0.0);
    }

    @Test
    public void testSingleFunctionSequenceDistance() {
        PatternManager.PatternDistances patternDistances = getBasicDistances();

        // Sequences with just the base pattern.
        Sequence s1 = new Sequence(patternDistances, Constants.NULL_FUNCTION_ID);
        Sequence s2 = new Sequence(patternDistances, 0);
        Sequence s3 = new Sequence(patternDistances, 1);
        assertEquals(s1.getDistance(s2), 0.5);
        assertEquals(s2.getDistance(s2), 0.0);
        assertEquals(s2.getDistance(s3), 1.0);
    }

    @Test
    public void testBasicSequenceDistance() {
        PatternManager.PatternDistances patternDistances = getBasicDistances();

        Sequence s1 = new Sequence(patternDistances, 0);
        s1.addPatternID(1);
        Sequence s2 = new Sequence(patternDistances, 0);
        s2.addPatternID(1);
        assertEquals(s1.getDistance(s2), 0.0);
        Sequence s3 = new Sequence(patternDistances, 0);
        assertEquals(s1.getDistance(s3), 0.5);
    }

    private PatternManager.PatternDistances getBasicDistances() {
        double[][] distances = {
            {0.0, 0.5, 1.0},
            {0.5, 0.0, 1.0},
            {1.0, 1.0, 0.0}
        };
        assertTrue(verifyMetric(distances));
        return createDistances(distances);
    }

    /** When we make distance maps for the purpose of testing, this makes sure that it is actually a metric. */
    private boolean verifyMetric(double[][] distances) {
        // Make sure this is square
        int m = distances.length;
        for (int i = 0; i < m; i++) {
            if (distances[i].length != m) return false;
        }

        // Verify only the diagonal is 0.0
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                if (i == j && distances[i][j] != 0.0) return false;
                if (i != j && distances[i][j] == 0.0) return false; 
            }
        }

        // This is symmetric, and that non diagonal elements are non 0.0
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < m; j++) {
                if (distances[i][j] == 0.0) return false;
                if (distances[i][j] != distances[j][i]) return false;
            }
        }

        // Verify the distance map is transitive
        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < m; j++) {
                for (int k = 0; k < m; k++) {
                    if (distances[i][j] > distances[i][k] + distances[k][j]) return false;
                }
            }
        }

        // The distance map is a metric
        return true;
    }

    private PatternManager.PatternDistances createDistances(double[][] distances) {
        List<List<Double>> distanceLists = new ArrayList<>();
        for (int i = 0; i < distances.length; i++) {
            List<Double> distanceList = new ArrayList<>();
            for (int j = 0; j < distances[i].length; j++) {
                distanceList.add(distances[i][j]);
            }
            distanceLists.add(distanceList);
        }
        return new PatternManager.PatternDistances(distanceLists);
    }

    /**
     * Mine patterns from the given test file
     * @param testFile test file with trace data in the form of a standard trace
     * @param manager manager we collected the mined data into.
     */
    private void mine(String testFile, PatternManager manager) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Constants.TEST_DATA_DIR + testFile));
        PatternMiner miner = new PatternMiner(manager);
        miner.mineThread(reader);
        reader.close();
    }

    private ArrayList<Sequence> getSingleFunctionsFilteredOut(List<Sequence> sequences) {
        ArrayList<Sequence> filtered = new ArrayList<>();
        for (Sequence sequence : sequences) {
            if (!sequence.isSingleFunction()) {
                filtered.add(sequence);
            }
        }
        return filtered;
    }
}
