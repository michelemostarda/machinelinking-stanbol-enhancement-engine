package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * Defines an alternative form in annotation.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class Alt {

    private final String form;
    private final double freq;

    public Alt(String form, double freq) {
        this.form = form;
        this.freq = freq;
    }

    public String getForm() {
        return form;
    }

    public double getFreq() {
        return freq;
    }

    @Override
    public String toString() {
        return String.format("form: %s, freq: %f", form, freq);
    }

}
