package pt.ipleiria.estg.dei.pi.voidchain.client;

import java.io.Serializable;

/*
    TODO: REQUEST TYPE ENUM ?
*/
public class ClientMessage implements Serializable {
    private final int type;
    private final byte[] data;

    public ClientMessage(int type) {
        this.type = type;
        this.data = null;
    }

    public ClientMessage(int type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public boolean hasData() {
        return this.data != null;
    }

    public byte[] getData() {
        return data;
    }
}
