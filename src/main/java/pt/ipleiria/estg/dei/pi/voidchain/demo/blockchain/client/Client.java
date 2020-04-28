package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain.client;

import bftsmart.tom.ServiceProxy;
import org.bouncycastle.util.encoders.Base64;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain.Request;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;

public class Client {
    private JButton getCurrentBlockButton;
    private JButton getCurrentBlockHashButton;
    private JButton getCurrentBlockHeightButton;
    private JButton getTransactionsButton;
    private JButton addTransactionButton;
    private JButton createBlockButton;
    private JPanel Panel;

    private ServiceProxy serviceProxy;

    public Client(int clientId) {
        this.serviceProxy = new ServiceProxy(clientId);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain.client.Client <client id>");
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

        JFrame frame = new JFrame("Client");
        frame.setContentPane(client.Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public ActionListener getCurrentBlockButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                    Request req = new Request(1);
                    objOut.writeObject(req);

                    objOut.flush();
                    byteOut.flush();

                    byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                    if (reply.length == 0) {
                        System.err.println("ERROR");
                        return;
                    }

                    Block currentBlock = null;

                    try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                         ObjectInput objIn = new ObjectInputStream(byteIn)) {
                        currentBlock = (Block) objIn.readObject();
                    }

                    System.out.println("Block: " + currentBlock.toString());

                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public ActionListener getCurrentBlockHashButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                    Request req = new Request(2);
                    objOut.writeObject(req);

                    objOut.flush();
                    byteOut.flush();

                    byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                    if (reply.length == 0) {
                        System.err.println("ERROR");
                        return;
                    }

                    byte[] blockHash = reply;

                    System.out.println("Block hash: " + Base64.toBase64String(blockHash));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public ActionListener getCurrentBlockHeightButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                    Request req = new Request(3);
                    objOut.writeObject(req);

                    objOut.flush();
                    byteOut.flush();

                    byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                    if (reply.length == 0) {
                        System.err.println("ERROR");
                        return;
                    }

                    int blockHeight = -1;

                    try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                         ObjectInput objIn = new ObjectInputStream(byteIn)) {
                        blockHeight = objIn.readInt();
                    }

                    System.out.println("Block height: " + blockHeight);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public ActionListener getTransactionsButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                    Request req = new Request(4);
                    objOut.writeObject(req);

                    objOut.flush();
                    byteOut.flush();

                    byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                    if (reply.length == 0) {
                        System.err.println("ERROR");
                        return;
                    }

                    Map<String, Transaction> transactions = null;

                    try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                         ObjectInput objIn = new ObjectInputStream(byteIn)) {
                        transactions = (Hashtable<String, Transaction>) objIn.readObject();
                    }

                    System.out.println("Transactions: " + transactions.values().toString());

                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public ActionListener addTransactionButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                    String string = "Hello World";
                    byte[] data = string.getBytes(StandardCharsets.UTF_8);

                    Request req = new Request(5, data);
                    objOut.writeObject(req);

                    objOut.flush();
                    byteOut.flush();

                    byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                    if (reply.length == 0) {
                        System.err.println("ERROR");
                        return;
                    }

                    boolean added = false;

                    try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                         ObjectInput objIn = new ObjectInputStream(byteIn)) {
                        added = objIn.readBoolean();
                    }

                    System.out.println("Transaction added: " + added);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public ActionListener createBlockButtonActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

                    Request req = new Request(6);
                    objOut.writeObject(req);

                    objOut.flush();
                    byteOut.flush();

                    byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                    if (reply.length == 0) {
                        System.err.println("ERROR");
                        return;
                    }

                    boolean added = false;

                    try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                         ObjectInput objIn = new ObjectInputStream(byteIn)) {
                        added = objIn.readBoolean();
                    }

                    System.out.println("Block created: " + added);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }
}
