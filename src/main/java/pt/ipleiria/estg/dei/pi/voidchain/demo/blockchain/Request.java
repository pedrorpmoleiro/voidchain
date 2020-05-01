package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import java.io.Serializable;

public class Request implements Serializable {
    private final int req;
    private final boolean hasData;
    private final byte[] data;

    public Request(int req) {
        this.req = req;
        this.hasData = false;
        this.data = null;
    }

    public Request(int req, byte[] data) {
        this.req = req;
        this.hasData = true;
        this.data = data;
    }

    public int getReq() {
        return req;
    }

    public boolean hasData() {
        return hasData;
    }

    public byte[] getData() {
        return data;
    }
}