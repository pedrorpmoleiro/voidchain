package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import jdk.jshell.spi.ExecutionControl;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Blockchain data structure is an ordered, back-linked list of blocks of transactions/data.
 * BFT-Smart runs on top of this blockchain (more like blockchain is piece of the puzzle that is BFT-Smart),
 *  and by running on top of a blockchain, making it more secure and robust.
 */
public class Blockchain implements Serializable {
    /* Attributes */
    // TODO: Stack OR MAYBE MAP (?)
    private final List<Block> blocks;
    // TODO: MOVE TRANSACTION POOL TO REPLICA ?
    private final List<Transaction> transactionPool;

    /**
     * Instantiates the Blockchain data structure.
     * Keep in mind that can only exist a valid "chain".
     */
    public Blockchain() {
        var genesisBytes = "What to Know and What to Do About the Global Pandemic".getBytes(StandardCharsets.UTF_8);
        RIPEMD160.Digest hash = new RIPEMD160.Digest();

        Block genesisBlock = new Block(hash.digest(genesisBytes), Configuration.getInstance().getProtocolVersion(), 0, 0L, new byte[0]);

        this.blocks = new ArrayList<>();
        this.blocks.add(genesisBlock);

        this.transactionPool = new ArrayList<>();
    }

    /* Methods */
    /**
     * Tests if this blockchain is a valid blockchain
     *
     * @return the boolean
     */
    public boolean isChainValid() {
        Block currentBlock = this.getCurrentBlock();
        Block previousBlock = this.blocks.get(1);

        return Arrays.equals(currentBlock.getPreviousBlockHash(), previousBlock.getHash());
    }

    /**
     * Creates a new block, then adds it to the chain.
     *
     * @param timestamp the timestamp
     * @param nonce     the nonce
     * @return the block
     */
    public Block createBlock(long timestamp, byte[] nonce) {
        Block auxBlock = this.getCurrentBlock();

        Block block = new Block(auxBlock.getHash(), Configuration.getInstance().getProtocolVersion(), auxBlock.getBlockHeight() + 1,
                timestamp, nonce);

        this.blocks.add(0, block);

        return block;
    }

    /**
     * Creates a new block, with predefined transactions, then adds it to the chain.
     *
     * @param timestamp the timestamp
     * @param nonce     the nonce
     * @return the block
     */
    public Block createBlock(long timestamp, byte[] nonce, Map<byte[], Transaction> transactions) {
        Block auxBlock = this.getCurrentBlock();

        Block block = new Block(auxBlock.getHash(), Configuration.getInstance().getProtocolVersion(), auxBlock.getBlockHeight() + 1,
                transactions,timestamp, nonce);

        this.blocks.add(0, block);

        return block;
    }

    public Block createBlock(long timestamp, byte[] nonce, List<Transaction> transactions) {
        Block auxBlock = this.getCurrentBlock();

        Block block = new Block(auxBlock.getHash(), Configuration.getInstance().getProtocolVersion(), auxBlock.getBlockHeight() + 1,
                transactions,timestamp, nonce);

        this.blocks.add(0, block);

        return block;
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
     * @return the current block
     */
    public Block getCurrentBlock() {
        return this.blocks.get(0);
    }

    // TODO: IMPLEMENT & USE BLOCKS STORED ON DISK
    public Block getBlock(int blockHeight) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("This is not yet implemented 😢");
    }

    @Override
    public String toString() {
        return "Blockchain: {" + System.lineSeparator() +
                "blocks: " + blocks + System.lineSeparator() +
                "}";
    }
}
