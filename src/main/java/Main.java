import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        Map<Integer, Pattern> patterns = new HashMap<>();

        for (int i = 1;; i++) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(
                    new FileReader(Constants.THREAD_DIR + "thread." + String.valueOf(i) + ".trace"));
            } catch (FileNotFoundException e) {
                break;
            }

            System.out.println("Starting to mine thread: " + String.valueOf(i));
            patterns = mineThread(patterns, reader);
    
            BufferedWriter writer = new BufferedWriter(
                new FileWriter(Constants.PATTERN_DIR + "thread." + String.valueOf(i) + ".patterns"));
                
            PatternWriter patternWriter = new PatternWriter(patterns, writer);
            patternWriter.write();
    
            reader.close();
            writer.close();

            for (Pattern pattern : patterns.values())
                pattern.reset();
        }
    }

    public static Map<Integer, Pattern> mineThread(Map<Integer, Pattern> currentPatterns, BufferedReader reader) 
        throws IOException {
        
        PatternMiner miner = new PatternMiner(currentPatterns);
        ArrayList<Integer> funcStack = new ArrayList<>();
        ArrayList<Long> startTimes = new ArrayList<>();

        String line = reader.readLine();

        int count = 0;
        while (line != null) {
            if (count % 1000000 == 0) System.out.println(count);
            count++;

            String[] record = line.split("\t");
            
            int functionID = Integer.parseInt(record[0]);
            int dir = Integer.parseInt(record[1]);
            long time = Long.parseLong(record[2]);

            if (dir == Constants.FUNCTION_EXIT) {
                int lastFunctionID = funcStack.remove(funcStack.size() - 1);
                if (lastFunctionID != functionID) 
                    System.out.println("Error: existed a function without entering it");

                long startTime = startTimes.remove(startTimes.size() - 1);
                miner.processEvent(functionID, funcStack.size(), startTime, time);
            } else {
                funcStack.add(functionID);
                startTimes.add(time);
            }

            line = reader.readLine();
        }

        miner.finish();
        return miner.getPatterns();
    }
}
