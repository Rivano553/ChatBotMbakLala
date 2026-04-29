package org.example.mbaklala;

import java.util.*;

public class TextPreprocessor {

    public static List<String> process(String text) {

        text = text.toLowerCase();
        text = text.replaceAll("[^a-z0-9 ]", "");

        return Arrays.asList(text.split("\\s+"));
    }
}