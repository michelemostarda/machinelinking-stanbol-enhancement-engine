package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * Defines any {@link APIClient} response.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public interface Response {

    String getLang();

    int getCost();

}
