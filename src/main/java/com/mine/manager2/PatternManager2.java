package com.mine.manager2;

import com.mine.Constants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternManager2 {

    DistanceMap distanceMap;
    List<Integer> representationDepth = new ArrayList<>();
    List<SubtraceRepresentation> patternRepresentations = new ArrayList<>();
    List<Pattern2> patternInstances = new ArrayList<>();

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

        // Update instances
        patternInstances.add(new Pattern2(Constants.NULL_PATTERN_ID));

        // Assign depth of null patterns to be 0.
        representationDepth.add(0);
    }

    public double getAcceptanceDistance(int patternId) {
        return Constants.NULL_FUNCTION_DISTANCE * representationDepth.get(patternId);
    }

    public DistanceMap getDistanceMap() {
        return distanceMap;
    }

    public int updatePatterns(RepresentationContainer container) {
        SubtraceRepresentation newRepresentation = container.getRepresentation();
        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            SubtraceRepresentation representation = patternRepresentations.get(patternId);
            double acceptanceDistance = getAcceptanceDistance(patternId);
            double distance = newRepresentation.getDistance(representation);
            if (distance < acceptanceDistance) {
                patternInstances.get(patternId).addInstance(container.getStartTime(), container.getEndTime());
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

        // Update representation list
        int newPatternId = patternRepresentations.size();
        patternRepresentations.add(newRepresentation);

        // Update instances
        Pattern2 pattern = new Pattern2(newPatternId);
        pattern.addInstance(container.getStartTime(), container.getEndTime());
        patternInstances.add(pattern);

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

            Pattern2 pattern = patternInstances.get(patternId);
            List<Long> startTimes = pattern.getStartTimes();
            List<Long> durations = pattern.getDurations();
            // Since the pattern manager contains pattern shapes from previous threads, the patterns
            // need to have any instances in this thread.
            if (startTimes.size() == 0) {
                continue;
            }

            writer.write("#############\n");
            writer.write(String.valueOf(patternId + Constants.PATTERN_BASE) + ":\n");
            writer.write(patternRepresentations.get(patternId).toString(singleFunctionPatterns) + "\n");

            writer.write(String.valueOf(startTimes.size()) + " OCCURRENCES.\n");
            for (int i = 0; i < startTimes.size(); i++) {
                writer.write(String.valueOf(startTimes.get(i)));
                writer.write(" : ");
                writer.write(String.valueOf(durations.get(i)));
                writer.write("\n");
            }
        }
    }
}