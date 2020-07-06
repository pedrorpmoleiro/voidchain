package pt.ipleiria.estg.dei.pi.voidchain;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.SignatureKeyGenerator;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestingMain {
    public static void main(String[] args) throws Exception {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        SignatureKeyGenerator.generatePubAndPrivKeys(100);

        Blockchain voidchain = Blockchain.getInstance();

        String protocolVersion = Configuration.getInstance().getProtocolVersion();
        final int BLOCK_COUNT = 5;
        final int TRANSACTION_COUNT = Configuration.getInstance().getNumTransactionsInBlock();
        final int TRANSACTION_DATA_SIZE = Configuration.getInstance().getTransactionMaxSize() - 16;

        long timestamp = 1L;
        final byte[] nonce = new byte[10];
        final byte[] transactionData = new byte[TRANSACTION_DATA_SIZE];

        Random random = new Random(System.currentTimeMillis());

        for (int b = 0; b < BLOCK_COUNT; b++) {
            random.nextBytes(nonce);
            List<Transaction> transactionList = new ArrayList<>();
            for (int t = 0; t < TRANSACTION_COUNT; t++) {
                random.nextBytes(transactionData);
                Transaction transaction = new Transaction(transactionData, protocolVersion, timestamp);
                transactionList.add(transaction);
                timestamp += 1L;
            }
            Block previousBlock = voidchain.getMostRecentBlock();
            Block newBlock = new Block(previousBlock.getHash(), protocolVersion, previousBlock.getBlockHeight() + 1,
                    transactionList, timestamp, nonce);
            if (newBlock.getTransactionCounter() != TRANSACTION_COUNT) {
                System.out.println("DIDN'T ADD ALL TRANSACTIONS");
                break;
            }

            voidchain.addBlock(newBlock);
        }

        System.out.println("Chain valid: " + voidchain.isChainValid());
    }
}
