package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class Summary {

    private final String sentence;
    private final float weight;
    private final int start;
    private final int end;

    public Summary(String sentence, float weight, int start, int end) {
        this.sentence = sentence;
        this.weight = weight;
        this.start = start;
        this.end = end;
    }

    public String getSentence() {
        return sentence;
    }

    public float getWeight() {
        return weight;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

}
