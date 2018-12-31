package com.mine;

import java.util.ArrayList;
import java.util.Map;

public class Sequence {

    /**
     * This class compresses subtraces of events, and holds onto metadata about
     * that compression. These subtraces have a base function, which the subtrace
     * occurs in. `functionID` stores this base function. `shape` is a list of
     * pattern IDs occurred in this subtrace. `count` keeps count of the number
     * of times a pattern in `shape` get's compressed.
     */
    private PatternManager.PatternDistances patternDistances;
    private int functionID;
    private ArrayList<SequenceElement> shape = new ArrayList<>();

    public Sequence(PatternManager.PatternDistances patternDistances, int functionID) {
        this.patternDistances = patternDistances;
        this.functionID = functionID;
    }

    public int getFunction() {
        return functionID;
    }

    public void addPatternID(Integer patternID) {
        shape.add(new SequenceElement(patternDistances, patternID));
    }

    /**
     * Lossy compression alogrithm. Checks if there is a suffix that is equivalent 
     * to the suffix of the same length in the prefix, and removes it.
     */
    public void compressVeryLossy() {
        int sequenceEnd = shape.size();
        for (int i = sequenceEnd - 1; i > 0; i--) {
            // The shape.subList(i, sequenceEnd) is the current candidate list for compression.
            int candidateListLength = sequenceEnd - i;
            if (i - candidateListLength < 0) return;

            int j = i;
            for (; j < sequenceEnd; j++) {
                if (!shape.get(j - candidateListLength).canMerge(shape.get(j))) break;
            }

            if (j < sequenceEnd) continue;
            for (j = i; j < sequenceEnd; j++) {
                shape.get(j - candidateListLength).merge(shape.get(j));
            }

            cropEnd(i);
            return;
        }
    }

    /**
     * Single functions that don't call any other functions, are, by definition, patterns.
     * These are usually unintersting, so this function identifies them.
     */
    public boolean isSingleFunction() {
        return shape.isEmpty();
    }

    /**
     * Sequences can be merged if they are the same length and the SequenceElements 
     * can be merged.
     */
    public boolean canMerge(Sequence otherSequence) {
        if (this.functionID != otherSequence.functionID) {
            return false;
        }

        if (this.shape.size() != otherSequence.shape.size()) {
            return false;
        }
        
        for (int i = 0; i < this.shape.size(); i++) {
            if (!this.shape.get(i).canMerge(otherSequence.shape.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Adds the counts of the other shape to this shape. The sequences must both have
     * the same list shape.
     * 
     * WARNING: this operation might break the PATTERN_SIMILARITY_THRESHOLD
     * requirement in the SequenceElements. We must be sure the elements can be 
     * merged before merging.
     * @param otherSequence shape whose elements are to be merged into the current shape.
     */
    public void merge(Sequence otherSequence) {
        if (Constants.RUN_MODE == Constants.DEBUG) {
            if (this.shape.size() != otherSequence.shape.size())
                System.out.println("Error: Trying to merge non equivalent sequences; different length.");
        }

        for (int i = 0; i < this.shape.size(); i++) {
            if (Constants.RUN_MODE == Constants.DEBUG) {
                if (!this.shape.get(i).canMerge(otherSequence.shape.get(i)))
                    System.out.println("Error: Trying to merge non equivalent sequences; different patternID.");
            }

            this.shape.get(i).merge(otherSequence.shape.get(i));
        }
    }

    /**
     * Computes distance between this sequence and `otherSequence`. We require
     * this distance to be a metric.
     * 
     * This distance we choose is the edit distance of the shapes. The elements of the 
     * shapes are SequenceElements. Since SequenceElements (with the nullSequenceElement) form 
     * a metric space, it can be proven that edit distance is a metric. Thus, this function 
     * turns the set of all sequences into a metric space.
     * 
     * We would also like for shapes with different base functions to be considered very 
     * far away (and thus never be grouped together similar), so we add a large penalty 
     * if the base function differ (this penalty, coupled with the edit distance, still
     * results in a metric).
     * 
     * Research Notes: prove the above, and prove that the null pattern extension is also a metric.
     * Note that the addition of null must still make this a metric, which is possible since
     * the distances between existing elements were <= 1.0f.
     */
    public double getDistance(Sequence otherSequence) {

        // Compute the edit distance between the shapes
        // TO-DO: Doc how null pattern, and null sequence element, form metric spaces just the same.
        SequenceElement nullSequenceElement = SequenceElement.createNullSequenceElement(patternDistances);

        ArrayList<SequenceElement> s1 = this.shape;
        ArrayList<SequenceElement> s2 = otherSequence.shape;

        double[] ed = new double[s2.size() + 1]; // already initialized to 0
        ed[0] = 0;
        for (int i = 1; i < s2.size() + 1; i++) {
            ed[i] = ed[i - 1] + s2.get(i - 1).getDistance(nullSequenceElement);
        }
        
        for (int i = 1; i < s1.size() + 1; i++) {
            double prevEd = ed[0];
            ed[0] += s1.get(i - 1).getDistance(nullSequenceElement);
            for (int j = 1; j < s2.size() + 1; j++) {
                double d1 = prevEd + s2.get(j - 1).getDistance(s1.get(i - 1));
                double d2 = ed[j] + s1.get(i - 1).getDistance(nullSequenceElement);
                double d3 = ed[j - 1] + s2.get(j - 1).getDistance(nullSequenceElement);

                prevEd = ed[j];
                ed[j] = Math.min(Math.min(d1, d2), d3);
            }
        }

        double shapeDistance = ed[s2.size()];
        double functionDistance = getFunctionDistance(this.functionID, otherSequence.functionID);
        return shapeDistance + functionDistance;
    }

    /**
     * Creates a clone with all counts set to 0.
     */
    public Sequence createEmptyClone() {
        Sequence emptyClone = new Sequence(patternDistances, functionID);
        for (int i = 0; i < this.shape.size(); i++) {
            emptyClone.shape.add(shape.get(i).createEmptyClone());
        }

        return emptyClone;
    }

    public String toString(Map<Integer, Integer> singleFunctions) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        ArrayList<String> sequenceElementStrings = new ArrayList<>();
        sequenceElementStrings.add(String.valueOf(functionID));
        for (int i = 0; i < shape.size(); i++) {
            sequenceElementStrings.add(shape.get(i).toString(singleFunctions));
        }

        s.append(String.join(", ", sequenceElementStrings));
        s.append("}");

        return s.toString();
    }

    /**
     * Removes all elements that come after sequenceEnd
     */
    private void cropEnd(int sequenceEnd) {
        for (int i = shape.size() - 1; i >= sequenceEnd; i--) 
            shape.remove(i);
    }

    /**
     * This is a separate metric over the set of function ids. All non null function are 
     * distance 1.0 away, while null is some <= 1.0 distance away. We can verify easily
     * this forms a metric.
     */
    private double getFunctionDistance(int f1, int f2) {
        if (f1 == f2) 
            return 0.0;
        else if (f1 == Constants.NULL_FUNCTION_ID || f2 == Constants.NULL_FUNCTION_ID) 
            return Constants.NULL_FUNCTION_DISTANCE;
        else
            return Constants.FUNCTION_DISTANCE;
    }
}
