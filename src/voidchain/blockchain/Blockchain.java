package voidchain.blockchain;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The type Blockchain.
 */
public class Blockchain {
    private static final float PROTOCOL_VERSION = 0.1f;

    // TODO: Stack
    private List<Block> blocks;

    /**
     * Instantiates a new Blockchain.
     */
    public Blockchain() {
        var genesisBytes = "What to Know and What to Do About the Global Pandemic".getBytes(StandardCharsets.UTF_8);
        RIPEMD160.Digest hash = new RIPEMD160.Digest();

        Block genesisBlock = new Block(hash.digest(genesisBytes), PROTOCOL_VERSION, 0);

        this.blocks = new LinkedList<>();
        this.blocks.add(genesisBlock);

        this.createBlock();
    }

    /**
     * Is chain valid boolean.
     *
     * @return the boolean
     */
    public Boolean isChainValid() {
        Block currentBlock = this.getCurrentBlock();
        Block previousBlock = this.blocks.get(1);

        return Arrays.equals(currentBlock.getPreviousBlockHash(), previousBlock.getHash());
    }

    /**
     * Gets current block.
     *
     * @return the current block
     */
    public Block getCurrentBlock() {
        return this.blocks.get(0);
    }

    /**
     * Create block block.
     *
     * @return the block
     */
    public Block createBlock() {
        Block auxBlock = this.getCurrentBlock();

        Block block = new Block(auxBlock.getHash(), PROTOCOL_VERSION, auxBlock.getBlockHeight() + 1);

        this.blocks.add(0, block);

        return block;
    }
}
