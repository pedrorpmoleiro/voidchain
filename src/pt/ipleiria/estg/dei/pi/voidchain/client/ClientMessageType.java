package pt.ipleiria.estg.dei.pi.voidchain.client;

import java.io.Serializable;

public enum ClientMessageType implements Serializable {
    /**
     * Get most recent block client message type.
     */
    GET_MOST_RECENT_BLOCK,
    /**
     * Get block client message type.
     */
    GET_BLOCK,
    /**
     * Get most recent block height client message type.
     */
    GET_MOST_RECENT_BLOCK_HEIGHT,
    /**
     * Add transaction client message type.
     */
    ADD_TRANSACTION,
    /**
     * Add transactions client message type.
     */
    ADD_TRANSACTIONS,
    /**
     * Is chain valid client message type.
     */
    IS_CHAIN_VALID,
    /**
     * Get leader client message type.
     */
    GET_LEADER;

    /**
     * Converts this enum into an int value.
     *
     * @return the int
     */
    public int toInt() {
        switch (this) {
            case GET_MOST_RECENT_BLOCK: return 0;
            case GET_BLOCK: return 1;
            case GET_MOST_RECENT_BLOCK_HEIGHT: return 2;
            case ADD_TRANSACTION: return 3;
            case ADD_TRANSACTIONS: return 4;
            case IS_CHAIN_VALID: return 5;
            case GET_LEADER: return 6;
            default: return -1;
        }
    }

    /**
     * Converts an int into a value of this Enum.
     *
     * @param i the integer
     * @return the client message type
     */
    public static ClientMessageType fromInt(int i) {
        switch (i) {
            case 0: return GET_MOST_RECENT_BLOCK;
            case 1: return GET_BLOCK;
            case 2: return GET_MOST_RECENT_BLOCK_HEIGHT;
            case 3: return ADD_TRANSACTION;
            case 4: return ADD_TRANSACTIONS;
            case 5: return IS_CHAIN_VALID;
            case 6: return GET_LEADER;
            default: return null;
        }
    }
}
