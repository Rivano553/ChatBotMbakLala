package org.example.mbaklala;

import java.util.*;

public class IntentService {

    public static String detectIntent(String input, List<PatternData> patterns) {

        List<String> tokens = TextPreprocessor.process(input);
        Map<String, Integer> scoreMap = new HashMap<>();

        for (PatternData p : patterns) {
            for (String token : tokens) {

                if (token.equals(p.getPattern())) {
                    scoreMap.put(
                            p.getIntent(),
                            scoreMap.getOrDefault(p.getIntent(), 0) + p.getWeight()
                    );
                }
            }
        }

        String bestIntent = "unknown";
        int max = 0;

        for (var e : scoreMap.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                bestIntent = e.getKey();
            }
        }

        return bestIntent;
    }
}