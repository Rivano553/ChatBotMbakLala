package org.example.mbaklala.bot;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChatbotEngine {
    private Random random = new Random();
    public String process(String intent) {
        Map<String, List<String>> responses = ChatbotRepository.getResponses();
        List<String> list = responses.getOrDefault(intent, responses.get("fallback"));
        return list.get(random.nextInt(list.size()));
    }
}