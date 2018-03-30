import java.util.ArrayList;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class Sequence {
    
    private ArrayList<SequenceElement> sequence;
    private int sequenceEnd;

    public Sequence() {
        this.sequence = new ArrayList<SequenceElement>();
        this.sequenceEnd = 0;
    }

    public ArrayList<SequenceElement> getSequence() {
        crop();
        return sequence;
    }

    public void add(SequenceElement element) {
        if (sequence.size() == sequenceEnd) sequence.add(element);
        else sequence.set(sequenceEnd, element);
        sequenceEnd++;
    }

    public void compressVeryLossy() {
        SequenceElement lastElement = sequence.get(sequenceEnd - 1);
        for (int i = sequenceEnd - 1; i > 0; i--) {
            if (sequence.get(i - 1).getType() == lastElement.getType() &&
                (lastElement.getType() == Constants.TYPE_SUB_PATTERNS ||
                sequence.get(i - 1).getFunction() == lastElement.getFunction())) {

                int candidateListLength = sequenceEnd - i;
                if (i - candidateListLength < 0) return; 
                
                if (!same(i, sequenceEnd, i - candidateListLength, i)) continue;
                
                for (int i1 = i; i1 < sequenceEnd; i1++) {
                    int i2 = i1 - candidateListLength;

                    if (sequence.get(i1).getType() == Constants.TYPE_SUB_PATTERNS) {
                        sequence.get(i2).getSubPatterns().addAll(
                            sequence.get(i1).getSubPatterns());
                    }
                }

                sequenceEnd = sequenceEnd - candidateListLength;
                return;
            }
        }
    }

    private boolean same(int start1, int end1, int start2, int end2) {
        if ((end1 - start1) != (end2 - start2)) return false;
        for (int i = 0; i < end1 - start1; i++) {

            /**
             * We determine equality based on the functions alone. As long
             * as all subPatterns elements occur at the same offset, we
             * only require that the functions at all other points are
             * the same. Recall that the `function` value for an element
             * is -1 if the type is a SUB_PATTERN.
             */
            
            if (sequence.get(start1 + i).getFunction() 
             != sequence.get(start2 + i).getFunction()) return false;
        }

        return true;
    }

    public int hash() {
        Hasher hasher = Hashing.murmur3_32(Constants.MURMUR_SEED).newHasher();
        for (int i = 0; i < sequenceEnd; i++) {
            SequenceElement e = sequence.get(i);

            /**
             * Similar to the case of `same`, only the elemnts of TYPE_FUNCTION
             * determine the hash of sequence. The location of the elements
             * of TYPE_SUB_PATTERN affect the hash, but not the contents of the
             * subPatterns.
             */

            int updateVal = e.getType() == Constants.TYPE_FUNCTION ? e.getFunction() : -1;
            hasher.putInt(updateVal);
        }

        return hasher.hash().asInt();
    }

    @Override
    public String toString() {
        crop();

        StringBuilder s = new StringBuilder();
        s.append("[");
        String[] sequenceElementStrings = new String[sequence.size()];
        for (int i = 0; i < sequence.size(); i++)
            sequenceElementStrings[i] = sequence.get(i).toString();

        s.append(String.join(", ", sequenceElementStrings));
        s.append("]");

        return s.toString();
    }

    private void crop() {
        for (int i = sequence.size() - 1; i >= sequenceEnd; i--)
            sequence.remove(i);
    }
}
