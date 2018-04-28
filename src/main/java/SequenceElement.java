import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SequenceElement {

    /**
     * If `type == Constants.TYPE_SUB_PATTERNS`, then `subPatterns` is not 
     * null, and `function` is undefined. If `type == Constants.TYPE_FUNCTION`, 
     * then `subPatterns` is null, and `function` is a functionID.
     */
    private int type;

    /**
     * If this has `type == Constants.TYPE_SUB_PATTERNS`, then subPatterns is a map
     * that maps patternIDs to the number of times they occur for the given element.
     */
    private Map<Integer, Integer> subPatterns;
    private int function;

    public SequenceElement(int function) {
        this.type = Constants.TYPE_FUNCTION;
        this.subPatterns = null;
        this.function = function;
    }

    public SequenceElement() {
        this.type = Constants.TYPE_SUB_PATTERNS;
        this.subPatterns = new HashMap<>();
        this.function = -1;
    }

    public int getType() {
        return type;
    }

    public int getFunction() {
        return function;
    }

    public void add(int patternID) {
        Integer count = subPatterns.get(patternID);
        count = count == null ? 1 : count + 1;
        subPatterns.put(patternID, count);
    }

    /**
     * Merges the subPatterns in otherElement to the subPatterns of this element. Both
     * this and the other element must be of type Constants.TYPE_SUB_PATTERN.
     * @param otherElement SequenceElement who's subpatterns are going to be merge in.
     */
    public void addAll(SequenceElement otherElement) {
        Map<Integer, Integer> otherSubPatterns = otherElement.subPatterns;
        for (Map.Entry<Integer, Integer> entry : otherSubPatterns.entrySet()) {
            Integer count = subPatterns.get(entry.getKey());
            count = count == null ? entry.getValue() : count + entry.getValue();
            subPatterns.put(entry.getKey(), count);
        }
    }

    @Override
    public String toString() {
        if (type == Constants.TYPE_FUNCTION) {
            return String.valueOf(function);
        } else {
            StringBuilder s = new StringBuilder();
            s.append("{");
            ArrayList<String> subPatternStrings = new ArrayList<String>();
            for (Map.Entry<Integer, Integer> entry : subPatterns.entrySet()) {
                String patternID = String.valueOf(entry.getKey());
                String count = String.valueOf(entry.getValue());
                subPatternStrings.add("(" + patternID + " => " + count + ")");
            }

            s.append(String.join(", ", subPatternStrings));
            s.append("}");

            return s.toString();
        }
    }
}
