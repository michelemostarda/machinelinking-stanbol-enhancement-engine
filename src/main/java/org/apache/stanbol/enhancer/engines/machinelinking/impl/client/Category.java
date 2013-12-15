package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

import java.net.URL;

/**
 * Defines a <i>Wikipedia</i> category.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class Category {

    private final String label;
    private final URL url;

    public Category(String label, URL url) {
        this.label = label;
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return String.format("label: [%s], url: %s", label, url);
    }

}
