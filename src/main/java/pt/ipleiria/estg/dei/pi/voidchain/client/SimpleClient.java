package pt.ipleiria.estg.dei.pi.voidchain.client;

import bftsmart.tom.ServiceProxy;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.BlockNoTransactions;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;
import pt.ipleiria.estg.dei.pi.voidchain.util.KeyGenerator;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;

/**
 * The Simple Client class is equivalent to the Lightweight Client described in the VOIDChain report/documentation.
 * Contains a Wallet, to store all of its transactions.
 * It does not contain a full copy of the Blockchain.
 * To keep track of its transactions, it only stores the block headers. Using the merkle root, which is part of the block header, the lightweight client can check if a transaction is included in a block.
 */
public class SimpleClient {
    private JButton getCurrentBlockButton;
    private JButton getCurrentBlockHeightButton;
    private JButton getBlockButton;
    private JButton addTransactionButton;
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JPanel transactionPanel;
    private JButton buttonQuit;
    private JTextArea transactionDataTextArea;
    private JPanel getBlockPanel;
    private JTextField blockHeightTextField;
    private JButton isChainValidButton;

    private final ServiceProxy serviceProxy;
    private final Wallet wallet;

    private static final Logger logger = LoggerFactory.getLogger(SimpleClient.class);

    /**
     * Instantiates a new SimpleClient.
     *
     * @param id the id of the client
     */
    public SimpleClient(int id, byte[] password) {
        this.serviceProxy = new ServiceProxy(id);
        this.wallet = Wallet.getInstance(this.serviceProxy.getViewManager().getStaticConf(), password);
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException the io exception
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: voidchain-simple-client <client id> <wallet password>");
            return;
        }

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        Storage.createDefaultConfigFiles();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        int clientId = Integer.parseInt(args[0]);
        String password = args[1];

        if (password.length() < 8) {
            System.out.println("Password length should be greater than 8 characters");
            return;
        }

        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        KeyGenerator.generatePubAndPrivKeys(clientId);
        KeyGenerator.generateSSLKey(clientId);

        try {
            SimpleClient client = new SimpleClient(clientId, passwordBytes);

            client.getCurrentBlockButton.addActionListener(client.getCurrentBlockButtonActionListener());
            client.getCurrentBlockHeightButton.addActionListener(client.getCurrentBlockHeightButtonActionListener());
            client.getBlockButton.addActionListener(client.getBlockButtonActionListener());
            client.addTransactionButton.addActionListener(client.addTransactionButtonActionListener());
            client.isChainValidButton.addActionListener(client.isChainValidButtonActionListener());

            client.buttonQuit.addActionListener(e -> System.exit(0));

            Runtime.getRuntime().addShutdownHook(new Thread(client::close));

            JFrame frame = new JFrame("Simple Client");
            frame.setPreferredSize(new Dimension(600, 300));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(client.mainPanel);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private ActionListener getCurrentBlockButtonActionListener() {
        return e -> {
            logger.debug("Sending GET_MOST_RECENT_BLOCK request to network");
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(ClientMessageType.GET_MOST_RECENT_BLOCK);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());

                if (reply.length == 0) {
                    logger.error("Empty reply from replicas");
                    return;
                }

                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                BlockNoTransactions currentBlock = (BlockNoTransactions) objIn.readObject();

                objIn.close();
                byteIn.close();

                logger.debug("Current block: " + currentBlock);
                JOptionPane.showMessageDialog(null, currentBlock.toString(),
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener getCurrentBlockHeightButtonActionListener() {
        return e -> {
            logger.debug("Sending GET_MOST_RECENT_BLOCK_HEIGHT request to network");
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(ClientMessageType.GET_MOST_RECENT_BLOCK_HEIGHT);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());

                if (reply.length == 0) {
                    logger.error("Empty reply from replicas");
                    return;
                }

                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                int blockHeight = objIn.readInt();

                objIn.close();
                byteIn.close();

                logger.debug("Block height: " + blockHeight);
                JOptionPane.showMessageDialog(null, "Block height: " + blockHeight,
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener getBlockButtonActionListener() {
        return e -> {
            logger.debug("Sending GET_BLOCK ("+ this.blockHeightTextField.getText() + ") request to network");
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                String blockHeightText = this.blockHeightTextField.getText();

                if (blockHeightText.isBlank() || blockHeightText.isEmpty())
                    return;

                int blockHeight = Integer.parseInt(blockHeightText);

                ClientMessage req = new ClientMessage(ClientMessageType.GET_BLOCK,
                        Converters.intToByteArray(blockHeight));

                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());

                if (reply.length == 0) {
                    logger.error("Empty reply from replicas");
                    return;
                }

                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                BlockNoTransactions block = (BlockNoTransactions) objIn.readObject();

                objIn.close();
                byteIn.close();

                logger.debug("Block " + blockHeight + " Data: " + block);
                JOptionPane.showMessageDialog(null, block.toString(),
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener addTransactionButtonActionListener() {
        return e -> {
            logger.debug("Sending ADD_TRANSACTION request to network");
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                Configuration config = Configuration.getInstance();

                Transaction t;
                try {
                    t = new Transaction(transactionDataTextArea.getText().getBytes(StandardCharsets.UTF_8),
                            config.getProtocolVersion(), Instant.now().toEpochMilli(),
                            this.serviceProxy.getViewManager().getStaticConf());
                    logger.debug("Transaction Hash: " + Base64.toBase64String(t.getHash()));
                    logger.debug("Transaction Signature: " + Base64.toBase64String(t.getSignature()));
                } catch (IllegalArgumentException | SignatureException | NoSuchAlgorithmException |
                        InvalidKeyException exception) {
                    logger.error("Unable to create transaction", exception);
                    JOptionPane.showMessageDialog(null, "Unable to create transaction check logs",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                this.wallet.addTransaction(t);

                ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
                ObjectOutput objOut2 = new ObjectOutputStream(byteOut2);

                objOut2.writeObject(t);
                objOut2.flush();
                byteOut2.flush();

                byte[] tBytes = byteOut2.toByteArray();

                objOut2.close();
                byteOut2.close();

                ClientMessage req = new ClientMessage(ClientMessageType.ADD_TRANSACTION, tBytes);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0) {
                    logger.error("Empty reply from replicas");
                    return;
                }

                boolean added;

                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                added = objIn.readBoolean();

                objIn.close();
                byteIn.close();

                String message;
                if (added)
                    message = "Transaction added";
                else
                    message = "Transaction not added";

                logger.debug(message);
                JOptionPane.showMessageDialog(null, message,
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener isChainValidButtonActionListener() {
        return e -> {
            logger.debug("Sending IS_CHAIN_VALID request to network");
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(ClientMessageType.IS_CHAIN_VALID);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0) {
                    logger.error("Empty reply from replicas");
                    return;
                }

                ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                ObjectInput objIn = new ObjectInputStream(byteIn);

                boolean isValid = objIn.readBoolean();

                objIn.close();
                byteIn.close();

                logger.debug("Is chain valid: " + isValid);
                JOptionPane.showMessageDialog(null, "Is chain valid: " + isValid,
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private void close() {
        this.serviceProxy.close();
    }
}
