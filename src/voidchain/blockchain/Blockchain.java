package voidchain.blockchain;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Blockchain {
    private ArrayList<Block> blockchain;
    private int blockHeight;

    public Blockchain() {
        this.blockchain = new ArrayList<>();

        String aux = "GENESIS";
        Block genesisBlock = new Block(aux.getBytes(StandardCharsets.UTF_8));
        this.blockchain.add(genesisBlock);

        Block secondBlock = new Block(genesisBlock.getHash());
        this.blockchain.add(secondBlock);

        this.blockHeight = 2;
    }

    public Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;

        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            if (!Arrays.equals(previousBlock.getHash(), currentBlock.getPreviousHash())) {
                return false;
            }
        }

        return true;
    }

    public Block getCurrentBlock() {
        return this.blockchain.get(this.blockHeight - 1);
    }

    public void createBlock() {
        Block newBlock = new Block(this.getCurrentBlock().getHash());

        this.blockchain.add(newBlock);

        this.blockHeight++;
    }

    public int getBlockHeight() {
        return blockHeight;
    }
}
