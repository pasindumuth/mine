import java.util.ArrayList;

public class SequenceElement {

    /**
     * If `type` == Constants.TYPE_SUB_PATTERNS, then subPatterns is a 
     * list of patternIDs, and`function` is -1. If `type` == Constants.TYPE_FUNCTION, 
     * then subPatterns is null, and `function` is a functionID.
     */

    private int type;
    private ArrayList<Integer> subPatterns;
    private int function;

    public SequenceElement(int function) {
        this.type = Constants.TYPE_FUNCTION;
        this.subPatterns = null;
        this.function = function;
    }

    public SequenceElement(ArrayList<Integer> subPatterns) {
        this.type = Constants.TYPE_SUB_PATTERNS;
        this.subPatterns = subPatterns;
        this.function = -1;
    }

    public int getType() {
        return type;
    }

    public void add(int patternID) {
        subPatterns.add(patternID);
    }

    public ArrayList<Integer> getSubPatterns() {
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
            String[] subPatternStrings = new String[subPatterns.size()];
            for (int i = 0; i < subPatterns.size(); i++)
                subPatternStrings[i] = String.valueOf(subPatterns.get(i));
                
            s.append(String.join(", ", subPatternStrings));
            s.append("}");

            return s.toString();
        }
    }
}
