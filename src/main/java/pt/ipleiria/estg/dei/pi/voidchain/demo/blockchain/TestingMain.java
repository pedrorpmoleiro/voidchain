package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;

import java.nio.charset.StandardCharsets;

public class TestingMain {
    public static void main(String[] args) {
        Blockchain voidchain = new Blockchain();

        voidchain.getCurrentBlock().addTransaction(
                new Transaction("FIRST TRANSACTION".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));

        Block midBlock = voidchain.getCurrentBlock(); // SECOND BLOCK

        voidchain.createBlock().addTransaction(new Transaction("NEW TRANSACTION".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));
        voidchain.createBlock().addTransaction(new Transaction("NEW TRANSACTION".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));
        voidchain.createBlock().addTransaction(new Transaction("NEW TRANSACTION".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));

        System.out.println("NUMBER OF BLOCKS: " + (voidchain.getCurrentBlock().getBlockHeight() + 1));

        voidchain.getCurrentBlock().addTransaction(new Transaction("NEW TRANSACTION".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));

        System.out.println("IS BLOCKCHAIN VALID: " + voidchain.isChainValid());

        System.out.println("ALTERING SECOND BLOCK (ADDING TRANSACTION)");
        midBlock.addTransaction(new Transaction("BREAKING TRANSACTION".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));

        System.out.println("IS BLOCKCHAIN VALID: " + voidchain.isChainValid());
    }
}
