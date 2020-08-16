package pt.ipleiria.estg.dei.pi.voidchain;

import bftsmart.tom.ServiceProxy;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessage;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessageType;
import pt.ipleiria.estg.dei.pi.voidchain.client.Wallet;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.KeyGenerator;

import java.io.*;
import java.security.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestingMain {
    public static void main(String[] args) {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        int id = Integer.parseInt(args[0]);
        KeyGenerator.generatePubAndPrivKeys(id);
        ServiceProxy serviceProxy = new ServiceProxy(id);

        Wallet wallet = new Wallet(serviceProxy.getViewManager().getStaticConf(), "&V2%v3TWsPBCnpAo");
        //Wallet wallet = new Wallet(serviceProxy.getViewManager().getStaticConf(), "&V2%v3TWsPBCnpAo");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        Random random = new Random(Instant.now().toEpochMilli());
        List<Transaction> transactions = new ArrayList<>();
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

        for (int i = 0; i < 700; i++) {
            byte[] data = new byte[config.getTransactionMaxSize() - 25];
            random.nextBytes(data);
            Transaction t;
            try {
                t = new Transaction(data, config.getProtocolVersion(), Instant.now().toEpochMilli(),
                        serviceProxy.getViewManager().getStaticConf());
            } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
                return;
            }
            transactions.add(t);
        }

        System.out.println("Finished creating transactions");

        /*for (Transaction t : transactions) {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                objOut.writeObject(t);
                objOut.flush();
                byteOut.flush();
                byte[] tBytes = byteOut.toByteArray();

                ClientMessage cm = new ClientMessage(ClientMessageType.ADD_TRANSACTION, tBytes);

                ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
                ObjectOutput objOut2 = new ObjectOutputStream(byteOut2);

                objOut2.writeObject(cm);
                objOut2.flush();
                byteOut2.flush();

                System.out.println("Sending transaction");
                byte[] reply = serviceProxy.invokeOrdered(byteOut2.toByteArray());

                if (reply.length == 0) {
                    System.out.println("Empty reply from replicas");
                    continue;
                }

                boolean added;

                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                added = objIn.readBoolean();

                objIn.close();
                byteIn.close();

                System.out.println("Transaction added: " + added);

                if (added)
                    wallet.addTransaction(t);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(transactions);
            objOut.flush();
            byteOut.flush();
            byte[] tBytes = byteOut.toByteArray();

            ClientMessage cm = new ClientMessage(ClientMessageType.ADD_TRANSACTIONS, tBytes);

            ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
            ObjectOutput objOut2 = new ObjectOutputStream(byteOut2);

            objOut2.writeObject(cm);
            objOut2.flush();
            byteOut2.flush();

            System.out.println("Sending all transactions");
            byte[] reply = serviceProxy.invokeOrdered(byteOut2.toByteArray());

            if (reply.length == 0) {
                System.out.println("Empty reply from replicas");
                return;
            }

            boolean added;

            ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
            ObjectInput objIn = new ObjectInputStream(byteIn);

            added = objIn.readBoolean();

            objIn.close();
            byteIn.close();

            System.out.println("Transactions added: " + added);

            if (added)
                wallet.addTransactions(transactions);

        } catch (IOException ioException) {
            ioException.printStackTrace();
            return;
        }

        System.out.println("##### DONE #####");

        System.exit(0);
    }
}
