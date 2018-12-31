package com.mine.manager2;

import com.mine.Constants;

import java.io.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        PatternManager2 manager = new PatternManager2(new DistanceMap(new ArrayList<>()));

        for (int i = 1; i < 2; i++) { // For now, just do one thread to see what we get.
            BufferedReader reader = new BufferedReader(
                    new FileReader(Constants.THREAD_DIR + "thread." + String.valueOf(i) + ".trace"));

            System.out.println("Starting to mine thread: " + String.valueOf(i));

            PatternMiner2 miner = new PatternMiner2(manager);
            miner.mineThread(reader);
            reader.close();

            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(Constants.PATTERN_DIR + "thread." + String.valueOf(i) + ".patterns"));

            manager.dumpPatterns(writer);
            writer.close();
        }
    }
}
