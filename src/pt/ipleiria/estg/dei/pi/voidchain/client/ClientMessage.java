package pt.ipleiria.estg.dei.pi.voidchain.client;

import java.io.Serializable;

/*
    TODO: REQUEST TYPE ENUM ?
*/
public class ClientMessage implements Serializable {
    private final int req;
    private final byte[] data;

    public ClientMessage(int req) {
        this.req = req;
        this.data = null;
    }

    public ClientMessage(int req, byte[] data) {
        this.req = req;
        this.data = data;
    }

    public int getReq() {
        return req;
    }

    public boolean hasData() {
        return this.data != null;
    }

    public byte[] getData() {
        return data;
    }
}
