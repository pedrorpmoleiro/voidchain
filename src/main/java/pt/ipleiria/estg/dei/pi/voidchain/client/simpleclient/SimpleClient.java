package pt.ipleiria.estg.dei.pi.voidchain.client.simpleclient;

import bftsmart.tom.ServiceProxy;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.BlockNoTransactions;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessage;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessageType;
import pt.ipleiria.estg.dei.pi.voidchain.sync.BlockSyncClient;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;
import pt.ipleiria.estg.dei.pi.voidchain.util.SignatureKeyGenerator;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;

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
    private JButton getLeaderButton;

    private final ServiceProxy serviceProxy;

    private static final Logger logger = LoggerFactory.getLogger(SimpleClient.class);

    /**
     * Instantiates a new SimpleClient.
     *
     * @param id the id of the client
     */
    public SimpleClient(int id) {
        this.serviceProxy = new ServiceProxy(id);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            System.out.println("Usage: voidchain-simpleclient <client id>");

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        Storage.createDefaultConfigFiles();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        int clientId = Integer.parseInt(args[0]);

        SignatureKeyGenerator.generatePubAndPrivKeys(clientId);
        SignatureKeyGenerator.generateSSLKey(clientId);

        SimpleClient client = new SimpleClient(clientId);

        client.getCurrentBlockButton.addActionListener(client.getCurrentBlockButtonActionListener());
        client.getCurrentBlockHeightButton.addActionListener(client.getCurrentBlockHeightButtonActionListener());
        client.getBlockButton.addActionListener(client.getBlockButtonActionListener());
        client.addTransactionButton.addActionListener(client.addTransactionButtonActionListener());
        client.isChainValidButton.addActionListener(client.isChainValidButtonActionListener());
        client.getLeaderButton.addActionListener(client.getLeaderButtonActionListener());

        client.buttonQuit.addActionListener(e -> System.exit(0));

        JFrame frame = new JFrame("Simple Client");
        frame.setPreferredSize(new Dimension(600, 300));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(client.mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private ActionListener getCurrentBlockButtonActionListener() {
        return e -> {
            logger.info("Sending GET_MOST_RECENT_BLOCK request to network");
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

                //System.out.println(currentBlock.toString());
                logger.info("Current block: " + currentBlock);
                JOptionPane.showMessageDialog(null, currentBlock.toString(),
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener getCurrentBlockHeightButtonActionListener() {
        return e -> {
            logger.info("Sending GET_MOST_RECENT_BLOCK_HEIGHT request to network");
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

                logger.info("Block height: " + blockHeight);
                JOptionPane.showMessageDialog(null, "Block height: " + blockHeight,
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener getBlockButtonActionListener() {
        return e -> {
            logger.info("Sending GET_BLOCK ("+ this.blockHeightTextField.getText() + ") request to network");
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

                //System.out.println(block.toString());
                logger.info("Block " + blockHeight + " Data: " + block);
                JOptionPane.showMessageDialog(null, block.toString(),
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener addTransactionButtonActionListener() {
        return e -> {
            logger.info("Sending ADD_TRANSACTION request to network");
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

                logger.info(message);
                JOptionPane.showMessageDialog(null, message,
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener isChainValidButtonActionListener() {
        return e -> {
            logger.info("Sending IS_CHAIN_VALID request to network");
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

                logger.info("Is chain valid: " + isValid);
                JOptionPane.showMessageDialog(null, "Is chain valid: " + isValid,
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener getLeaderButtonActionListener() {
        return e -> {
            logger.info("Sending GET_LEADER request to network");
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(ClientMessageType.GET_LEADER);
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

                int leaderID = objIn.readInt();

                objIn.close();
                byteIn.close();

                logger.info("Leader Replica ID: " + leaderID);
                JOptionPane.showMessageDialog(null, "Leader Replica ID: " + leaderID,
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }
}
