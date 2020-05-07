package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;

import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Blockchain data structure is an ordered, back-linked list of blocks of transactions/data.
 * BFT-Smart runs on top of this blockchain (more like blockchain is piece of the puzzle that is BFT-Smart),
 * and by running on top of a blockchain, making it more secure and robust.
 */
public class Blockchain implements Serializable {
    /* Attributes */
    // TODO: Stack OR MAYBE MAP (?)
    private final List<Block> blocks;
    // TODO: MOVE TRANSACTION POOL TO REPLICA ?
    private final List<Transaction> transactionPool;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Instantiates the Blockchain data structure.
     * Keep in mind that can only exist a valid "chain".
     */
    public Blockchain() {
        /*
        var genesisBytes = "What to Know and What to Do About the Global Pandemic".getBytes(StandardCharsets.UTF_8);
        RIPEMD160.Digest hash = new RIPEMD160.Digest();
        Block genesisBlock = new Block(hash.digest(genesisBytes));
        */

        Block genesisBlock = new Block("What to Know and What to Do About the Global Pandemic".getBytes(StandardCharsets.UTF_8));

        this.blocks = new ArrayList<>();
        this.blocks.add(genesisBlock);

        this.transactionPool = new ArrayList<>();
    }

    /* Methods */

    /**
     * Tests if this blockchain is a valid blockchain.
     *
     * @return true if the block chain is valid or false otherwise
     */
    // TODO: DOES IT WORK IF PREVIOUS PREVIOUS BLOCK ALTERED ?
    public boolean isChainValid() {
        Block currentBlock = this.getCurrentBlock();
        Block previousBlock = this.blocks.get(1);

        return Arrays.equals(currentBlock.getPreviousBlockHash(), previousBlock.getHash());
    }

    /**
     * Creates a new block, then adds it to the chain.
     *
     * @param timestamp the timestamp (long)
     * @param nonce     the nonce (byte[])
     * @return the newly created block
     */
    public Block createBlock(long timestamp, byte[] nonce) {
        try {
            Block auxBlock = this.getCurrentBlock();

            Block block = new Block(auxBlock.getHash(), Configuration.getInstance().getProtocolVersion(),
                    auxBlock.getBlockHeight() + 1, new Hashtable<>(), timestamp, nonce);

            this.blocks.add(0, block);
            return block;

        } catch (InstantiationException e) {
            logger.error("Error occurred while creating new block", e);
            return Block.DEFAULT_BLOCK;
        }
    }

    /**
     * Creates a new block, with predefined transactions, then adds it to the chain.
     *
     * @param timestamp    the timestamp (long)
     * @param nonce        the nonce (byte[])
     * @param transactions the transactions (Map)
     * @return the newly created block
     */
    public Block createBlock(long timestamp, byte[] nonce, Map<byte[], Transaction> transactions) {
        try {
            Block auxBlock = this.getCurrentBlock();

            Block block = new Block(auxBlock.getHash(), Configuration.getInstance().getProtocolVersion(), auxBlock.getBlockHeight() + 1,
                    transactions, timestamp, nonce);

            this.blocks.add(0, block);
            return block;
        } catch (InstantiationException e) {
            logger.error("Error occurred while creating new block", e);
            return Block.DEFAULT_BLOCK;
        }
    }

    /**
     * Creates a new block, with predefined transactions, then adds it to the chain.
     *
     * @param timestamp    the timestamp (long)
     * @param nonce        the nonce (byte[])
     * @param transactions the transactions (List)
     * @return the newly created block
     */
    // TODO: REMOVE ?
    public Block createBlock(long timestamp, byte[] nonce, List<Transaction> transactions) {
        try {
            Block auxBlock = this.getCurrentBlock();

            Block block = new Block(auxBlock.getHash(), Configuration.getInstance().getProtocolVersion(), auxBlock.getBlockHeight() + 1,
                    transactions, timestamp, nonce);

            this.blocks.add(0, block);
            return block;
        } catch (InstantiationException e) {
            logger.error("Error occurred while creating new block", e);
            return Block.DEFAULT_BLOCK;
        }
    }

    private void processNewBlock() {
        int transactionsPerBlock = Configuration.getInstance().getNumTransactionsInBlock();

        if (this.transactionPool.size() < transactionsPerBlock) {
            return;
        }

        List<Transaction> transactions = new ArrayList<>();

        while (transactions.size() < transactionsPerBlock) {
            transactions.add(this.transactionPool.get(0));
            this.transactionPool.remove(0);
        }

        /*
            TODO: TIMESTAMP & NONCE (?) & READ BELOW
            REPLICAS SHOULD COMMUNICATE TO ADD NEW BLOCK
            TIMESTAMP AND NONCE WOULD COME FROM MSGCTX
        */

        this.createBlock(0, new byte[0], transactions);
    }

    public boolean addTransaction(Transaction transaction) {
        int aux = this.transactionPool.size();
        this.transactionPool.add(transaction);

        if (aux == this.transactionPool.size() || (aux + 1) != this.transactionPool.size()) {
            return false;
        }

        processNewBlock();
        return true;
    }

    public boolean addTransactions(List<Transaction> transactions) {
        int aux = this.transactionPool.size();
        this.transactionPool.addAll(transactions);

        if (aux == this.transactionPool.size() || (aux + transactions.size()) != this.transactionPool.size()) {
            return false;
        }

        processNewBlock();
        return true;
    }

    /* Getters */

    /**
     * Gets the last added block to the chain, or in other words, the highest block in the blockchain
     *
     * @return the most recently created block
     */
    public Block getCurrentBlock() {
        return this.blocks.get(0);
    }

    /**
     * Gets block, with defined block height, from memory or disk.
     * Will return DEFAULT_BLOCK if error occured while loading the block from disk.
     *
     * @param blockHeight the block height
     * @return the block
     * @throws NoSuchElementException If no matching block found exception will be thrown
     */
    public Block getBlock(int blockHeight) throws NoSuchElementException {
        for (Block b : blocks) {
            if (b.getBlockHeight() == blockHeight) {
                return b;
            }
        }

        Configuration config = Configuration.getInstance();

        File[] blockFiles = new File(config.getBlockFileDirectory()).listFiles();
        if (blockFiles == null)
            throw new NoSuchElementException("Block doesn't exist");

        String wantedFile = config.getBlockFileDirectory() + config.getBlockFileBaseName() +
                blockHeight + config.getBlockFileExtension();

        for (File f : blockFiles) {
            if (f.getName().equals(wantedFile)) {
                try {
                    return (Block) Storage.readFromDiskCompressed(f.getName());
                } catch (IOException | ClassNotFoundException e) {
                    logger.error("Error getting block " + blockHeight + " from disk", e);

                    return Block.DEFAULT_BLOCK;
                }
            }
        }

        throw new NoSuchElementException("Block doesn't exist");
    }

    @Override
    public String toString() {
        return "Blockchain: {" + System.lineSeparator() +
                "blocks: " + blocks + System.lineSeparator() +
                "}";
    }
}
