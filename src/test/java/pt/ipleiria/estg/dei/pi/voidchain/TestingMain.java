package pt.ipleiria.estg.dei.pi.voidchain;

import bftsmart.tom.ServiceProxy;

import bitcoinj.Base58;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessage;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessageType;
import pt.ipleiria.estg.dei.pi.voidchain.client.Wallet;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Keys;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.Random;

public class TestingMain {
    private static final Logger logger = LoggerFactory.getLogger(TestingMain.class);

    public static void main(String[] args) {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        int id = Integer.parseInt(args[0]);
        Keys.generatePubAndPrivKeys(id);
        ServiceProxy serviceProxy = new ServiceProxy(id);

        // logger.info("# Pub Key in Base58: " + Base58.encode(serviceProxy.getViewManager().getStaticConf().getPublicKey().getEncoded()));

        Wallet wallet = Wallet.getInstance(serviceProxy.getViewManager().getStaticConf(), "&V2%v3TWsPBCnpAo".getBytes(StandardCharsets.UTF_8));
        //Wallet wallet = new Wallet(serviceProxy.getViewManager().getStaticConf(), "&V2%v3TWsPBCnpA".getBytes(StandardCharsets.UTF_8));

        // logger.info("Sleeping for 5 seconds for ServiceProxy to init");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        Random random = new Random(Instant.now().toEpochMilli());
        Configuration config = Configuration.getInstance();
        Signature signature;
        try {
            signature = Signature.getInstance(serviceProxy.getViewManager().getStaticConf().
                    getSignatureAlgorithm(), serviceProxy.getViewManager().getStaticConf().getSignatureAlgorithmProvider());
            signature.initSign(serviceProxy.getViewManager().getStaticConf().getPrivateKey());
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            e.printStackTrace();
            return;
        }

        //List<Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {
            byte[] data = new byte[config.getTransactionMaxSize() - 100];
            random.nextBytes(data);
            Transaction t;
            // logger.info("Creating Transaction " + i);
            try {
                t = new Transaction(data, config.getProtocolVersion(), Instant.now().toEpochMilli(),
                        serviceProxy.getViewManager().getStaticConf());
            } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
                return;
            }
            logger.info(" " + i);
            //transactions.add(t);
            // Send Transaction
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                objOut.writeObject(t);
                objOut.flush();
                byteOut.flush();
                byte[] tBytes = byteOut.toByteArray();

                // logger.info("Creating Message to send");
                ClientMessage cm = new ClientMessage(ClientMessageType.ADD_TRANSACTION, tBytes);

                ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
                ObjectOutput objOut2 = new ObjectOutputStream(byteOut2);

                objOut2.writeObject(cm);
                objOut2.flush();
                byteOut2.flush();

                // logger.info("Sending transaction - " + i);
                byte[] reply = serviceProxy.invokeOrdered(byteOut2.toByteArray());

                if (reply == null || reply.length == 0) {
                    System.out.println("Empty reply from replicas");
                    continue;
                }

                // logger.info("Got reply from nodes");

                boolean added;

                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                added = objIn.readBoolean();

                objIn.close();
                byteIn.close();

                System.out.println("Transaction " + i + " added: " + added);

                if (added) {
                    // logger.info("Adding transaction " + i + " to wallet");
                    wallet.addTransaction(t);
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }

        //transactions.sort(Transaction.LIST_COMPARATOR);
        //Collections.reverse(transactions);

        logger.info("##### DONE #####");

        // logger.info("# Pub Key in Base58: " + Base58.encode(serviceProxy.getViewManager().getStaticConf().getPublicKey().getEncoded()));

        serviceProxy.close();
        System.exit(0);
    }
}
