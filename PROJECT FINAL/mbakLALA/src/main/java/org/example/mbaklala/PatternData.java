package org.example.mbaklala;

public class PatternData {

    private String pattern;
    private String intent;
    private int weight;

    public PatternData(String pattern, String intent) {
        this.pattern = pattern;
        this.intent = intent;
        this.weight = 1;
    }

    public String getPattern() {
        return pattern;
    }

    public String getIntent() {
        return intent;
    }

    public int getWeight() {
        return weight;
    }
}