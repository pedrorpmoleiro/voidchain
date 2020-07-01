package pt.ipleiria.estg.dei.pi.voidchain.replica;

import java.io.*;

public class ReplicaMessage implements Serializable {

    private final ReplicaMessageType type;
    private final byte[] content;
    private final int sender;

    /**
     * Instantiates a new Replica message.
     *
     * @param sender the sender
     * @param type   the type
     */
    public ReplicaMessage(int sender, ReplicaMessageType type) {
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
    public ReplicaMessage(int sender, ReplicaMessageType type, byte[] content) {
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
    public ReplicaMessageType getType() {
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
     * @return the content (byte[])
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
