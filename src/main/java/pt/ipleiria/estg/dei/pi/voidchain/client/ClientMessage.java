package pt.ipleiria.estg.dei.pi.voidchain.client;

import java.io.Serializable;

public class ClientMessage implements Serializable {
    private final ClientMessageType type;
    private final byte[] content;

    /**
     * Instantiates a new Client message.
     *
     * @param type the type
     */
    public ClientMessage(ClientMessageType type) {
        this.type = type;
        this.content = null;
    }

    /**
     * Instantiates a new Client message.
     *
     * @param type    the type
     * @param content the data
     */
    public ClientMessage(ClientMessageType type, byte[] content) {
        this.type = type;
        this.content = content;
    }

    /* Getter */

    /**
     * Returns the type of the message.
     *
     * @return the type of the message
     */
    public ClientMessageType getType() {
        return type;
    }

    /**
     * Has content.
     *
     * @return true if the message has data or false otherwise.
     */
    public boolean hasContent() {
        return this.content != null && this.content.length > 0;
    }

    /**
     * Returns the content of the message.
     *
     * @return the data
     */
    public byte[] getContent() {
        return content;
    }
}
