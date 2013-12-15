package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

/**
 * Defines a text N-gram.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class NGram {

    private int start;
    private int end;

    public NGram(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

}
