package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import java.io.Serializable;

/**
 * The enum Transaction status.
 */
public enum TransactionStatus implements Serializable {
    /**
     * In block transaction status.
     */
    IN_BLOCK,
    /**
     * In mem pool transaction status.
     */
    IN_MEM_POOL,
    /**
     * Unknown transaction status.
     */
    UNKNOWN
}
