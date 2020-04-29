package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.util.encoders.Base64;
import pt.ipleiria.estg.dei.pi.voidchain.Util;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * A block is where the transactions/data are stored in,
 * and then the block is added to the blockchain, if it's valid
 * The structure of a block follows: version, a header (which is its own class),
 * size, transaction counter and its height
 * In the blockchain, what identifies a block is its ID, which is the hash of its header (block header)
 * Keep in mind that the block hash is not stored in the blocks data structure nor in the blockchain,
 * it needs to be calculated if needed.
 */
public class Block implements Serializable {
    /* Attributes */
    // TODO: SIZE TO INCLUDE TRANSACTIONS
    private final Map<String, Transaction> transactions;
    private final BlockHeader blockHeader;
    private final long size;
    private int transactionCounter;
    private final int blockHeight;

    /**
     * Instantiates a new Block without any initial transactions.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     */
    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight) {
        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion);
        this.blockHeight = blockHeight;
        this.transactionCounter = 0;
        this.transactions = new Hashtable<>();
        this.size = this.blockHeader.getSize() + (Integer.SIZE * 2);
    }

    /**
     * Instantiates a new Block without any initial transactions and with predefined timestamp and nonce.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     * @param timestamp         the timestamp
     * @param nonce             the nonce
     */
    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight, long timestamp, int nonce) {
        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion, timestamp, nonce);
        this.blockHeight = blockHeight;
        this.transactionCounter = 0;
        this.transactions = new Hashtable<>();
        this.size = this.blockHeader.getSize() + (Integer.SIZE * 2);
    }

    /**
     * Instantiates a new Block with a preset "list" of transactions and with predefined timestamp and nonce.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     * @param transactions      the transactions
     */
    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight, Map<String, Transaction> transactions) {
        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion);
        this.blockHeight = blockHeight;

        this.transactionCounter = transactions.size();
        this.transactions = new Hashtable<>(transactions);

        this.size = this.blockHeader.getSize() + (Integer.SIZE * 2) + this.transactions.size();
    }

    /**
     * Instantiates a new Block with a preset "list" of transactions.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     * @param transactions      the transactions
     * @param timestamp         the timestamp
     * @param nonce             the nonce
     */
    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight, Map<String, Transaction> transactions, long timestamp, int nonce) {
        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion, timestamp, nonce);
        this.blockHeight = blockHeight;

        this.transactionCounter = transactions.size();
        this.transactions = new Hashtable<>(transactions);

        this.size = this.blockHeader.getSize() + (Integer.SIZE * 2) + this.transactions.size();
    }

    /* Methods */

    /**
     * Adds a transaction to the block
     *
     * @param transaction the transaction
     */
    public void addTransaction(Transaction transaction) {
        this.transactions.put(Base64.toBase64String(transaction.getHash()), transaction);
        this.transactionCounter++;
    }

    /**
     * Adds transactions to the block.
     *
     * @param transactions the transactions
     */
    public void addTransactions(Map<String, Transaction> transactions) {
        this.transactions.putAll(transactions);
        this.transactionCounter += transactions.size();
    }

    /* Getters */
    /**
     * Gets all the transactions that are stored in a block
     * For security reasons, we do not give direct access to the transactions,
     * we return a copy of it.
     *
     * @return the transactions (Map<String, Transaction>)
     */
    public Map<String, Transaction> getTransactions() {
        return new Hashtable<>(this.transactions);
    }

    /**
     * Gets size of the block in bytes.
     * Does not include size of transactions.
     *
     * @return the size (long)
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the number of transactions inside a block.
     *
     * @return the transaction counter (long)
     */
    public int getTransactionCounter() {
        return transactionCounter;
    }

    /**
     * Gets block height.
     *
     * @return the block height (int)
     */
    public int getBlockHeight() {
        return blockHeight;
    }

    /**
     * Gets the Epoch time the block was created, from the block header
     *
     * @return the timestamp (long)
     */
    public long getTimestamp() {
        return this.blockHeader.getTimestamp();
    }

    /**
     * Gets the hash of the previous block in the chain, from the block header.
     *
     * @return the previous block hash (byte[])
     */
    public byte[] getPreviousBlockHash() {
        return this.blockHeader.getPreviousBlockHash();
    }

    /**
     * Gets protocol version, from the block header.
     *
     * @return the protocol version (float)
     */
    public float getProtocolVersion() {
        return this.blockHeader.getProtocolVersion();
    }

    /**
     * Gets nonce, a random int, from the block header.
     *
     * @return the nonce (int)
     */
    public int getNonce() {
        return this.blockHeader.getNonce();
    }

    /**
     * Calculates the hash of the block.
     * To calculate the hash of a block, we double hash it's header (block header)
     * SHA3_512(RIPEMD160(blockHeader))
     *
     * @return the block hash / block header hash (byte[])
     */
    public byte[] getHash() {
        return Util.calculateHash(this.blockHeader.getData());
    }

    @Override
    public String toString() {
        return "Block: {" + System.lineSeparator() +
                "transactions: " + transactions.values() + System.lineSeparator() +
                blockHeader + System.lineSeparator() +
                "size: " + size + System.lineSeparator() +
                "transaction counter: " + transactionCounter + System.lineSeparator() +
                "block height: " + blockHeight + System.lineSeparator() +
                "hash: " + Base64.toBase64String(getHash()) + System.lineSeparator() +
                "}";
    }
}
