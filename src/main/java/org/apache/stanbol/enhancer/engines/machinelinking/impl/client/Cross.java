package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * Defines a cross language.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class Cross {

    private final String lang;
    private final String page;

    public Cross(String lang, String page) {
        this.lang = lang;
        this.page = page;
    }

    public String getLang() {
        return lang;
    }

    public String getPage() {
        return page;
    }

    @Override
    public String toString() {
        return String.format("lang: %s, page: %s", lang, page);
    }

}