package com.mine.manager2;

import com.mine.Constants;
import com.mine.manager2.analyzer.Analyzer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        Constants.RunMode mode = Constants.RunMode.MINE;
        switch (mode) {
            case ANALYZE:
                // Run the analyzer
                Analyzer analyzer = new Analyzer();
                analyzer.analyze();
                break;
            case MINE:
                // Mine patterns
                minePatterns();
                break;
            case SPACE_FIX:
                // Fix pattern indentation for readability in case we accidently indent with 0
                BufferedReader reader = new BufferedReader(
                        new FileReader(Constants.PATTERN_DIR + "thread.1.patterns"));
                JSONTokener tokener = new JSONTokener(reader);
                JSONArray patterns = new JSONArray(tokener);
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(Constants.PATTERN_DIR + "thread.1.patterns"));
                writer.write(patterns.toString(2));
                break;
            default:
                throw new IllegalArgumentException("No such RunMode supported");
        }
    }

    private static void minePatterns()  throws IOException {
        PatternManager2 manager = new PatternManager2(new DistanceMap(new ArrayList<>()));
        Long absoluteStartTime = Long.MAX_VALUE;
        Long absoluteEndTime = Long.MIN_VALUE;

        // Find absolute start and end across all threads
        for (int i = Constants.START_THREAD; i <= Constants.END_THREAD; i++) {
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

        for (int i = Constants.START_THREAD; i <= Constants.END_THREAD; i++) {
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
