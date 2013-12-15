package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

import java.net.URL;

/**
 * Defines a classification based on a <i>Wikipedia</i> class.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class Clazz {

    private final String label;
    private final URL url;
    private final String resouce;
    private final double prob;

    public Clazz(String label, URL url, String resource, double prob) {
        this.label = label;
        this.url = url;
        this.resouce = resource;
        this.prob = prob;
    }

    public String getLabel() {
        return label;
    }

    public URL getUrl() {
        return url;
    }

    public String getResource() {
        return resouce;
    }

    public double getProb() {
        return prob;
    }

    @Override
    public String toString() {
        return String.format("label: [%s], url: %s, resource: %s, prob: %f", label, url, resouce, prob);
    }

}
