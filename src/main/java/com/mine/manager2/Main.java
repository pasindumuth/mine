package com.mine.manager2;

import com.mine.Constants;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    private static final int START_THREAD = 1;
    private static final int END_THREAD = 11;

    public static void main(String[] args) throws IOException {
        PatternManager2 manager = new PatternManager2(new DistanceMap(new ArrayList<>()));
        Long absoluteStartTime = Long.MAX_VALUE;
        Long absoluteEndTime = Long.MIN_VALUE;

        // Find absolute start and end across all threads
        for (int i = START_THREAD; i <= END_THREAD; i++) {
            BufferedReader reader = new BufferedReader(
                    new FileReader(Constants.THREAD_DIR + "thread." + i + ".trace"));
            String line = reader.readLine();
            Long threadStartTime = Long.parseLong(line.split("\t")[2]);
            absoluteStartTime = Math.min(absoluteStartTime, threadStartTime);

            // Get to last line
            String lastLine = line;
            while (line != null) {
                lastLine = line;
                line = reader.readLine();
            }
            Long threadEndTime = Long.parseLong(lastLine.split("\t")[2]);
            absoluteEndTime = Math.max(absoluteEndTime, threadEndTime);
        }

        // Write metadata
        BufferedWriter metadataWriter = new BufferedWriter(
                new FileWriter(Constants.PATTERN_DIR + "metadata"));

        metadataWriter.write(new JSONObject()
                .put("absoluteStartTime", absoluteStartTime.toString())
                .put("absoluteEndTime", absoluteEndTime.toString())
                .put("duration", absoluteEndTime - absoluteStartTime)
                .toString());

        metadataWriter.close();

        for (int i = START_THREAD; i <= END_THREAD; i++) { // For now, just do one thread to see what we get.
            // Read and process data
            BufferedReader reader = new BufferedReader(
                    new FileReader(Constants.THREAD_DIR + "thread." + i + ".trace"));
            System.out.println("Starting to mine thread: " + i);
            PatternMiner2 miner = new PatternMiner2(manager);
            miner.mineThread(reader);
            reader.close();

            // Write data
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(Constants.PATTERN_DIR + "thread." + i + ".patterns"));
            manager.dumpPatterns(writer, absoluteStartTime);
            manager.resetPatterns();
            writer.close();
        }
    }
}
