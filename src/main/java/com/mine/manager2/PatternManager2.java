package com.mine.manager2;

import com.mine.Constants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternManager2 {

    List<SubtraceRepresentation> patternRepresentations = new ArrayList<>();
    DistanceMap distanceMap;
    List<Integer> counts = new ArrayList<>();
    List<Integer> representationDepth = new ArrayList<>();

    public PatternManager2(DistanceMap distanceMap) {
        this.distanceMap = distanceMap;
        initializeNullPattern();
    }

    private void initializeNullPattern() {
        patternRepresentations.add(new SubtraceRepresentation(distanceMap, Constants.NULL_FUNCTION_ID));

        // Update distance map to include null pattern
        List<Double> newDistances = new ArrayList<>();
        newDistances.add(0.0);
        distanceMap.get().add(newDistances);

        // Counts
        counts.add(0);

        // Assign depth of null patterns to be 0.
        representationDepth.add(0);
    }

    public double getAcceptanceDistance(int patternId) {
        return Constants.NULL_FUNCTION_DISTANCE * representationDepth.get(patternId);
    }

    public DistanceMap getDistanceMap() {
        return distanceMap;
    }

    public int updatePatterns(SubtraceRepresentation newRepresentation) {
        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            SubtraceRepresentation representation = patternRepresentations.get(patternId);
            double acceptanceDistance = getAcceptanceDistance(patternId);
            double distance = newRepresentation.getDistance(representation);
            if (distance < acceptanceDistance) {
                counts.set(patternId, counts.get(patternId) + 1);
                return patternId;
            }
        }

        // None of the existing patterns accepts newRepresentation
        // Update distanceMap
        List<Double> newDistances = new ArrayList<>();
        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            SubtraceRepresentation representation = patternRepresentations.get(patternId);
            double distance = newRepresentation.getDistance(representation);
            newDistances.add(distance);
            distanceMap.get().get(patternId).add(distance);
        }
        newDistances.add(0.0);
        distanceMap.get().add(newDistances);

        // Update depth
        int maxDepth = 0;
        for (int patternId : newRepresentation.getPatternIds()) {
            maxDepth = Math.max(maxDepth, representationDepth.get(patternId));
        }
        representationDepth.add(maxDepth + 1);

        // Update counts
        counts.add(1);

        // Update representation list
        int newPatternId = patternRepresentations.size();
        patternRepresentations.add(newRepresentation);
        return newPatternId;
    }

    public void dumpPatterns(BufferedWriter writer) throws IOException {
        Map<Integer, Integer> singleFunctionPatterns = new HashMap<>(); // maps patternIds of single function patterns to their base functions.
        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            if (representationDepth.get(patternId) == 1) {
                singleFunctionPatterns.put(patternId, patternRepresentations.get(patternId).getBaseFunction());
            }
        }

        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            if (singleFunctionPatterns.containsKey(patternId)) {
                continue;
            }
            writer.write("#############\n");
            writer.write(String.valueOf(patternId + Constants.PATTERN_BASE) + ":\n");
            writer.write(patternRepresentations.get(patternId).toString(singleFunctionPatterns) + "\n");
            writer.write(String.valueOf(counts.get(patternId)) + " OCCURRENCES.\n");
            writer.write("\n");
        }

//        for (int depth : representationDepth) {
//            writer.write(String.valueOf(depth) + " ");
//        }
//
//        writer.write("\n");
//        writer.write(distanceMap.toString());
    }
}