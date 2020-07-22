package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;

/**
 * Blockchain data structure is an ordered, back-linked list of blocks of transactions/data.
 * BFT-Smart runs on top of this blockchain (more like blockchain is piece of the puzzle that is BFT-Smart),
 * and by running on top of a blockchain, making it more secure and robust.
 */
public class Blockchain implements Serializable {
    /* Attributes */
    private List<Block> blocks;
    private int sizeInMemory;

    private static Blockchain INSTANCE = null;

    private static final String GENESIS_STRING = "What to Know and What to Do About the Global Pandemic";

    private static final Logger logger = LoggerFactory.getLogger(Blockchain.class);

    private Blockchain() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Block genesisBlock = new Block(GENESIS_STRING.getBytes(StandardCharsets.UTF_8));
        genesisBlock.toDisk();

        this.blocks = new ArrayList<>();
        this.blocks.add(genesisBlock);
        this.sizeInMemory = genesisBlock.getSize();
    }

    private Blockchain(List<Block> blocks, int sizeInMemory) {
        this.blocks = new ArrayList<>(blocks);
        this.sizeInMemory = sizeInMemory;
    }

    /* Methods */

    /**
     * Gets the instance of Blockchain Singleton class.
     *
     * @return the Blockchain class instance
     */
    public static Blockchain getInstance() {
        if (INSTANCE == null) {
            Pair<Integer, List<Block>> r = getBlocksListFromDisk();
            if (r == null) {
                try {
                    INSTANCE = new Blockchain();
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                    logger.error("Unable to create Genesis Block", e);
                    INSTANCE = new Blockchain(new ArrayList<>(), 0);
                }
            }
            else
                INSTANCE = new Blockchain(r.getO2(), r.getO1());
        } else {
            try {
                INSTANCE = new Blockchain();
            } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                logger.error("Unable to create Genesis Block", e);
                INSTANCE = new Blockchain(new ArrayList<>(), 0);
            }
        }

        return INSTANCE;
    }

    /**
     * Reloads blocks from disk.
     */
    public void reloadBlocksFromDisk() {
        logger.info("Refreshing blocks from disk to memory");
        Pair<Integer, List<Block>> r = getBlocksListFromDisk();
        this.sizeInMemory = r.getO1();
        this.blocks = r.getO2();
    }

    /**
     * Tests if this blockchain is a valid blockchain.
     *
     * @return true if the block chain is valid or false otherwise
     */
    public boolean isChainValid() {
        if (this.blocks.size() == 0) return false;
        if (this.getMostRecentBlock().getBlockHeight() == 0) return true;

        try {
            Block currentBlock = this.getMostRecentBlock();
            int previousBlockHeight = currentBlock.getBlockHeight() - 1;
            Block previousBlock = this.getBlock(previousBlockHeight);

            if (previousBlockHeight == 0)
                return Arrays.equals(currentBlock.getPreviousBlockHash(), previousBlock.getHash());
            else
                return Arrays.equals(currentBlock.getPreviousBlockHash(), previousBlock.getHash()) &&
                        recursivePreviousBlockHashValidation(previousBlockHeight, previousBlockHeight - 1);

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error occurred while validating chain", e);
            return false;
        }
    }

    private boolean recursivePreviousBlockHashValidation(int bH1, int bH2) throws IOException, ClassNotFoundException {
        Block b1 = this.getBlock(bH1);
        Block b2 = this.getBlock(bH2);

        if (bH2 == 0)
            return Arrays.equals(b1.getPreviousBlockHash(), b2.getHash());
        else
            return Arrays.equals(b1.getPreviousBlockHash(), b2.getHash()) &&
                    recursivePreviousBlockHashValidation(b2.getBlockHeight(), b2.getBlockHeight() - 1);
    }

    /**
     * Adds a block to the front of the chain.
     *
     * @param block the block to be added
     * @return true if the block was added successfully or false otherwise
     */
    public boolean addBlock(Block block) {
        if (block == null) return false;
        if (block.getBlockHeight() <= this.getMostRecentBlock().getBlockHeight()) return false;

        this.blocks.add(0, block);
        this.sizeInMemory += block.getSize();
        block.toDisk();

        if (this.blocks.size() > 1)
            while (this.sizeInMemory > (Configuration.getInstance().getMemoryUsedForBlocks() * 1000000)) {
                Block b = this.blocks.remove(this.blocks.size() - 1);
                this.sizeInMemory -= b.getSize();
            }

        return true;
    }

    /* Getters */

    private static File[] getBlockFilesArray() {
        Configuration config = Configuration.getInstance();

        return new File(config.getBlockFileDirectory()).listFiles();
    }

    private static Pair<Integer, List<Block>> getBlocksListFromDisk() {
        List<Integer> blocksDisk = getBlockFileHeightArray();

        if (blocksDisk == null)
            return null;

        List<Block> blocks = new ArrayList<>();
        int sizeInMemory = 0;

        for (Integer i : blocksDisk) {
            try {
                Block b = Block.fromDisk(i);
                blocks.add(0, b);
                sizeInMemory += b.getSize();
            } catch (IOException | ClassNotFoundException ioException) {
                logger.error("Error retrieving block from disk", ioException);
            }

            if (blocks.size() > 1)
                while (sizeInMemory > (Configuration.getInstance().getMemoryUsedForBlocks() * 1000000)) {
                    Block b = blocks.remove(blocksDisk.size() - 1);
                    sizeInMemory -= b.getSize();
                }
        }

        return new Pair<>(sizeInMemory, blocks);
    }

    /**
     * Gets a list of block height from block files stored in disk.
     *
     * @return the block height list
     */
    public static List<Integer> getBlockFileHeightArray() {
        Configuration config = Configuration.getInstance();
        File[] blockFiles = getBlockFilesArray();

        if (blockFiles != null) {
            List<Integer> blockHeightList = new ArrayList<>();

            for (File blockFile : blockFiles) {
                String[] aux = blockFile.getName().split(config.getBlockFileBaseNameSeparator());

                if (!aux[0].equals(config.getBlockFileBaseName()))
                    continue;

                String blockHeightString = aux[1].split(Configuration.BLOCK_FILE_EXTENSION_SEPARATOR_SPLIT)[0];
                int currentFileBlockHeight = Integer.parseInt(blockHeightString);
                blockHeightList.add(currentFileBlockHeight);
            }

            blockHeightList.sort(Integer::compare);

            return blockHeightList;
        } else
            return null;
    }

    /**
     * Gets size of blocks in memory.
     *
     * @return the size in memory
     */
    public int getSizeInMemory() {
        return sizeInMemory;
    }

    /**
     * Gets the last added block to the chain, or in other words, the highest block in the blockchain
     *
     * @return the most recently created block
     */
    public Block getMostRecentBlock() {
        return this.blocks.get(0);
    }

    /**
     * Gets block, with defined block height, from memory or disk.
     * Will return NULL if error occurred while loading the block from disk.
     *
     * @param blockHeight the block height
     * @return the block
     * @throws NoSuchElementException No such element exception will be thrown if no matching block is found
     * @throws IOException            IO exception if an error while loading the block data from disk
     * @throws ClassNotFoundException Class not found exception if an error while converting block data to Block class instance
     */
    public Block getBlock(int blockHeight) throws NoSuchElementException, IOException, ClassNotFoundException {
        if (blockHeight > this.blocks.get(0).getBlockHeight())
            throw new NoSuchElementException("Requested block is above the most recent block");

        for (Block b : this.blocks)
            if (b.getBlockHeight() == blockHeight)
                return b;

        return Block.fromDisk(blockHeight);
    }

    @Override
    public String toString() {
        return "Blockchain: {" + System.lineSeparator() +
                "blocks: " + blocks + System.lineSeparator() +
                "}";
    }
}
