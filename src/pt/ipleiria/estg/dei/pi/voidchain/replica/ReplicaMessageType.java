package pt.ipleiria.estg.dei.pi.voidchain.replica;

import java.io.Serializable;

public enum ReplicaMessageType implements Serializable {
    NEW_BLOCK,
    SYNC_BLOCKS; // ?

    public int toInt() {
        switch (this) {
            case NEW_BLOCK: return 0;
            case SYNC_BLOCKS: return 1;
            default: return -1;
        }
    }

    public static ReplicaMessageType fromInt(int i) {
        switch (i) {
            case 0: return NEW_BLOCK;
            case 1: return SYNC_BLOCKS;
            default: return null; // ?
        }
    }
}
