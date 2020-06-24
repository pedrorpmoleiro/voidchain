package pt.ipleiria.estg.dei.pi.voidchain.client;

import java.io.Serializable;

public enum ClientMessageType implements Serializable {
    GET_MOST_RECENT_BLOCK,
    GET_BLOCK,
    GET_MOST_RECENT_BLOCK_HEIGHT,
    ADD_TRANSACTION,
    IS_CHAIN_VALID;

    public int toInt() {
        switch (this) {
            case GET_MOST_RECENT_BLOCK: return 0;
            case GET_BLOCK: return 1;
            case GET_MOST_RECENT_BLOCK_HEIGHT: return 2;
            case ADD_TRANSACTION: return 3;
            case IS_CHAIN_VALID: return 4;
            default: return -1;
        }
    }

    public static ClientMessageType fromInt(int i) {
        switch (i) {
            case 0: return GET_MOST_RECENT_BLOCK;
            case 1: return GET_BLOCK;
            case 2: return GET_MOST_RECENT_BLOCK_HEIGHT;
            case 3: return ADD_TRANSACTION;
            case 4: return IS_CHAIN_VALID;
            default: return null; // ?
        }
    }
}
