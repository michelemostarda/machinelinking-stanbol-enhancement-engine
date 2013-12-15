package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * Defines the response for the <i>compare</i> method.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class CompareResponse implements Response {

    private final int cost;
    private final float similarity;

    public CompareResponse(int cost, float similarity) {
        this.cost = cost;
        this.similarity = similarity;
    }

    public float getSimilarity() {
        return similarity;
    }

    @Override
    public String getLang() {
        return null;
    }

    @Override
    public int getCost() {
        return cost;
    }

}
