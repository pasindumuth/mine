import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        PatternManager manager = new PatternManager();

        for (int i = 1;; i++) {
            BufferedReader reader = new BufferedReader(
                new FileReader(Constants.THREAD_DIR + "thread." + String.valueOf(i) + ".trace"));

            System.out.println("Starting to mine thread: " + String.valueOf(i));

            PatternMiner miner = new PatternMiner(manager);
            miner.mineThread(reader);
            reader.close();

            BufferedWriter writer = new BufferedWriter(
                new FileWriter(Constants.PATTERN_DIR + "thread." + String.valueOf(i) + ".patterns"));
                
            manager.flushPatterns(writer);
            writer.close();
        }
    }
}
