package pt.ipleiria.estg.dei.pi.voidchain.replica;

import java.io.Serializable;

public enum ReplicaMessageType implements Serializable {
    /**
     * Propose new block replica message type.
     */
    NEW_BLOCK;

    /**
     * Converts this enum into an int value.
     *
     * @return the int
     */
    public int toInt() {
        switch (this) {
            case NEW_BLOCK:
                return 0;
            default:
                return -1;
        }
    }

    /**
     * Converts an int into a value of this Enum.
     *
     * @param i the integer
     * @return the replica message type
     */
    public static ReplicaMessageType fromInt(int i) {
        switch (i) {
            case 0:
                return NEW_BLOCK;
            default:
                return null; // ?
        }
    }
}
