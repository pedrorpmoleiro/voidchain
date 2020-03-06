package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Util;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Set<Block> blockchain = new LinkedHashSet<>();
        //Block mostRecentBlock = null;

        String firstPreviousHash = "GENESIS";
        Block genesisBlock = new Block(firstPreviousHash.getBytes(StandardCharsets.UTF_8));
        System.out.println("GENESIS HASH: " + Util.convertByteArrayToHexString(genesisBlock.getHash()));
        blockchain.add(genesisBlock);

        Block secondBlock = new Block(genesisBlock.getHash());
        blockchain.add(secondBlock);
        //mostRecentBlock = secondBlock;
        System.out.println("SECOND BLOCK HASH: " + Util.convertByteArrayToHexString(secondBlock.getHash()));

        secondBlock.addTransaction(new Transaction("1st Transaction"));
        System.out.println("SECOND HASH (1 Transaction): " + Util.convertByteArrayToHexString(secondBlock.getHash()));

        secondBlock.addTransaction(new Transaction("THE SECOND"));
        System.out.println("SECOND HASH (2 Transaction): " + Util.convertByteArrayToHexString(secondBlock.getHash()));

        secondBlock.addTransaction(new Transaction("END"));
        System.out.println("SECOND HASH (3 Transaction): " + Util.convertByteArrayToHexString(secondBlock.getHash()));
    }
}
