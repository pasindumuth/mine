package com.mine.manager2;

import com.mine.Constants;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatternManager2 {

    DistanceMap distanceMap;
    List<SubtraceRepresentation> patternRepresentations = new ArrayList<>();
    List<Pattern2> patternInstances = new ArrayList<>();

    public PatternManager2(DistanceMap distanceMap) {
        this.distanceMap = distanceMap;
        initializeNullPattern();
    }

    private void initializeNullPattern() {
        SubtraceRepresentation nullRepresentation = new SubtraceRepresentation(distanceMap, Constants.NULL_FUNCTION_ID);
        nullRepresentation.setDepth(0);
        patternRepresentations.add(nullRepresentation);

        // Update distance map to include null pattern
        List<Double> newDistances = new ArrayList<>();
        newDistances.add(0.0);
        distanceMap.get().add(newDistances);

        // Update instances
        patternInstances.add(new Pattern2(Constants.NULL_PATTERN_ID, nullRepresentation));
    }

    public double getAcceptanceDistance(int patternId) {
        return Constants.NULL_FUNCTION_DISTANCE * patternRepresentations.get(patternId).getDepth();
    }

    public DistanceMap getDistanceMap() {
        return distanceMap;
    }

    public int updatePatterns(RepresentationContainer container) {
        SubtraceRepresentation newRepresentation = container.getRepresentation();

        // Update depth
        int maxDepth = 0;
        for (int patternId : newRepresentation.getPatternIds()) {
            maxDepth = Math.max(maxDepth, patternRepresentations.get(patternId).getDepth());
        }
        newRepresentation.setDepth(maxDepth + 1);

        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            SubtraceRepresentation representation = patternRepresentations.get(patternId);
            double acceptanceDistance = getAcceptanceDistance(patternId);
            double distance = newRepresentation.getDistance(representation);
            // Accept subtrace only from patterns whose depths which are greater.
            if (distance < acceptanceDistance && representation.getDepth() >= newRepresentation.getDepth()) {
                patternInstances.get(patternId).addInstance(container);
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

        // Update representation list
        int newPatternId = patternRepresentations.size();
        patternRepresentations.add(newRepresentation);

        // Update instances
        Pattern2 pattern = new Pattern2(newPatternId, newRepresentation);
        pattern.addInstance(container);
        patternInstances.add(pattern);

        return newPatternId;
    }

    public void resetPatterns() {
        for (Pattern2 pattern : patternInstances) {
            pattern.reset();
        }
    }

    public void dumpPatterns(BufferedWriter writer) throws IOException {
        verifyInstances();
        Map<Integer, Integer> singleFunctionPatterns = new HashMap<>(); // maps patternIds of single function patterns to their base functions.
        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            if (patternRepresentations.get(patternId).getDepth() == 1) {
                singleFunctionPatterns.put(patternId, patternRepresentations.get(patternId).getBaseFunction());
            }
        }

        // Since the pattern manager contains pattern shapes from previous threads, the patterns
        // need to have any instances in this thread.
        List<Pattern2> filteredPatternInstances = patternInstances.stream().filter(pattern ->
                !(singleFunctionPatterns.containsKey(pattern.getPatternID()) || pattern.getStartTimes().isEmpty())
        ).collect(Collectors.toList());

//        List<Pattern2> sortedPatternInstances = filteredPatternInstances.stream().sorted((p1, p2) ->
//                p2.getStartTimes().size() - p1.getStartTimes().size()
//        ).collect(Collectors.toList());

        for (Pattern2 pattern : filteredPatternInstances) {
            int patternId = pattern.getPatternID();
            List<Long> startTimes = pattern.getStartTimes();
            List<Long> durations = pattern.getDurations();

            writer.write("#############\n");
            writer.write(String.valueOf(patternId + Constants.PATTERN_BASE) + ":\n");
            writer.write(new JSONObject()
                    .put("pattern", pattern.toString(singleFunctionPatterns))
                    .put("representation", patternRepresentations.get(patternId).toString(singleFunctionPatterns))
                    .toString() + "\n");

            writer.write(String.valueOf(startTimes.size()) + " OCCURRENCES.\n");
            for (int i = 0; i < startTimes.size(); i++) {
                writer.write(String.valueOf(startTimes.get(i)));
                writer.write(" : ");
                writer.write(String.valueOf(durations.get(i)));
                writer.write("\n");
            }
        }

        writer.write("\n");
        writer.write("\n");

        for (Pattern2 pattern1 : filteredPatternInstances) {
            for (Pattern2 pattern2 : filteredPatternInstances) {
                writer.write(String.valueOf(distanceMap.getDistance(pattern1.getPatternID(), pattern2.getPatternID())));
                writer.write("\t");
            }
            writer.write("\n");
        }
    }

    /**
     * Verifies:
     * 1. All patterns of a given depth are disjoint.
     */
    private void verifyInstances() {
        int maxDepth = 0;
        for (Pattern2 pattern : patternInstances) {
            maxDepth = Math.max(maxDepth, pattern.getDepth());
        }

        List<List<Pattern2>> patternsByDepth = new ArrayList<>();
        // Initialized patternsByDepth
        for (int i = 0; i <= maxDepth; i++) {
            patternsByDepth.add(new ArrayList<>());
        }

        // Populate ordered patterns
        for (Pattern2 pattern : patternInstances) {
            patternsByDepth.get(pattern.getDepth()).add(pattern);
        }

        // Verify patterns of the same depth are disjoint
        for (List<Pattern2> patterns : patternsByDepth) {
            List<Interval> intervals = new ArrayList<>();
            for (Pattern2 pattern : patterns) {
                List<Long> startTimes = pattern.getStartTimes();
                List<Long> durations = pattern.getDurations();
                for (int i = 0; i < startTimes.size(); i++) {
                    intervals.add(new Interval(startTimes.get(i), durations.get(i)));
                }
            }
            intervals.sort((i1, i2) -> {
                if (i1.startTime - i2.startTime > 0) {
                    return 1;
                } else if (i1.startTime == i2.startTime) {
                    return 0;
                } else {
                    return -1;
                }
            });
            for (int i = 0; i < intervals.size() - 1; i++) {
                if (!(intervals.get(i + 1).startTime >= intervals.get(i).startTime + intervals.get(i).duration)) {
                    System.out.println("Failure");
                }
            }
        }

        System.out.println("Patterns verified: all good.");
    }

    private static class Interval {
        long startTime;
        long duration;

        Interval(long startTime, long duration) {
            this.startTime = startTime;
            this.duration = duration;
        }
    }
}