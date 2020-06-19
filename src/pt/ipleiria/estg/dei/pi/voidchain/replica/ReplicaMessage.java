package pt.ipleiria.estg.dei.pi.voidchain.replica;

import java.io.*;

// TODO: JAVADOC
public class ReplicaMessage implements Serializable {

    private ReplicaMessageType type;
    private byte[] content = new byte[0];
    private int sender;

    public ReplicaMessage(int sender, ReplicaMessageType type, byte[] content) {
        this.sender = sender;
        this.type = type;

        if (type == ReplicaMessageType.NEW_BLOCK) {
            this.content = content;
        }
    }

    /* Getter */
    public ReplicaMessageType getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }

    public int getSender() {
        return sender;
    }
}
