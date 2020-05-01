package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.Util;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;

import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;

public class TestingMain {
    public static void main(String[] args) {
        Blockchain voidchain = new Blockchain();

        Block block1 = voidchain.createBlock(0L, new byte[0]);
        block1.addTransaction(new Transaction("TRANSACTION 1".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION, 1L));
        block1.addTransaction(new Transaction("TRANSACTION 2".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION, 2L));
        block1.addTransaction(new Transaction("TRANSACTION 3".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION, 3L));
        block1.addTransaction(new Transaction("TRANSACTION 4".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION, 4L));
        System.out.println("Transaction Count: " + block1.getTransactionCounter());
        System.out.println("Merkle Root: " + Base64.toBase64String(Util.getMerkleRoot(block1.getTransactions().keySet())));

        System.out.println("----------------------------------------------------------------------");

        System.out.println("Original 1: " + block1.toString());
        System.out.println("Clone of 1: " + block1.clone().toString());

        System.out.println("----------------------------------------------------------------------");

        block1.addTransaction(new Transaction("TRANSACTION 5".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION, 5L));
        var mapTransactions = block1.getTransactions();
        System.out.println("Transaction Count: " + block1.getTransactionCounter());
        System.out.println("Merkle Root: " + Base64.toBase64String(Util.getMerkleRoot(mapTransactions.keySet())));

        Block block2 = voidchain.createBlock(6L, new byte[0], mapTransactions);
        block2.addTransaction(new Transaction("TRANSACTION 6".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION, 7L));
        System.out.println("Transaction Count: " + block2.getTransactionCounter());
        System.out.println("Merkle Root: " + Base64.toBase64String(Util.getMerkleRoot(block2.getTransactions().keySet())));

        System.out.println("Is chain valid: " + voidchain.isChainValid());
        System.out.println("ALTERING PREVIOUS BLOCK (ADD TRANSACTION)");
        block1.addTransaction(new Transaction("TRANSACTION 7".getBytes(StandardCharsets.UTF_8), Blockchain.PROTOCOL_VERSION, 8L));
        System.out.println("Is chain valid: " + voidchain.isChainValid());
    }
}
