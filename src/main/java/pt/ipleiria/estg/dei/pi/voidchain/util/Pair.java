package pt.ipleiria.estg.dei.pi.voidchain.util;

import java.io.Serializable;

/**
 * Auxiliary class used to store pairs of values.
 *
 * @param <A> the type parameter
 * @param <B> the type parameter
 */
public class Pair<A, B> implements Serializable {
    private final A o1;
    private final B o2;

    /**
     * Instantiates a new Pair.
     *
     * @param o1 the o 1
     * @param o2 the o 2
     */
    public Pair(A o1, B o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    /**
     * Gets object 1.
     *
     * @return the object 1
     */
    public A getO1() {
        return o1;
    }

    /**
     * Gets objects 2.
     *
     * @return the object 2
     */
    public B getO2() {
        return o2;
    }
}
