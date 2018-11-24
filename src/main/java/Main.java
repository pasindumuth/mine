import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        PatternManager manager = new PatternManager(new PatternManager.PatternDistances(new ArrayList<>()));

        for (int i = 1;; i++) {
            BufferedReader reader = new BufferedReader(
                new FileReader(Constants.THREAD_DIR + "thread." + String.valueOf(i) + ".trace"));

            System.out.println("Starting to mine thread: " + String.valueOf(i));

            PatternMiner miner = new PatternMiner(manager);
            miner.mineThread(reader);
            reader.close();

            BufferedWriter writer = new BufferedWriter(
                new FileWriter(Constants.PATTERN_DIR + "thread." + String.valueOf(i) + ".patterns"));
                
            manager.writePatterns(writer);
            manager.resetPatterns();

            BufferedWriter managerWriter = new BufferedWriter(
                new FileWriter(Constants.PATTERN_DIR + "thread." + String.valueOf(i) + ".managerDump"));

            manager.dumpPatternManager(managerWriter, false);
            writer.close();
        }
    }
}
