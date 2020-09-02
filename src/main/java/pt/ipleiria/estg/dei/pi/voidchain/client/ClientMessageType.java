package pt.ipleiria.estg.dei.pi.voidchain.client;

import java.io.Serializable;

/**
 * The enum Client message type.
 */
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
    GET_LEADER,
    /**
     * Transaction status client message type.
     */
    TRANSACTION_STATUS,
    /**
     * Number nodes client message type.
     */
    NUMBER_NODES
}
