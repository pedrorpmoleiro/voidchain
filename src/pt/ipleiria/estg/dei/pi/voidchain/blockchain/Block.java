package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.util.encoders.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.util.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

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
    private static final Logger logger = LoggerFactory.getLogger(Block.class.getName());

    private Map<byte[], Transaction> transactions;
    private final BlockHeader blockHeader;
    private int transactionCounter;
    private final int blockHeight;

    /**
     * Instantiates a new Genesis Block.
     *
     * @param genesisBytes the genesis bytes
     */
    // FOR USE BY BLOCKCHAIN CLASS TO CREATE GENESIS BLOCK
    protected Block(byte[] genesisBytes) {
        Configuration config = Configuration.getInstance();

        // Date and time of the first meeting to plan the development of this project
        long timestamp = 1582135200000L;

        Transaction t = new Transaction(genesisBytes, config.getProtocolVersion(), timestamp);
        this.transactions = new Hashtable<>();
        this.transactions.put(t.getHash(), t);

        byte[] nonce = new byte[10];
        new Random(timestamp).nextBytes(nonce);

        this.blockHeader = new BlockHeader(new byte[0], config.getProtocolVersion(), timestamp,
                nonce, MerkleTree.getMerkleRoot(this.transactions));
        this.blockHeight = 0;
        this.transactionCounter = 1;
    }

    /**
     * Instantiates a new Block.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     * @param transactions      the transactions (Map)
     * @param timestamp         the timestamp
     * @param nonce             the nonce
     * @throws InstantiationException instantiation exception will be thrown if error occurs while calculating merkle tree root
     */
    public Block(byte[] previousBlockHash, String protocolVersion, int blockHeight,
                 Map<byte[], Transaction> transactions, long timestamp, byte[] nonce) throws InstantiationException {

        byte[] merkleRoot;
        if (transactions.size() > 0) {
            merkleRoot = MerkleTree.getMerkleRoot(transactions);

            if (Arrays.equals(merkleRoot, new byte[0]))
                throw new InstantiationException("Error occurred while calculating merkle tree root");
        } else
            merkleRoot = new byte[0];

        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion, timestamp, nonce, merkleRoot);
        this.blockHeight = blockHeight;
        this.transactionCounter = transactions.size();
        this.transactions = new Hashtable<>(transactions);
    }

    /**
     * Instantiates a new Block.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param blockHeight       the block height
     * @param transactions      the transactions (List)
     * @param timestamp         the timestamp
     * @param nonce             the nonce
     * @throws InstantiationException instantiation exception will be thrown if error occurs while calculating merkle tree root
     */
    public Block(byte[] previousBlockHash, String protocolVersion, int blockHeight,
                 List<Transaction> transactions, long timestamp, byte[] nonce) throws InstantiationException {
        this.transactions = new Hashtable<>(Converters.transactionListToMap(transactions));

        byte[] merkleRoot;
        if (transactions.size() > 0) {
            merkleRoot = MerkleTree.getMerkleRoot(this.transactions);

            if (Arrays.equals(merkleRoot, new byte[0]))
                throw new InstantiationException("Error occurred while calculating merkle tree root");
        } else
            merkleRoot = new byte[0];

        this.blockHeader = new BlockHeader(previousBlockHash, protocolVersion, timestamp, nonce, merkleRoot);
        this.blockHeight = blockHeight;
        this.transactionCounter = transactions.size();
    }

    /* Methods */

    /**
     * Saves block to disk according to current system configurations.
     *
     * @return true if the block was saved to disk or false if an error occurred
     */
    public boolean toDisk() {
        Configuration config = Configuration.getInstance();

        return Storage.writeObjectToDisk(this, config.getBlockFileDirectory(),
                config.getBlockFileBaseName() + config.getBlockFileBaseNameSeparator() + this.blockHeight +
                        config.BLOCK_FILE_EXTENSION_SEPARATOR + config.getBlockFileExtension());
    }

    /**
     * Loads block from disk.
     * <p>
     * Will return NULL upon block not found or error occurred while loading from disk
     *
     * @param blockHeight the block height of the wanted block
     * @return the block (Block)
     */
    public static Block fromDisk(int blockHeight) {
        try {
            Configuration config = Configuration.getInstance();
            return (Block) Storage.readObjectFromDisk(config.getBlockFileDirectory() +
                    config.getBlockFileBaseName() + config.getBlockFileBaseNameSeparator() + blockHeight +
                    config.BLOCK_FILE_EXTENSION_SEPARATOR + config.getBlockFileExtension());
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error getting block " + blockHeight + " from disk", e);
            return null;
        }
    }

    /* Getters */

    /**
     * Gets all the transactions that are stored in a block
     * For security reasons, we do not give direct access to the transactions,
     * a copy of the original is returned.
     *
     * @return the transactions (Map<String, Transaction>)
     */
    public Map<byte[], Transaction> getTransactions() {
        return new Hashtable<>(this.transactions);
    }

    /**
     * Calculates size of the block in bytes.
     * Includes the size of transactions.
     *
     * @return the size (int)
     */
    public int getSize() {
        int allTransactionSize = 0;

        for (Transaction t : transactions.values())
            allTransactionSize += t.getSize();

        return this.blockHeader.getSize() + (Integer.BYTES * 2) + allTransactionSize;
    }

    /**
     * Gets the number of transactions inside a block.
     *
     * @return the transaction counter (int)
     */
    public int getTransactionCounter() {
        return this.transactionCounter;
    }

    /**
     * Gets block height.
     *
     * @return the block height (int)
     */
    public int getBlockHeight() {
        return this.blockHeight;
    }

    /**
     * Gets the transactions merkle tree root, from the block header.
     *
     * @return the merkle tree root (byte[])
     */
    public byte[] getMerkleRoot() {
        return this.blockHeader.merkleRoot;
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
     * @return the protocol version (String)
     */
    public String getProtocolVersion() {
        return this.blockHeader.protocolVersion;
    }

    /**
     * Calculates the hash of the block.
     * To calculate the hash of a block, we double hash it's header (block header).
     * <p>
     * SHA3_512(RIPEMD160(blockHeader))
     * <p>
     * Will return byte[0] if error occurred while calculating hash.
     *
     * @return the block hash (byte[])
     */
    public byte[] getHash() {
        byte[] blockHeaderData = this.blockHeader.getData();
        byte[] aux = new byte[0];

        if (Arrays.equals(blockHeaderData, aux)) {
            return aux;
        }

        return Hash.calculateSHA3512RIPEMD160(blockHeaderData);
    }

    /**
     * Gets a copy of the block without any transactions.
     *
     * @return the block no transactions
     */
    public BlockNoTransactions getBlockNoTransactions() {
        return new BlockNoTransactions(this.blockHeader.clone(), this.transactionCounter, this.getBlockHeight(),
                this.getSize());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return transactionCounter == block.transactionCounter &&
                blockHeight == block.blockHeight &&
                blockHeader.equals(block.blockHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockHeader, transactionCounter, blockHeight);
    }

    @Override
    public String toString() {
        return "Block: {" + System.lineSeparator() +
                "transactions: " + this.transactions.values() + System.lineSeparator() +
                this.blockHeader + System.lineSeparator() +
                "size: " + this.getSize() + System.lineSeparator() +
                "transaction counter: " + this.transactionCounter + System.lineSeparator() +
                "block height: " + this.blockHeight + System.lineSeparator() +
                "hash: " + Base64.toBase64String(this.getHash()) + System.lineSeparator() +
                "}";
    }
}
