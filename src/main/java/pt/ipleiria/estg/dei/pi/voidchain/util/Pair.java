package pt.ipleiria.estg.dei.pi.voidchain.util;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {
    private final A o1;
    private final B o2;

    public Pair(A o1, B o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public A getO1() {
        return o1;
    }

    public B getO2() {
        return o2;
    }
}
