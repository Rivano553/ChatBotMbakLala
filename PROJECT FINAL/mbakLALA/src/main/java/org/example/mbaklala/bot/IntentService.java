package org.example.mbaklala.bot;

import java.util.List;
import java.util.Map;

public class IntentService {
    public static String detectIntent(String input) {
        String lowerInput = input.toLowerCase();
        for (Map.Entry<String, List<String>> entry : ChatbotRepository.getKeywords().entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerInput.contains(keyword)) return entry.getKey();
            }
        }
        return "fallback";
    }
}