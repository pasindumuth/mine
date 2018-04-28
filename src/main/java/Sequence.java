import java.util.ArrayList;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class Sequence {
    
    private ArrayList<SequenceElement> sequence;

    public Sequence() {
        this.sequence = new ArrayList<SequenceElement>();
    }

    public void add(SequenceElement element) {
        sequence.add(element);
    }

    public void compressVeryLossy() {
        int sequenceEnd = sequence.size();
        SequenceElement lastElement = sequence.get(sequenceEnd - 1);
        for (int i = sequenceEnd - 1; i > 0; i--) {
            if (sequence.get(i - 1).getType() == lastElement.getType() &&
                (lastElement.getType() == Constants.TYPE_SUB_PATTERNS ||
                sequence.get(i - 1).getFunction() == lastElement.getFunction())) {

                int candidateListLength = sequenceEnd - i;
                if (i - candidateListLength < 0) return; 
                
                if (!same(i, sequenceEnd, i - candidateListLength, i)) continue;
                
                for (int i1 = i; i1 < sequenceEnd; i1++) {
                    if (sequence.get(i1).getType() == Constants.TYPE_SUB_PATTERNS) {
                        sequence.get(i1 - candidateListLength).addAll(sequence.get(i1));
                    }
                }

                cropEnd(i);
                return;
            }
        }
    }

    /**
     * Merges all elements of the other sequence with the elements of this sequence.
     * Both sequences must be the same length.
     * @param otherSequence sequence whose elements are to merged into the current sequence.
     */
    public void mergeSequence(Sequence otherSequence) {
        for (int i = 0; i < otherSequence.sequence.size(); i++) {
            SequenceElement thisElement = sequence.get(i);
            SequenceElement otherElement = otherSequence.sequence.get(i);
            if (thisElement.getType() != otherElement.getType())
                System.out.println("Error: Trying to merge non-equivalent sequences");
            
            if (thisElement.getType() == Constants.TYPE_SUB_PATTERNS) {
                thisElement.addAll(otherElement);
            }
        }
    }

    /**
     * Similar to the case of `same`, only the elemnts of TYPE_FUNCTION determine the 
     * hash of sequence. The location of the elements of TYPE_SUB_PATTERN affect the 
     * hash, but not the contents of the subPatterns, hence why we fill in those positions
     * with a constant value when updating the hashing function.
     */
    public int hash() {
        Hasher hasher = Hashing.murmur3_32(Constants.MURMUR_SEED).newHasher();
        for (int i = 0; i < sequence.size(); i++) {
            SequenceElement e = sequence.get(i);

            int updateVal = e.getType() == Constants.TYPE_FUNCTION ? e.getFunction() : Constants.PATTERN_BASE;
            hasher.putInt(updateVal);
        }

        return hasher.hash().asInt();
    }

    /**
     * Creates a clone of this sequence with all the sequenceElements of type 
     * Constants.TYPE_SUB_PATTERN being empty. This clone will be equivalent 
     * to the current sequence.
     */
    public Sequence createEmptyClone() {
        Sequence emptyClone = new Sequence();
        for (SequenceElement element : sequence) {
            if (element.getType() == Constants.TYPE_FUNCTION) {
                emptyClone.add(element);
            } else {
                emptyClone.add(new SequenceElement());
            }
        }

        return emptyClone;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[");
        String[] sequenceElementStrings = new String[sequence.size()];
        for (int i = 0; i < sequence.size(); i++)
            sequenceElementStrings[i] = sequence.get(i).toString();

        s.append(String.join(", ", sequenceElementStrings));
        s.append("]");

        return s.toString();
    }

    /**
     * We determine sequenceEquivalent based on the functions alone. As long as all 
     * subPattern elements occur at the positions, we only require that the functions 
     * at all other position be same.
     */
    private boolean same(int start1, int end1, int start2, int end2) {
        if ((end1 - start1) != (end2 - start2)) return false;
        for (int i = 0; i < end1 - start1; i++) {

            if (sequence.get(start1 + i).getType() == sequence.get(start2 + i).getType() &&
                (sequence.get(start2 + i).getType() == Constants.TYPE_SUB_PATTERNS ||
                sequence.get(start1 + i).getFunction() == sequence.get(start2 + i).getFunction())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Removes all elements that come after sequenceEnd
     */
    private void cropEnd(int sequenceEnd) {
        for (int i = sequence.size() - 1; i >= sequenceEnd; i--)
            sequence.remove(i);
    }
}
