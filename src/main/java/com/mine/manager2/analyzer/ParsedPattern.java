package com.mine.manager2.analyzer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class ParsedPattern {

    int patternId;
    int depth;
    TreeMap<Integer, Integer> patternIdCounts = new TreeMap<>();
    TreeMap<Integer, Integer> baseFunctionCounts = new TreeMap<>();

    ParsedPattern(JSONObject serializedObject) {
        patternId = serializedObject.getInt("id");
        JSONObject serializedRepresentation = serializedObject.getJSONObject("representation");
        depth = serializedRepresentation.getInt("depth");
        JSONArray serializedBaseFunctionCounts = serializedRepresentation.getJSONArray("baseFunctions");
        for (int i = 0; i < serializedBaseFunctionCounts.length(); i++) {
            JSONObject baseFunctionCount = serializedBaseFunctionCounts.getJSONObject(i);
            baseFunctionCounts.put(baseFunctionCount.getInt("baseFunction"), baseFunctionCount.getInt("count"));
        }
        JSONArray serializedPatternIdCounts = serializedRepresentation.getJSONArray("patternIds");
        for (int i = 0; i < serializedPatternIdCounts.length(); i++) {
            JSONObject patternIdCount = serializedPatternIdCounts.getJSONObject(i);
            patternIdCounts.put(patternIdCount.getInt("patternId"), patternIdCount.getInt("count"));
        }
    }

    public String prettyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Add depth
        sb.append(depth);
        sb.append(", ");

        // Add base function set
        sb.append("[");
        List<String> baseFunctionCountStrings = new ArrayList<>();
        for (Map.Entry<Integer, Integer> baseFunctionCount : baseFunctionCounts.entrySet()) {
            baseFunctionCountStrings.add(baseFunctionCount.getKey() + " -> " + baseFunctionCount.getValue());
        }
        sb.append(String.join(", ", baseFunctionCountStrings));
        sb.append("], ");

        // Add patternId set
        sb.append("[");
        List<String> patternIdCountStrings = new ArrayList<>(); // single function patterns will just use their base functions
        for (Map.Entry<Integer, Integer> patternIdCount : patternIdCounts.entrySet()) {
            patternIdCountStrings.add(patternIdCount.getKey() + " -> " + patternIdCount.getValue());
        }
        sb.append(String.join(", ", patternIdCountStrings));
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }
}
