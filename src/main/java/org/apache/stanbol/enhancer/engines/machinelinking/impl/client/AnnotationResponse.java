package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

import java.util.Arrays;

/**
 * Models an annotation from the <i>MachineLinking</i> <b>/annotate</b> service.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class AnnotationResponse implements Response {

    private final String lang;

    private final Keyword[] keywords;

    private final int cost;

    public AnnotationResponse(String lang, Keyword[] keywords, int cost) {
        this.lang = lang;
        this.keywords = keywords;
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

    public Keyword[] getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return String.format("lang: [%s], keywords: %s", lang, Arrays.toString(keywords));
    }

}
