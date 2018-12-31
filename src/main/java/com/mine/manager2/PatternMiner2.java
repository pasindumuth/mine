package com.mine.manager2;

import com.mine.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class PatternMiner2 {

    private PatternManager2 manager;
    private ArrayList<SubtraceRepresentation> representationForLevel = new ArrayList<>();
    private int stackLevel = 0;

    /**
     * We can imagine a fictitious function entrance right at the beginning, acting as the base function for the whole
     * trace. This function is at stack level 0.
     */
    public PatternMiner2(PatternManager2 manager) {
        this.manager = manager;
        representationForLevel.add(new SubtraceRepresentation(manager.getDistanceMap(), Constants.BASE_FUNCTION_ID));
    }

    /**
     * Reads every line from the thread and adds any pattern that it detects into the pattern manager.
     */
    public void mineThread(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        int count = 0;
        while (line != null) {
            if (count % 1000000 == 0) System.out.println(count);
            if (count == 6000000) break;
            count++;

            String[] record = line.split("\t");

            int functionID = Integer.parseInt(record[0]);
            int dir = Integer.parseInt(record[1]);
            long time = Long.parseLong(record[2]);

            processEvent(functionID, dir, time);

            line = reader.readLine();
        }
    }

    /**
     * Fundamentally, this function defines how (complete) function call sequences map to patterns.
     */
    public void processEvent(int functionID, int dir, long time) {
        if (dir == Constants.FUNCTION_ENTER) {
            SubtraceRepresentation newRepresentation = new SubtraceRepresentation(manager.getDistanceMap(), functionID);
            stackLevel++;
            if (stackLevel < representationForLevel.size()) representationForLevel.set(stackLevel, newRepresentation);
            else representationForLevel.add(newRepresentation);
        } else {
            // The highest sequence is finished. Update the set of patterns.
            SubtraceRepresentation finishedRepresentation = representationForLevel.get(stackLevel);
            int newPatternId = manager.updatePatterns(finishedRepresentation);
            stackLevel--;

            // Update representation below with the new pattern instance.
            SubtraceRepresentation belowRepresentation = representationForLevel.get(stackLevel);
            belowRepresentation.addPatternId(newPatternId);
        }
    }
}
