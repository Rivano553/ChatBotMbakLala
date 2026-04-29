package org.example.mbaklala;

import java.util.*;

public class ChatbotEngine {

    private Random random = new Random();

    public String process(String intent, Map<String, List<String>> responses) {

        List<String> list = responses.get(intent);

        if (list == null || list.isEmpty()) {
            list = responses.get("fallback");
        }

        return list.get(random.nextInt(list.size()));
    }
}