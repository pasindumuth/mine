import java.util.ArrayList;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class Sequence {
    
    private int functionID;
    private ArrayList<Integer> patternIDs;
    private ArrayList<Integer> count;

    public Sequence() {
        this.patternIDs = new ArrayList<>();
        this.count = new ArrayList<>();
    }

    public void setFunction(int functionID) {
        this.functionID = functionID;
    }

    public int getFunction() {
        return functionID;
    }

    public void addPatternID(Integer patternID) {
        patternIDs.add(patternID);
        count.add(1);
    }

    public void compressVeryLossy() {
        int sequenceEnd = patternIDs.size();
        for (int i = sequenceEnd - 1; i > 0; i--) {
            int candidateListLength = sequenceEnd - i;
            if (i - candidateListLength < 0) return;

            int j = i;
            for (; j < sequenceEnd; j++) {
                if (!patternIDs.get(j).equals(patternIDs.get(j - candidateListLength))) break;
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
        return patternIDs.isEmpty();
    }

    /**
     * Adds the counts of the other patternIDs to this patternIDs. The sequences must both have
     * the same list patternIDs.
     * @param otherSequence patternIDs whose elements are to merged into the current patternIDs.
     */
    public void mergeSequence(Sequence otherSequence) {
        if (patternIDs.size() != otherSequence.patternIDs.size())
            System.out.println("Error: Trying to merge non equivalent sequences; different length.");

        for (int i = 0; i < this.patternIDs.size(); i++) {
            if (!patternIDs.get(i).equals(otherSequence.patternIDs.get(i)))
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
        for (int i = 0; i < patternIDs.size(); i++) {
            hasher.putInt(patternIDs.get(i));
        }

        return hasher.hash().asInt();
    }

    /**
     * Creates a a clone with all counts set to 0.
     */
    public Sequence createEmptyClone() {
        Sequence emptyClone = new Sequence();
        for (int i = 0; i < patternIDs.size(); i++) {
            emptyClone.patternIDs.add(patternIDs.get(i));
            emptyClone.count.add(0);
        }

        return emptyClone;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("{");
        String[] sequenceElementStrings = new String[patternIDs.size() + 1];
        sequenceElementStrings[0] = String.valueOf(functionID);
        for (int i = 0; i < patternIDs.size(); i++) {
            sequenceElementStrings[i + 1] = "(" + patternIDs.get(i).toString() 
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
        for (int i = patternIDs.size() - 1; i >= sequenceEnd; i--) {
            patternIDs.remove(i);
            count.remove(i);
            assert patternIDs.size() == count.size();
        }
    }
}
