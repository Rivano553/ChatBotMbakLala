package org.example.mbaklala.bot;
import java.util.List;
import java.util.Map;
public class IntentService {
    public static String detectIntent(String input) {
        for (Map.Entry<String, List<String>> entry : ChatbotRepository.getKeywords().entrySet()) {
            for (String keyword : entry.getValue()) {
                if (input.toLowerCase().contains(keyword)) return entry.getKey();
            }
        }
        return "fallback";
    }
}