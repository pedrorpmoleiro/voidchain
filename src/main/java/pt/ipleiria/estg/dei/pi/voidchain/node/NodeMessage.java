package pt.ipleiria.estg.dei.pi.voidchain.node;

import java.io.*;

public class NodeMessage implements Serializable {

    private final NodeMessageType type;
    private final byte[] content;
    private final int sender;

    /**
     * Instantiates a new Replica message.
     *
     * @param sender the sender
     * @param type   the type
     */
    public NodeMessage(int sender, NodeMessageType type) {
        this.sender = sender;
        this.type = type;
        this.content = null;
    }

    /**
     * Instantiates a new Replica message.
     *
     * @param sender  the sender
     * @param type    the type
     * @param content the content
     */
    public NodeMessage(int sender, NodeMessageType type, byte[] content) {
        this.sender = sender;
        this.type = type;
        this.content = content;
    }

    /* Getter */

    /**
     * Returns the type of the message.
     *
     * @return the type of the message
     */
    public NodeMessageType getType() {
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
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Returns the id of the sender.
     *
     * @return the sender
     */
    public int getSender() {
        return sender;
    }
}
