package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Base64;

import java.util.Hashtable;
import java.util.Map;

/**
 * A block is where the transactions/data are stored in,
 *      and then the block is added to the blockchain, if it's valid
 * The structure of a block follows: version, a header (which is its own class),
 *      size, transaction counter and its height
 * In the blockchain, what identifies a block is its ID, which is the hash of its header (block header)
 * Keep in mind that the block hash is not stored in the blocks data structure nor in the blockchain,
 *      it needs to be calculated if needed.
 *
 */
public class Block {
    /* Attributes */
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

    /* Methods */
    /**
     * Gets all the transactions that are stored in a block
     * For security reasons, we do not give direct access to the transactions,
     *      we return a copy of it.
     *
     * @return the transactions (Map<String, Transaction>)
     */
    public Map<String, Transaction> getTransactions() {
        Map<String, Transaction> transactions = new Hashtable<>();
        transactions.putAll(this.transactions);

        return transactions;
    }

    /**
     * Gets size of the block
     *
     * @return the size
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
     * Gets the hash of the last block in the chain.
     * Or in other words, it gets the blocks parent.
     *
     * @return the previous block hash/ parent hash (byte [ ])
     */
    public byte[] getPreviousBlockHash() {
        return this.blockHeader.getPreviousBlockHash();
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version (float)
     */
    public float getProtocolVersion() {
        return this.blockHeader.getProtocolVersion();
    }

    /**
     * Gets nonce.
     *
     * @return the nonce (int)
     */
    public int getNonce() {
        return this.blockHeader.getNonce();
    }

    /**
     * Gets calculates the hash of the block.
     * To calculate the hash of a block, we double hash its header (block header)
     *      SHA256(RIPEMD160(blockHeader))
     *
     *
     * @return the block hash / black header hash 8byte [ ])
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
     * Adds transactions/data the newly created block
     *
     * @param transaction the transaction
     */
    public void addTransaction(Transaction transaction) {
        this.transactions.put(Base64.toBase64String(transaction.getHash()), transaction);
        this.transactionCounter++;
    }
}
