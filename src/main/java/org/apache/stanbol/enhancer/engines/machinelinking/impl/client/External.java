package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

import java.net.URL;

/**
 * Defines an external resource.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class External {

    private final String label;
    private final URL url;
    private final String resource;

    public External(String label, URL url, String resource) {
        this.label = label;
        this.url = url;
        this.resource = resource;
    }

    public String getLabel() {
        return label;
    }

    public URL getUrl() {
        return url;
    }

    public String getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return String.format("label: [%s], url: %s, resource: %s", label, url, resource);
    }
}
