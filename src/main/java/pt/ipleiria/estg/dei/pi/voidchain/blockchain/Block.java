package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import bftsmart.reconfiguration.util.TOMConfiguration;

import bitcoinj.Base58;

import org.bouncycastle.util.encoders.Base64;

import pt.ipleiria.estg.dei.pi.voidchain.util.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * A block is where the transactions/data are stored in,
 * and then the block is added to the Blockchain if it's valid
 * The structure of a block follows: version, a header (which is its own class),
 * size, transaction counter, and its height.
 * In the Blockchain, what identifies a block is its ID, which is the hash of its header (block header)
 * Keep in mind that the block hash is not stored in the blocks data structure nor in the Blockchain,
 * it needs to be calculated if needed.
 */
public class Block implements Serializable {
    /* Attributes */
    private final Map<byte[], Transaction> transactions;
    private final BlockHeader blockHeader;
    private final int transactionCounter;
    private final int blockHeight;

    /* Constructors */

    /**
     * Instantiates a Static Genesis Block.
     *
     * @param genesisBytes the genesis data
     * @param signature    the signature of the data
     * @throws IllegalArgumentException illegal argument exception will be thrown if transaction size exceeds max                                  value of transaction
     */
    // FOR USE BY BLOCKCHAIN CLASS TO CREATE STAIC GENESIS BLOCK
    protected Block(byte[] genesisBytes, byte[] signature) {
        Configuration config = Configuration.getInstance();

        // Date and time of the first meeting to plan the development of this project
        long timestamp = 1582135200000L;

        Transaction t = new Transaction(genesisBytes, config.getProtocolVersion(), timestamp, signature);
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
     * Instantiates a new Genesis Block.
     *
     * @param genesisBytes the genesis bytes
     * @throws NoSuchAlgorithmException no such algorithm exception will be thrown if algorithm for signing the                                  transaction
     * @throws InvalidKeyException      invalid key exception will be thrown if private key is invalid
     * @throws SignatureException       signature exception will be thrown if error occurs while signing the transaction                                  data
     * @throws IOException              the io exception
     * @throws NoSuchProviderException  the no such provider exception
     * @throws InvalidKeySpecException  the invalid key spec exception
     */
    // FOR USE BY BLOCKCHAIN CLASS TO CREATE NEW GENESIS BLOCK
    protected Block(byte[] genesisBytes) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            IOException, NoSuchProviderException, InvalidKeySpecException {

        Configuration config = Configuration.getInstance();

        // Date and time of the first meeting to plan the development of this project
        long timestamp = 1582135200000L;

        TOMConfiguration tomConf = new TOMConfiguration(-100, Configuration.CONFIG_DIR, null);

        Signature signature = Signature.getInstance(tomConf.getSignatureAlgorithm());
        signature.initSign(Keys.getPrivKey(Keys.getPrivGenesisKeyBytes(), tomConf));
        signature.update(genesisBytes);
        byte[] signatureBytes = signature.sign();

        System.out.println("New Genesis Block Signature :: " + Base64.toBase64String(signatureBytes));

        Transaction t = new Transaction(genesisBytes, config.getProtocolVersion(), timestamp, signatureBytes);
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

        return Storage.writeObjectToDisk(this, config.getBlockFileDirectoryFull(),
                config.getBlockFileBaseName() + Configuration.FILE_NAME_SEPARATOR + this.blockHeight +
                        Configuration.FILE_EXTENSION_SEPARATOR + config.getDataFileExtension());
    }

    /**
     * Loads block from disk.
     * <br>
     * Will return NULL upon block not found or error occurred while loading from disk
     *
     * @param blockHeight the block height of the wanted block
     * @return the block
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static Block fromDisk(int blockHeight) throws IOException, ClassNotFoundException {
        Configuration config = Configuration.getInstance();

        return (Block) Storage.readObjectFromDisk(config.getBlockFileDirectoryFull() +
                config.getBlockFileBaseName() + Configuration.FILE_NAME_SEPARATOR + blockHeight +
                Configuration.FILE_EXTENSION_SEPARATOR + config.getDataFileExtension());
    }

    /* Getters */

    /**
     * Gets all the transactions that are stored in a block
     * For security reasons, we do not give direct access to the transactions,
     * a copy of the original is returned.
     *
     * @return the transactions
     */
    public Map<byte[], Transaction> getTransactions() {
        return new Hashtable<>(this.transactions);
    }

    /**
     * Calculates size of the block in bytes.
     * Includes the size of transactions.
     *
     * @return the size
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
     * @return the transaction counter
     */
    public int getTransactionCounter() {
        return this.transactionCounter;
    }

    /**
     * Gets block height.
     *
     * @return the block height
     */
    public int getBlockHeight() {
        return this.blockHeight;
    }

    /**
     * Gets the transactions merkle tree root, from the block header.
     *
     * @return the merkle tree root
     */
    public byte[] getMerkleRoot() {
        return this.blockHeader.merkleRoot;
    }

    /**
     * Gets the Epoch time the block was created, from the block header
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return this.blockHeader.timestamp;
    }

    /**
     * Gets the hash of the previous block in the chain, from the block header.
     *
     * @return the previous block hash
     */
    public byte[] getPreviousBlockHash() {
        return this.blockHeader.previousBlockHash;
    }

    /**
     * Gets protocol version, from the block header.
     *
     * @return the protocol version
     */
    public String getProtocolVersion() {
        return this.blockHeader.protocolVersion;
    }

    /**
     * Calculates the hash of the block.
     * To calculate the hash of a block, we double hash it's header (block header).
     * <br>
     * SHA3_512(RIPEMD160(blockHeader))
     * <br>
     * Will return byte[0] if error occurred while calculating hash.
     *
     * @return the block hash
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

    /**
     * Gets block file full name.
     *
     * @return the block file full name
     */
    public String getBlockFileFullName() {
        return getBlockFileFullName(this.blockHeight);
    }

    /**
     * Gets block file full name.
     *
     * @param height the height
     * @return the block file full name
     */
    public static String getBlockFileFullName(int height) {
        Configuration config = Configuration.getInstance();

        return config.getBlockFileDirectoryFull() + File.separator +
                config.getBlockFileBaseName() + Configuration.FILE_NAME_SEPARATOR +
                height + Configuration.FILE_EXTENSION_SEPARATOR + config.getDataFileExtension();
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
                "hash: " + Base58.encode(this.getHash()) + System.lineSeparator() +
                "}";
    }
}
