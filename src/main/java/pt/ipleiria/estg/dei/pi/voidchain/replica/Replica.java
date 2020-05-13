package pt.ipleiria.estg.dei.pi.voidchain.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.client.Request;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;

import java.io.*;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    TODO: READ BELOW
    BATCH EXECUTABLE ?
    API
    AUTOMATION OF MANAGEMENT OF THE BLOCKCHAIN:
        I.   COMMUNICATION BETWEEN REPLICAS (MAKE OWN SystemMessage)
        II.  CREATE BLOCKS VIA TRANSACTION POOL
        III. TRANSACTION POOL ON REPLICA (MOVE FROM BLOCKCHAIN (?)/CHANGE GET & INSTALL SNAPSHOT)
        IV.  VALIDATE BLOCKS
*/
public class Replica extends DefaultSingleRecoverable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private Blockchain blockchain;
    private final List<Transaction> transactionPool;

    public Replica(int id) {
        this.blockchain = new Blockchain();
        this.transactionPool = new ArrayList<>();

        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: pt.ipleiria.estg.dei.pi.voidchain.replica.Replica <server id>");
            System.exit(-1);
        }

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        new Replica(Integer.parseInt(args[0]));
    }

    // MEMORY POOL
    private void processNewBlock() {
        int transactionsPerBlock = Configuration.getInstance().getNumTransactionsInBlock();

        if (this.transactionPool.size() < transactionsPerBlock) {
            return;
        }

        List<Transaction> transactions = new ArrayList<>();

        while (transactions.size() < transactionsPerBlock) {
            transactions.add(this.transactionPool.get(0));
            this.transactionPool.remove(0);
        }

        /*
            TODO: TIMESTAMP & NONCE (?) & READ BELOW
            REPLICAS SHOULD COMMUNICATE TO ADD NEW BLOCK
            TIMESTAMP AND NONCE WOULD COME FROM MSGCTX
        */

        this.blockchain.createBlock(0, new byte[0], transactions);
    }

    public boolean addTransaction(Transaction transaction) {
        int aux = this.transactionPool.size();
        this.transactionPool.add(transaction);

        if (aux == this.transactionPool.size() || (aux + 1) != this.transactionPool.size()) {
            return false;
        }

        processNewBlock();
        return true;
    }

    public boolean addTransactions(List<Transaction> transactions) {
        int aux = this.transactionPool.size();
        this.transactionPool.addAll(transactions);

        if (aux == this.transactionPool.size() || (aux + transactions.size()) != this.transactionPool.size()) {
            return false;
        }

        processNewBlock();
        return true;
    }

    // END MEMORY POOL

    @Override
    public void installSnapshot(byte[] state) {
        if (Arrays.equals(state, new byte[0])) {
            return;
        }

        Blockchain aux = this.blockchain;
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {

            this.blockchain = (Blockchain) objIn.readObject();

        } catch (IOException | ClassNotFoundException e) {
            this.logger.error("Error installing snapshot", e);
            this.blockchain = aux;
        }

        /*this.replicaContext.getServerCommunicationSystem().send(
                this.replicaContext.getCurrentView().getProcesses(),
                new TOMMessage(
                        this.replicaContext.getCurrentView().getId(),
                        this.replicaContext.getCurrentView().
                )
        );*/
    }

    @Override
    public byte[] getSnapshot() {
        byte[] snapshot = new byte[0];

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(  byteOut)) {

            objOut.writeObject(this.blockchain);
            objOut.flush();
            byteOut.flush();
            snapshot = byteOut.toByteArray();

        } catch (IOException e) {
            this.logger.error("Error getting snapshot", e);
        }

        return snapshot;
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        return execute(command, msgCtx, true);
    }

    private byte[] execute(byte[] command, MessageContext msgCtx, boolean ordered) {
        byte[] reply = null;
        boolean hasReply = false;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            Request input = (Request) objIn.readObject();

            Block currentBlock = this.blockchain.getCurrentBlock();

            switch (input.getReq()) {
                case 1:
                    objOut.writeObject(currentBlock);
                    hasReply = true;
                    break;
                case 2:
                    objOut.write(currentBlock.getHash());
                    hasReply = true;
                    break;
                case 3:
                    objOut.writeInt(currentBlock.getBlockHeight());
                    hasReply = true;
                    break;
                case 4:
                    objOut.writeObject(currentBlock.getTransactions());
                    hasReply = true;
                    break;
                case 5:
                    if (ordered) {
                        if (input.hasData()) {
                            Transaction t = null;
                            try {
                                t = new Transaction(input.getData(), currentBlock.getProtocolVersion(),
                                        msgCtx.getTimestamp());
                            } catch (IllegalArgumentException e) {
                                logger.error(e.getMessage(), e);
                                objOut.writeBoolean(false);
                                objOut.writeUTF(e.getMessage());
                            }

                            currentBlock.addTransaction(t);
                            objOut.writeBoolean(true);
                        } else {
                            objOut.writeBoolean(false);
                            objOut.writeUTF("Transaction cannot be created without any data");
                        }

                        hasReply = true;
                        break;
                    }
                case 6:
                    if (ordered) {
                        this.blockchain.createBlock(msgCtx.getTimestamp(), msgCtx.getNonces());
                        objOut.writeBoolean(true);
                        hasReply = true;
                    }
                    break;
                default:
                    this.logger.error("Error of request");
            }

            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            }

        } catch (IOException | ClassNotFoundException e) {
            this.logger.error("ERROR", e);
        }

        return reply;
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        return execute(command, msgCtx, false);
    }
}
