package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Blockchain data structure is an ordered, back-linked list of blocks of transactions/data.
 * BFT-Smart runs on top of this blockchain, and by running on top of a blockchain,
 * is more secure and robust.
 */
public class Blockchain implements Serializable {
    /* Attributes */
    public static float PROTOCOL_VERSION = 0.1f;
    // TODO: Stack (?)
    private final List<Block> blocks;

    /**
     * Instantiates the Blockchain data structure.
     * Keep in mind that can only exist a valid "chain".
     */
    public Blockchain() {
        var genesisBytes = "What to Know and What to Do About the Global Pandemic".getBytes(StandardCharsets.UTF_8);
        RIPEMD160.Digest hash = new RIPEMD160.Digest();

        Block genesisBlock = new Block(hash.digest(genesisBytes), PROTOCOL_VERSION, 0, 0L, 0);

        this.blocks = new LinkedList<>();
        this.blocks.add(genesisBlock);
    }

    /* Methods */

    /**
     * Tests if this blockchain is a valid blockchain
     *
     * @return the boolean
     */
    @Deprecated
    public Boolean isChainValid() {
        Block currentBlock = this.getCurrentBlock();
        Block previousBlock = this.blocks.get(1);

        return Arrays.equals(currentBlock.getPreviousBlockHash(), previousBlock.getHash());
    }

    /**
     * Creates a new block, then adds it to the chain
     *
     * @return the block
     */
    public Block createBlock() {
        Block auxBlock = this.getCurrentBlock();

        Block block = new Block(auxBlock.getHash(), PROTOCOL_VERSION, auxBlock.getBlockHeight() + 1);

        this.blocks.add(0, block);

        return block;
    }

    /**
     * Creates a new block with predefined timestamp and nonce, then adds it to the chain.
     *
     * @param timestamp the timestamp
     * @param nonce     the nonce
     * @return the block
     */
    public Block createBlock(long timestamp, int nonce) {
        Block auxBlock = this.getCurrentBlock();

        Block block = new Block(auxBlock.getHash(), PROTOCOL_VERSION, auxBlock.getBlockHeight() + 1, timestamp, nonce);

        this.blocks.add(0, block);

        return block;
    }

    /* Getters */

    /**
     * Gets the last added block to the chain, or in other words, the highest block in the blockchain
     *
     * @return the current block
     */
    public Block getCurrentBlock() {
        return this.blocks.get(0);
    }

    @Override
    public String toString() {
        return "Blockchain: {" + System.lineSeparator() +
                "blocks: " + blocks + System.lineSeparator() +
                "}";
    }
}
