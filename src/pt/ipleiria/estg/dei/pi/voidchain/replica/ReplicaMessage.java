package pt.ipleiria.estg.dei.pi.voidchain.replica;

import java.io.*;

// TODO: JAVADOC
public class ReplicaMessage implements Serializable {

    private final ReplicaMessageType type;
    private final byte[] content;
    private final int sender;

    public ReplicaMessage(int sender, ReplicaMessageType type) {
        this.sender = sender;
        this.type = type;
        this.content = null;
    }

    public ReplicaMessage(int sender, ReplicaMessageType type, byte[] content) {
        this.sender = sender;
        this.type = type;

        if (type == ReplicaMessageType.NEW_BLOCK)
            this.content = content;
        else
            this.content = null;
    }

    /* Getter */
    public ReplicaMessageType getType() {
        return type;
    }

    public boolean hasContent() {
        return this.content != null && this.content.length > 0;
    }

    public byte[] getContent() {
        return content;
    }

    public int getSender() {
        return sender;
    }
}
