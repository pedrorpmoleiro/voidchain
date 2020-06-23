package pt.ipleiria.estg.dei.pi.voidchain.client;

import java.io.Serializable;

/**
 * The Client message.
 */
public class ClientMessage implements Serializable {
    private final ClientMessageType type;
    private final byte[] data;

    /**
     * Instantiates a new Client message.
     *
     * @param type the type
     */
    public ClientMessage(ClientMessageType type) {
        this.type = type;
        this.data = null;
    }

    /**
     * Instantiates a new Client message.
     *
     * @param type the type
     * @param data the data
     */
    public ClientMessage(ClientMessageType type, byte[] data) {
        this.type = type;

        if (type == ClientMessageType.ADD_TRANSACTION || type == ClientMessageType.GET_BLOCK ||
        type == ClientMessageType.GET_BLOCK_NO_TRANSACTIONS)
            this.data = data;
        else
            this.data = null;
    }

    /**
     * Gets the type of the message.
     *
     * @return the type of the message
     */
    public ClientMessageType getType() {
        return type;
    }

    /**
     * Has data.
     *
     * @return true if the message has data or false otherwise.
     */
    public boolean hasData() {
        return this.data != null && this.data.length > 0;
    }

    /**
     * Gets the data of the message.
     *
     * @return the data (byte[])
     */
    public byte[] getData() {
        return data;
    }
}
