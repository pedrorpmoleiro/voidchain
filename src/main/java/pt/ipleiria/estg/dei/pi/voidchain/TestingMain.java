package pt.ipleiria.estg.dei.pi.voidchain;

import org.bouncycastle.util.encoders.Base64;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.MerkleTree;

import javax.print.DocFlavor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TestingMain {
    public static void main(String[] args) {
        Blockchain voidchain = Blockchain.getInstance();

        String protocolVersion = Configuration.getInstance().getProtocolVersion();
        final int BLOCK_COUNT = 5;
        final int TRANSACTION_COUNT = Configuration.getInstance().getNumTransactionsInBlock();
        final int TRANSACTION_DATA_SIZE = Configuration.getInstance().getTransactionMaxSize() - 24;

        long timestamp = 1L;
        final byte[] nonce = new byte[10];
        final byte[] transactionData = new byte[TRANSACTION_DATA_SIZE];

        for (int b = 0; b < BLOCK_COUNT; b++) {
            Block block = voidchain.createBlock(timestamp, nonce);
            timestamp += 1L;
            for (int t = 0; t < TRANSACTION_COUNT; t++) {
                Transaction transaction = new Transaction(transactionData, protocolVersion, timestamp);
                // int size = transaction.getSize();
                block.addTransaction(transaction);
                timestamp += 1L;
            }
            if (block.getTransactionCounter() != TRANSACTION_COUNT) {
                System.out.println("DIDN'T ADD ALL TRANSACTIONS");
                break;
            }
            block.toDisk();
        }

        for (int b = 0; b < BLOCK_COUNT + 1; b++) {
            Block block = Block.fromDisk(b);
            System.out.println(block.toString());
        }
    }
}
