package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

/**
 * The enum Transaction status.
 */
public enum TransactionStatus {
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
