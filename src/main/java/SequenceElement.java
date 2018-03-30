import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SequenceElement {

    /**
     * If `type` == Constants.TYPE_SUB_PATTERNS, then subPatterns is a 
     * list of patternIDs, and`function` is -1. If `type` == Constants.TYPE_FUNCTION, 
     * then subPatterns is null, and `function` is a functionID.
     */

    private int type;
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

    public void add(int patternID) {
        Integer count = subPatterns.get(patternID);
        count = count == null ? 1 : count + 1;
        subPatterns.put(patternID, count);
    }

    public void addAll(Map<Integer, Integer> otherSubPatterns) {
        for (Map.Entry<Integer, Integer> entry : otherSubPatterns.entrySet()) {
            Integer count = subPatterns.get(entry.getKey());
            count = count == null ? entry.getValue() : count + entry.getValue();
            subPatterns.put(entry.getKey(), count);
        }
    }

    public Map<Integer, Integer> getSubPatterns() {
        return subPatterns;
    }

    public int getFunction() {
        return function;
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
