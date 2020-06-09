package pt.ipleiria.estg.dei.pi.voidchain.client;

import bftsmart.tom.ServiceProxy;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Hashtable;
import java.util.Map;

public class Client {
    private JButton getCurrentBlockButton;
    private JButton getCurrentBlockHashButton;
    private JButton getCurrentBlockHeightButton;
    private JButton getTransactionsButton;
    private JButton addTransactionButton;
    private JButton createBlockButton;
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JPanel transactionPanel;
    private JButton buttonQuit;
    private JTextArea transactionDataTextArea;

    private final ServiceProxy serviceProxy;

    public Client(int id) {
        this.serviceProxy = new ServiceProxy(id);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: pt.ipleiria.estg.dei.pi.voidchain.client.Client <client id>");
        }

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        int clientId = Integer.parseInt(args[0]);
        Client client = new Client(clientId);

        client.getCurrentBlockButton.addActionListener(client.getCurrentBlockButtonActionListener());
        client.getCurrentBlockHashButton.addActionListener(client.getCurrentBlockHashButtonActionListener());
        client.getCurrentBlockHeightButton.addActionListener(client.getCurrentBlockHeightButtonActionListener());
        client.getTransactionsButton.addActionListener(client.getTransactionsButtonActionListener());
        client.addTransactionButton.addActionListener(client.addTransactionButtonActionListener());
        client.createBlockButton.addActionListener(client.createBlockButtonActionListener());

        client.buttonQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JFrame frame = new JFrame("Client");
        frame.setPreferredSize(new Dimension(600, 300));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(client.mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public ActionListener getCurrentBlockButtonActionListener() {
        return e -> {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(1);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0) {
                    System.err.println("ERROR");
                    return;
                }

                Block currentBlock;

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    currentBlock = (Block) objIn.readObject();
                }

                System.out.println(currentBlock.toString());
                JOptionPane.showMessageDialog(null, currentBlock.toString(),
                        "Current Block Data", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        };
    }

    public ActionListener getCurrentBlockHashButtonActionListener() {
        return e -> {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(2);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0) {
                    System.err.println("ERROR");
                    return;
                }

                System.out.println("Block hash: " + Base64.toBase64String(reply));
                JOptionPane.showMessageDialog(null,
                        "Block hash: " + Base64.toBase64String(reply), "Current Block Hash",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
    }

    public ActionListener getCurrentBlockHeightButtonActionListener() {
        return e -> {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(3);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0) {
                    System.err.println("ERROR");
                    return;
                }

                int blockHeight;

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    blockHeight = objIn.readInt();
                }

                System.out.println("Block height: " + blockHeight);
                JOptionPane.showMessageDialog(null, "Block height: " + blockHeight,
                        "Current Block Height", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
    }

    public ActionListener getTransactionsButtonActionListener() {
        return e -> {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(4);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0) {
                    System.err.println("ERROR");
                    return;
                }

                Map<String, Transaction> transactions;

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    transactions = (Hashtable<String, Transaction>) objIn.readObject();
                }

                System.out.println(transactions.values().toString());
                JOptionPane.showMessageDialog(null, transactions.values().toString(),
                        "Current Block Transactions", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        };
    }

    public ActionListener addTransactionButtonActionListener() {
        return e -> {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                byte[] data = transactionDataTextArea.getText().getBytes(StandardCharsets.UTF_8);

                System.out.println("Data bytes: " + Base64.toBase64String(data));

                ClientMessage req = new ClientMessage(5, data);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0) {
                    System.err.println("ERROR");
                    return;
                }

                boolean added;
                String error = null;

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    added = objIn.readBoolean();
                    if (!added) {
                        error = objIn.readUTF();
                    }
                }

                String message;
                if (added) {
                    message = "Transaction added";
                } else {
                    message = "Transaction not added due to: " + error;
                }

                System.out.println(message);
                JOptionPane.showMessageDialog(null, message,
                        "Transaction added", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
    }

    public ActionListener createBlockButtonActionListener() {
        return e -> {
            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                ClientMessage req = new ClientMessage(6);
                objOut.writeObject(req);

                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0) {
                    System.err.println("ERROR");
                    return;
                }

                boolean added;

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    added = objIn.readBoolean();
                }

                System.out.println("Block created: " + added);
                JOptionPane.showMessageDialog(null, "Block created: " + added,
                        "Block Created", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
    }
}
