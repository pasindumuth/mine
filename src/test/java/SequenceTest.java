import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SequenceTest {

    BufferedWriter writer;

    @Before
    public void setup() throws IOException {
        // Write System.out.println messages to a file for easy debugging
        System.setOut(new PrintStream(new File(Constants.TEST_DATA_DIR + "debug"))); 
        writer = new BufferedWriter(new FileWriter(Constants.TEST_DATA_DIR + "output"));
    }

    @After
    public void cleanup() throws IOException {
        writer.close();
    }
    /**
     * Test Ideas: 
     * - We largly want to test what sequences are considered the same in our mining algorithm,
     * although it there might still be value in testing the exact form (which can vary).
     */
    
     /**
      * When two simple patterns have the same base function but have different level 1 function calls,
      * Then define the two patterns as the same.
      */
    @Test
    public void test1() throws IOException {
        // Mine trace for patterns.
        PatternManager manager = new PatternManager();
        mine("test1", manager);

        // Assert output
        ArrayList<Sequence> nonSingleFunctions = getSingleFunctionsFilteredOut(manager.getShapes());
        manager.dumpPatternManager(writer, false);
        assertEquals(nonSingleFunctions.size(), 1);
    }

    /**
     * Mine patterns from the given test file
     * @param testFile test file with trace data in the form of a standard trace
     * @param manager manager we collected the mined data into.
     */
    void mine(String testFile, PatternManager manager) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Constants.TEST_DATA_DIR + testFile));
        PatternMiner miner = new PatternMiner(manager);
        miner.mineThread(reader);
        reader.close();
    }

    ArrayList<Sequence> getSingleFunctionsFilteredOut(ArrayList<Sequence> sequences) {
        ArrayList<Sequence> filtered = new ArrayList<>();
        for (Sequence sequence : sequences) {
            if (!sequence.isSingleFunction()) {
                filtered.add(sequence);
            }
        }
        return filtered;
    }
}
