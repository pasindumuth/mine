public class Constants {

    public static final String DATA_DIR = "data/";
    public static final String TEST_DATA_DIR = "src/test/data/";
    public static final String TRACE_PATH = DATA_DIR + "trace.txt";
    public static final String FUNCTIONS_PATH = DATA_DIR + "functions.txt";
    public static final String THREAD_DIR = DATA_DIR + "threads/";
    public static final String PATTERN_DIR = DATA_DIR + "patterns/";

    // SequenceElement types
    public static final int TYPE_FUNCTION = 0;
    public static final int TYPE_SUB_PATTERNS = 1;

    public static final int MURMUR_SEED = 16109143;

    public static final int PATTERN_BASE = 10000;
    public static final int NULL_FUNCTION_ID = -1;
    public static final double NULL_FUNCTION_DISTANCE = 0.5;
    public static final int BASE_FUNCTION_ID = 1000;
    
    public static final int PATTERN_SIMILARITY_THRESHOLD = 1;

    // Data
    public static final int FUNCTION_ENTER = 0;
    public static final int FUNCTION_EXIT = 1;

    // Run Mode
    public static final int NORMAL = 0; 
    public static final int DEBUG = 1;

    public static int RUN_MODE = NORMAL;
}
