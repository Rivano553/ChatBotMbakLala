package org.example.mbaklala.bot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ChatbotRepository {
    public static Map<String, List<String>> getKeywords() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("opening", List.of("halo", "hai"));
        map.put("fallback", List.of("Maaf, tidak mengerti."));
        return map;
    }
    public static Map<String, List<String>> getResponses() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("opening", List.of("Halo! Ada yang bisa dibantu?"));
        map.put("fallback", List.of("Maaf, Launderly kurang paham."));
        return map;
    }
}