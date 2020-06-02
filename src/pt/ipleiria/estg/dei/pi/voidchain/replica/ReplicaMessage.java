package pt.ipleiria.estg.dei.pi.voidchain.replica;

import java.io.*;

public class ReplicaMessage implements Serializable {

    private ReplicaMessageType type;
    private byte[] content = new byte[0];

    public ReplicaMessage(ReplicaMessageType type, byte[] content) {
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
}
