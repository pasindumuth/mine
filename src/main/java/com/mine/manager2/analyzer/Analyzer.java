package com.mine.manager2.analyzer;

import com.mine.Constants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Analyzer {

    public void analyze() throws IOException {
        for (int i = Constants.START_THREAD; i <= Constants.END_THREAD; i++) {
            BufferedReader reader = new BufferedReader(
                    new FileReader(Constants.PATTERN_DIR + "thread." + i + ".patterns"));
            JSONTokener tokener = new JSONTokener(reader);
            JSONArray patterns = new JSONArray(tokener);

            for (int j = 0; j < 40; j++) {
                JSONObject serializedPattern = patterns.getJSONObject(j);
                ParsedPattern parsedPattern = new ParsedPattern(serializedPattern);
                System.out.println(parsedPattern.prettyString());
            }
        }
    }
}
