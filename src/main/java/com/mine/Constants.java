package com.mine;

public class Constants {
    public enum RunMode {
        ANALYZE,
        MINE,
        SPACE_FIX,
    }

    // Configurations
    public static final int START_THREAD = 1;
    public static final int END_THREAD = 1;

    // Directories
    public static final String DATA_DIR = "data/";
    public static final String TEST_DATA_DIR = "src/test/data/";
    public static final String THREAD_DIR = DATA_DIR + "threads/";
    public static final String PATTERN_DIR = DATA_DIR + "patterns/";

    // Shared
    public static final int PATTERN_BASE = 10000; // Used when serializing patterns
    public static final int NULL_PATTERN_ID = 0;
    public static final int NULL_FUNCTION_ID = -1;
    public static final double NULL_FUNCTION_DISTANCE = 0.5;
    public static final double FUNCTION_DISTANCE = 1.0;

    // Compressed sequence subtrace representation
    public static final int BASE_FUNCTION_ID = 1000;
    public static final int PATTERN_SIMILARITY_THRESHOLD = 1;

    // Occurance set subtrace representation

    // Data
    public static final int FUNCTION_ENTER = 0;

    // Run Mode
    public static final int NORMAL = 0; 
    public static final int DEBUG = 1;

    public static int RUN_MODE = NORMAL;
}
