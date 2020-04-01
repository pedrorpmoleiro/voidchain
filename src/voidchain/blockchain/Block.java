package voidchain.blockchain;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Base64;

import java.util.Hashtable;
import java.util.Map;

/**
 * The type Block.
 */
public class Block {
    private Map<String, Transaction> transactions;
    private BlockHeader blockHeader;
    private long size;
    private int transactionCounter;
    private int blockHeight;

    /**
     * Instantiates a new Block.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     */
    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight) {
        this.blockHeader = new BlockHeader(previousBlockHash,protocolVersion);
        this.blockHeight = blockHeight;
        this.transactionCounter = 0;
        this.transactions = new Hashtable<>();
        this.size = this.blockHeader.getSize() + (Integer.SIZE * 2);
    }

    /**
     * Instantiates a new Block.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     * @param transactions      the transactions
     */
    public Block(byte[] previousBlockHash, float protocolVersion, int blockHeight, Map<String, Transaction> transactions) {
        this.blockHeader = new BlockHeader(previousBlockHash,protocolVersion);
        this.blockHeight = blockHeight;

        this.transactionCounter = transactions.size();
        this.transactions = new Hashtable<>();
        this.transactions.putAll(transactions);

        this.size = this.blockHeader.getSize() + (Integer.SIZE * 2) + this.transactions.size();
    }

    /**
     * Gets transactions.
     *
     * @return the transactions
     */
    public Map<String, Transaction> getTransactions() {
        Map<String, Transaction> transactions = new Hashtable<>();
        transactions.putAll(this.transactions);

        return transactions;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets transaction counter.
     *
     * @return the transaction counter
     */
    public int getTransactionCounter() {
        return transactionCounter;
    }

    /**
     * Gets block height.
     *
     * @return the block height
     */
    public int getBlockHeight() {
        return blockHeight;
    }

    /**
     * Get previous block hash byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getPreviousBlockHash() {
        return this.blockHeader.getPreviousBlockHash();
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version
     */
    public float getProtocolVersion() {
        return this.blockHeader.getProtocolVersion();
    }

    /**
     * Gets nonce.
     *
     * @return the nonce
     */
    public int getNonce() {
        return this.blockHeader.getNonce();
    }

    /**
     * Get hash byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getHash() {
        var aux = this.blockHeader.getData();

        // TODO: SHA3 256
        SHA256.Digest sha256 = new SHA256.Digest();
        RIPEMD160.Digest ripemd160 = new RIPEMD160.Digest();

        var hash = sha256.digest(aux);

        return ripemd160.digest(hash);
    }

    /**
     * Add transaction.
     *
     * @param transaction the transaction
     */
    public void addTransaction(Transaction transaction) {
        this.transactions.put(Base64.toBase64String(transaction.getHash()), transaction);
        this.transactionCounter++;
    }
}
