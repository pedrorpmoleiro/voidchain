package pt.ipleiria.estg.dei.pi.voidchain.util;

public class Pair<A extends Object, B extends Object> {
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
