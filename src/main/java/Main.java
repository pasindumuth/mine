import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        PatternManager.initializeNullPattern();

        for (int i = 1;; i++) {
            BufferedReader reader = new BufferedReader(
                new FileReader(Constants.THREAD_DIR + "thread." + String.valueOf(i) + ".trace"));

            System.out.println("Starting to mine thread: " + String.valueOf(i));
            mineThread(reader);
            reader.close();

            BufferedWriter writer = new BufferedWriter(
                new FileWriter(Constants.PATTERN_DIR + "thread." + String.valueOf(i) + ".patterns"));
                
            PatternManager.flushPatterns(writer);
            writer.close();
        }
    }

    public static void mineThread(BufferedReader reader) 
        throws IOException {
        
        PatternMiner miner = new PatternMiner();
        String line = reader.readLine();

        int count = 0;
        while (line != null) {
            if (count % 1000000 == 0) System.out.println(count);
            count++;

            String[] record = line.split("\t");
            
            int functionID = Integer.parseInt(record[0]);
            int dir = Integer.parseInt(record[1]);
            long time = Long.parseLong(record[2]);

            miner.processEvent(functionID, dir, time);

            line = reader.readLine();
        }
    }
}
