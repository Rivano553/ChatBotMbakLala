package org.example.mbaklala;

import java.util.regex.*;

public class EntityExtractor {

    public static String extractLayanan(String input) {

        input = input.toLowerCase();

        if (input.contains("boneka")) return "boneka";
        if (input.contains("pakaian")) return "pakaian";
        if (input.contains("jaket")) return "jaket";

        return null;
    }

    public static double extractBerat(String input) {

        Pattern p = Pattern.compile("(\\d+(\\.\\d+)?)\\s*kg");
        Matcher m = p.matcher(input.toLowerCase());

        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }

        return 0;
    }
}