package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * Defines any error raised by {@link APIClient}.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class APIClientException extends Exception {

    public APIClientException(String msg, Exception e) {
        super(msg, e);
    }

    public APIClientException(String msg) {
        super(msg);
    }

}
