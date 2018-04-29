import java.util.ArrayList;
import java.util.Map;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class Sequence {

    /**
     * This class compresses subtraces of events, and holds onto metadata about
     * that compression. These subtraces have a base function, which the subtrace
     * occurs in. `functionID` stores this base function. `shape` is a list of
     * pattern IDs occurred in this subtrace. `count` keeps count of the number
     * of times a pattern in `shape` get's compressed.
     */
    
    private int functionID;
    private ArrayList<Integer> shape;
    private ArrayList<Integer> count;

    public Sequence() {
        this.shape = new ArrayList<>();
        this.count = new ArrayList<>();
    }

    public void setFunction(int functionID) {
        this.functionID = functionID;
    }

    public int getFunction() {
        return functionID;
    }

    public void addPatternID(Integer patternID) {
        shape.add(patternID);
        count.add(1);
    }

    public void compressVeryLossy() {
        int sequenceEnd = shape.size();
        for (int i = sequenceEnd - 1; i > 0; i--) {
            int candidateListLength = sequenceEnd - i;
            if (i - candidateListLength < 0) return;

            int j = i;
            for (; j < sequenceEnd; j++) {
                if (!shape.get(j).equals(shape.get(j - candidateListLength))) break;
            }

            if (j < sequenceEnd) continue;
            for (j = i; j < sequenceEnd; j++) {
                count.set(j - candidateListLength, count.get(j - candidateListLength) + count.get(j));
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
     * Adds the counts of the other shape to this shape. The sequences must both have
     * the same list shape.
     * @param otherSequence shape whose elements are to merged into the current shape.
     */
    public void mergeSequence(Sequence otherSequence) {
        if (shape.size() != otherSequence.shape.size())
            System.out.println("Error: Trying to merge non equivalent sequences; different length.");

        for (int i = 0; i < this.shape.size(); i++) {
            if (!shape.get(i).equals(otherSequence.shape.get(i)))
                System.out.println("Error: Trying to merge non equivalent sequences; different patternID.");

            count.set(i, count.get(i) + otherSequence.count.get(i));
        }
    }

    /**
     * Hash function calculated by base function and pattern sequence.
     */
    public int hash() {
        Hasher hasher = Hashing.murmur3_32(Constants.MURMUR_SEED).newHasher();
        hasher.putInt(functionID);
        for (int i = 0; i < shape.size(); i++) {
            hasher.putInt(shape.get(i));
        }

        return hasher.hash().asInt();
    }

    /**
     * Creates a a clone with all counts set to 0.
     */
    public Sequence createEmptyClone() {
        Sequence emptyClone = new Sequence();
        emptyClone.functionID = functionID;
        for (int i = 0; i < shape.size(); i++) {
            emptyClone.shape.add(shape.get(i));
            emptyClone.count.add(0);
        }

        return emptyClone;
    }

    public String toString(Map<Integer, Integer> singleFunctions) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        String[] sequenceElementStrings = new String[shape.size() + 1];
        sequenceElementStrings[0] = String.valueOf(functionID);
        for (int i = 0; i < shape.size(); i++) {
            int id = singleFunctions.containsKey(shape.get(i)) ? singleFunctions.get(shape.get(i)) : shape.get(i) + Constants.PATTERN_BASE;
            sequenceElementStrings[i + 1] = "(" + String.valueOf(id)
                                          + " => " + count.get(i).toString() + ")";
        }

        s.append(String.join(", ", sequenceElementStrings));
        s.append("}");

        return s.toString();
    }

    /**
     * Removes all elements that come after sequenceEnd
     */
    private void cropEnd(int sequenceEnd) {
        for (int i = shape.size() - 1; i >= sequenceEnd; i--) {
            shape.remove(i);
            count.remove(i);
            assert shape.size() == count.size();
        }
    }
}
