package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;

import java.nio.charset.StandardCharsets;

@Deprecated
public class TestingMain {
    public static void main(String[] args) {
        Blockchain voidchain = new Blockchain();

        voidchain.createBlock();
        voidchain.getCurrentBlock().addTransaction(new Transaction("TRANSACTION 1".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));
        voidchain.getCurrentBlock().addTransaction(new Transaction("TRANSACTION 2".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));
        voidchain.getCurrentBlock().addTransaction(new Transaction("TRANSACTION 3".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));
        voidchain.getCurrentBlock().addTransaction(new Transaction("TRANSACTION 4".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION));
        System.out.println(voidchain.getCurrentBlock().toString());

        var mapTransactions = voidchain.getCurrentBlock().getTransactions();
        voidchain.createBlock();
        voidchain.getCurrentBlock().addTransactions(mapTransactions);
        System.out.println(voidchain.getCurrentBlock().toString());

        /*voidchain.getCurrentBlock().addTransaction(
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

        System.out.println("IS BLOCKCHAIN VALID: " + voidchain.isChainValid());*/
    }
}
