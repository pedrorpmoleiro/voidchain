package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.util.encoders.Base64;

import pt.ipleiria.estg.dei.pi.voidchain.util.*;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
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
    // TODO: SIZE TO INCLUDE TRANSACTIONS & CHANGE HASHTABLE
    private final Map<byte[], Transaction> transactions;
    private final BlockHeader blockHeader;
    private final long size;
    private int transactionCounter;
    private final int blockHeight;

    /**
     * Instantiates a new Block without any initial transactions and with predefined timestamp and nonce.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     * @param timestamp         the timestamp
     * @param nonce             the nonce
     */
    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight, long timestamp, byte[] nonce) {
        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion, timestamp, nonce);
        this.blockHeight = blockHeight;
        this.transactionCounter = 0;
        this.transactions = new Hashtable<>();
        this.size = this.blockHeader.getSize() + (Integer.BYTES * 2);
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
    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight,
                 Map<byte[], Transaction> transactions, long timestamp, byte[] nonce) {
        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion, timestamp, nonce);
        this.blockHeight = blockHeight;
        this.transactionCounter = transactions.size();
        this.transactions = new Hashtable<>(transactions);
        this.size = this.blockHeader.getSize() + (Integer.BYTES * 2) + this.transactions.size();
        this.blockHeader.merkleRoot = MerkleTree.getMerkleRoot(this.transactions.keySet());
    }

    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight,
                 List<Transaction> transactions, long timestamp, byte[] nonce) {
        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion, timestamp, nonce);
        this.blockHeight = blockHeight;
        this.transactionCounter = transactions.size();
        this.transactions = new Hashtable<>(Converters.transactionListToMap(transactions));
        this.size = this.blockHeader.getSize() + (Integer.BYTES * 2) + this.transactions.size();
        this.blockHeader.merkleRoot = MerkleTree.getMerkleRoot(this.transactions.keySet());
    }

    // FOR USE WITH CLONE
    private Block (int blockHeight, Map<byte[], Transaction> transactions, long size, BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
        this.blockHeight = blockHeight;
        this.transactionCounter = transactions.size();
        this.transactions = new Hashtable<>(transactions);
        this.size = size;
    }

    /* Methods */
    /**
     * Adds a transaction to the block
     *
     * @param transaction the transaction
     */
    public boolean addTransaction(Transaction transaction) {
        if (transaction.getSize() > Transaction.MAX_SIZE) {
            return false;
        }

        this.transactions.put(transaction.getHash(), transaction);
        this.transactionCounter++;
        this.blockHeader.merkleRoot = MerkleTree.getMerkleRoot(this.transactions.keySet());

        return true;
    }

    /**
     * Adds transactions to the block.
     *
     * @param transactions the transactions
     */
    // TODO: MAX TRANSACTION SIZE
    public boolean addTransactions(Map<byte[], Transaction> transactions) {
        this.transactions.putAll(transactions);
        this.transactionCounter += transactions.size();
        this.blockHeader.merkleRoot = MerkleTree.getMerkleRoot(this.transactions.keySet());

        return true;
    }

    // TODO: MAX TRANSACTION SIZE
    public boolean addTransactions(List<Transaction> transactions) {
        this.transactions.putAll(Converters.transactionListToMap(transactions));
        this.transactionCounter += transactions.size();
        this.blockHeader.merkleRoot = MerkleTree.getMerkleRoot(this.transactions.keySet());

        return true;
    }

    /* Getters */
    /**
     * Gets all the transactions that are stored in a block
     * For security reasons, we do not give direct access to the transactions,
     * we return a copy of it.
     *
     * @return the transactions (Map<String, Transaction>)
     */
    public Map<byte[], Transaction> getTransactions() {
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
        return this.blockHeader.timestamp;
    }

    /**
     * Gets the hash of the previous block in the chain, from the block header.
     *
     * @return the previous block hash (byte[])
     */
    public byte[] getPreviousBlockHash() {
        return this.blockHeader.previousBlockHash;
    }

    /**
     * Gets protocol version, from the block header.
     *
     * @return the protocol version (float)
     */
    public float getProtocolVersion() {
        return this.blockHeader.protocolVersion;
    }

    /**
     * Gets nonce, a random int, from the block header.
     *
     * @return the nonce (int)
     */
    public byte[] getNonce() {
        return this.blockHeader.nonce;
    }

    /**
     * Calculates the hash of the block.
     * To calculate the hash of a block, we double hash it's header (block header)
     * SHA3_512(RIPEMD160(blockHeader))
     *
     * @return the block hash / block header hash (byte[])
     */
    public byte[] getHash() {
        return Hash.calculateSHA3512RIPEMD160(this.blockHeader.getData());
    }

    public Block clone() {
        return new Block(this.blockHeight, this.transactions, this.size, this.blockHeader.clone());
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
