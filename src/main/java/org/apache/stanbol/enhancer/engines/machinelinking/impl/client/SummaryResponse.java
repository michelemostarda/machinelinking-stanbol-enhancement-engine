package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class SummaryResponse {

    private final int cost;
    private final Summary[] summaries;

    public int getCost() {
        return cost;
    }

    public Summary[] getSummaries() {
        return summaries;
    }

    public SummaryResponse(int cost, Summary[] summaries) {
        this.cost = cost;
        this.summaries = summaries;
    }

}
