package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * Response for <i>lang</i> method.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class GuessedLanguageResponse implements Response {

    private final String lang;
    private int cost;

    public GuessedLanguageResponse(String lang, int cost) {
        this.lang = lang;
        this.cost = cost;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public int getCost() {
        return cost;
    }

}
