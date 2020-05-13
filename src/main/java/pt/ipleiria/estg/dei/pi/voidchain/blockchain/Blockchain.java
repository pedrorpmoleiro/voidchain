package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import bftsmart.reconfiguration.util.HostsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Blockchain data structure is an ordered, back-linked list of blocks of transactions/data.
 * BFT-Smart runs on top of this blockchain (more like blockchain is piece of the puzzle that is BFT-Smart),
 * and by running on top of a blockchain, making it more secure and robust.
 */
public class Blockchain implements Serializable {
    /* Attributes */
    // TODO: STACK OR MAYBE MAP (?)
    private final List<Block> blocks;

    private static Blockchain INSTANCE = null;

    private static final String GENESIS_STRING = "What to Know and What to Do About the Global Pandemic";

    private static final Logger logger = LoggerFactory.getLogger(Blockchain.class.getName());

    /**
     * Instantiates the Blockchain data structure.
     * Keep in mind that can only exist a valid "chain".
     */
    private Blockchain() {
        Block genesisBlock = new Block(GENESIS_STRING.getBytes(StandardCharsets.UTF_8));
        genesisBlock.toDisk();

        this.blocks = new ArrayList<>();
        this.blocks.add(genesisBlock);
    }

    private Blockchain(List<Block> blocks) {
        this.blocks = blocks;
    }

    /* Methods */

    // TODO: BLOCK DATA VALIDATE
    public static Blockchain getInstance() {
        if (INSTANCE == null) {
            Configuration config = Configuration.getInstance();

            File[] blockFiles = new File(config.getBlockFileDirectory()).listFiles();
            if (blockFiles != null) {
                List<Block> blocksDisk = new ArrayList<>();
                int previousFileBlockHeight = -1;

                for (File blockFile : blockFiles) {
                    String[] aux = blockFile.getName().split(config.getBlockFileBaseNameSeparator());

                    if (!aux[0].equals(config.getBlockFileBaseName())) {
                        continue;
                    }

                    String blockHeightString = aux[1].split(config.getBlockFileExtensionSeparatorSplit())[0];
                    int currentFileBlockHeight = Integer.parseInt(blockHeightString);

                    if (currentFileBlockHeight > previousFileBlockHeight) {
                        previousFileBlockHeight = currentFileBlockHeight;
                        try {
                            blocksDisk.add(0, (Block) Storage.readFromDiskCompressed(blockFile.getAbsolutePath()));
                        } catch (IOException | ClassNotFoundException e) {
                            logger.error("Error loading block from disk", e);
                            continue;
                        }

                        while (blocksDisk.size() > config.getNumBlockInMemory()) {
                            blocksDisk.remove(blocksDisk.size() - 1);
                        }
                    }
                }

                INSTANCE = new Blockchain(blocksDisk);
            } else
                INSTANCE = new Blockchain();
        }

        return INSTANCE;
    }

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
     * <p>
     * Will return NULL if error occured while creating the block.
     *
     * @param timestamp the timestamp (long)
     * @param nonce     the nonce (byte[])
     * @return the newly created block
     */
    // TODO: REMOVE ?
    public Block createBlock(long timestamp, byte[] nonce) {
        try {
            Block auxBlock = this.getCurrentBlock();

            Block block = new Block(auxBlock.getHash(), Configuration.getInstance().getProtocolVersion(),
                    auxBlock.getBlockHeight() + 1, new Hashtable<>(), timestamp, nonce);

            this.blocks.add(0, block);

            // TODO: TO DISK
            block.toDisk();

            // TODO: ONLY X BLOCKS IN MEMORY
            while (this.blocks.size() > Configuration.getInstance().getNumBlockInMemory()) {
                this.blocks.remove(this.blocks.size() - 1);
            }

            return block;

        } catch (InstantiationException e) {
            logger.error("Error occurred while creating new block", e);
            return null;
        }
    }

    /**
     * Creates a new block, with predefined transactions, then adds it to the chain.
     * <p>
     * Will return NULL if error occured while creating the block.
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

            // TODO: TO DISK
            block.toDisk();

            // TODO: ONLY X BLOCKS IN MEMORY
            while (this.blocks.size() > Configuration.getInstance().getNumBlockInMemory()) {
                this.blocks.remove(this.blocks.size() - 1);
            }

            return block;

        } catch (InstantiationException e) {
            logger.error("Error occurred while creating new block", e);
            return null;
        }
    }

    /**
     * Creates a new block, with predefined transactions, then adds it to the chain.
     * <p>
     * Will return NULL if error occured while creating the block.
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

            // TODO: TO DISK
            block.toDisk();

            // TODO: ONLY X BLOCKS IN MEMORY
            while (this.blocks.size() > Configuration.getInstance().getNumBlockInMemory()) {
                this.blocks.remove(this.blocks.size() - 1);
            }

            return block;

        } catch (InstantiationException e) {
            logger.error("Error occurred while creating new block", e);
            return null;
        }
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
     * Will return NULL if error occured while loading the block from disk.
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

                    return null;
                }
            }
        }

        throw new NoSuchElementException("Requested block doesn't exist");
    }

    @Override
    public String toString() {
        return "Blockchain: {" + System.lineSeparator() +
                "blocks: " + blocks + System.lineSeparator() +
                "}";
    }
}
