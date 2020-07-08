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
import pt.ipleiria.estg.dei.pi.voidchain.util.Hash;
import pt.ipleiria.estg.dei.pi.voidchain.util.SignatureKeyGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.time.Instant;

// TODO: Logging
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
    private JButton syncBlocksButton;

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

    /**
     * The entry point of application.
     */
    public static void main(String[] args) {
        if (args.length < 1)
            System.out.println("Usage: pt.ipleiria.estg.dei.pi.voidchain.client.Client <client id>");

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        int clientId = Integer.parseInt(args[0]);

        SignatureKeyGenerator.generatePubAndPrivKeys(clientId);

        SimpleClient client = new SimpleClient(clientId);

        client.getCurrentBlockButton.addActionListener(client.getCurrentBlockButtonActionListener());
        client.getCurrentBlockHeightButton.addActionListener(client.getCurrentBlockHeightButtonActionListener());
        client.getBlockButton.addActionListener(client.getBlockButtonActionListener());
        client.addTransactionButton.addActionListener(client.addTransactionButtonActionListener());
        client.isChainValidButton.addActionListener(client.isChainValidButtonActionListener());
        client.getLeaderButton.addActionListener(client.getLeaderButtonActionListener());

        client.syncBlocksButton.addActionListener(client.syncBlocksButtonActionListener());

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
                logger.info("Current block: ", currentBlock);
                JOptionPane.showMessageDialog(null, currentBlock.toString(),
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener getCurrentBlockHeightButtonActionListener() {
        return e -> {
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
                logger.info("Block " + blockHeight + " Data", block);
                JOptionPane.showMessageDialog(null, block.toString(),
                        "Response", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException ex) {
                logger.error("An error has occurred", ex);
            }
        };
    }

    private ActionListener addTransactionButtonActionListener() {
        return e -> {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                Configuration config = Configuration.getInstance();

                byte[] key = this.serviceProxy.getViewManager().getStaticConf().getPublicKey().getEncoded();

                if (key == null) {
                    logger.error("Unable to retrieve encoded public key");
                }

                Transaction t;
                try {
                    t = new Transaction(transactionDataTextArea.getText().getBytes(StandardCharsets.UTF_8),
                            config.getProtocolVersion(), Instant.now().toEpochMilli(),
                            Hash.calculateSHA3512RIPEMD160(key));
                    
                } catch (IllegalArgumentException illegalArgumentException) {
                    logger.error("Unable to create transaction", illegalArgumentException);
                    JOptionPane.showMessageDialog(null, "Unable to create transaction check logs",
                            "Transaction creation", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
                ObjectOutput objOut2 = new ObjectOutputStream(byteOut2);

                objOut2.writeObject(t);
                objOut2.flush();
                byteOut.flush();

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

    private ActionListener syncBlocksButtonActionListener() {
        return e -> {
            BlockSyncClient syncClient = new BlockSyncClient(this.serviceProxy);
            syncClient.sync(true);
        };
    }
}
