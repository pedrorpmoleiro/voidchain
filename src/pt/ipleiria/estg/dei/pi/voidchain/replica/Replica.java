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
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessage;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;

import java.io.*;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/*
    TODO: READ BELOW
    ? BATCH EXECUTABLE 🛑
    API
    BLOCKS IN DISK SYNC
    AUTOMATION OF MANAGEMENT OF THE BLOCKCHAIN:
        I.   COMMUNICATION BETWEEN REPLICAS (MAKE OWN SystemMessage) ❌
        II.  CREATE BLOCKS VIA TRANSACTION POOL ✅
        III. TRANSACTION POOL ON REPLICA (MOVE FROM BLOCKCHAIN (?)/CHANGE GET & INSTALL SNAPSHOT) ✅
        IV.  VALIDATE BLOCKS
*/
public class Replica extends DefaultSingleRecoverable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private Blockchain blockchain;
    private final List<Transaction> transactionPool;
    private final ReplicaMessenger messenger;

    private Block proposedBlock = null;

    public Replica(int id) {
        this.blockchain = Blockchain.getInstance();
        this.transactionPool = new ArrayList<>();
        this.messenger = new ReplicaMessenger(id);

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
    private void createProposedBlock() {

    }

    private void processNewBlock() {
        Configuration config = Configuration.getInstance();

        int transactionsPerBlock = config.getNumTransactionsInBlock();

        if (this.transactionPool.size() < transactionsPerBlock) {
            return;
        }

        List<Transaction> transactions = new ArrayList<>();
        long timestamp = -1L;

        while (transactions.size() < transactionsPerBlock) {
            Transaction t = this.transactionPool.get(0);
            transactions.add(t);
            this.transactionPool.remove(0);

            if (t.getTimestamp() > timestamp)
                timestamp = t.getTimestamp();
        }

        Block previousBlock = this.blockchain.getCurrentBlock();
        byte[] nonces = new byte[10];
        new Random(timestamp).nextBytes(nonces);

        try {
            this.proposedBlock = new Block(previousBlock.getPreviousBlockHash(), config.getProtocolVersion(),
                    previousBlock.getBlockHeight() + 1, transactions, timestamp, nonces);
        } catch (InstantiationException e) {
            this.logger.error("Error creating new block instance", e);
            this.transactionPool.addAll(transactions);
            return;
        }

        if (this.messenger.proposeBlock(this.proposedBlock)) {
            this.blockchain.addBlock(this.proposedBlock);
        } else {
            this.transactionPool.addAll(transactions);
        }

        this.proposedBlock = null;
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

    // TODO BLOCK SYNC
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
    }

    // TODO BLOCK SYNC
    @Override
    public byte[] getSnapshot() {
        byte[] snapshot = new byte[0];

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

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

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        return execute(command, msgCtx, false);
    }

    private byte[] execute(byte[] command, MessageContext msgCtx, boolean ordered) {
        byte[] reply = null;
        boolean hasReply = false;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            Object input = objIn.readObject();

            if (input.getClass() == ClientMessage.class) {
                Block currentBlock = this.blockchain.getCurrentBlock();

                ClientMessage req = (ClientMessage) input;

                switch (req.getType()) {
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
                            if (req.hasData()) {
                                Transaction t = null;
                                try {
                                    t = new Transaction(req.getData(), currentBlock.getProtocolVersion(),
                                            msgCtx.getTimestamp());
                                } catch (IllegalArgumentException e) {
                                    logger.error(e.getMessage(), e);
                                    objOut.writeBoolean(false);
                                    objOut.writeUTF(e.getMessage());
                                }

                                //currentBlock.addTransaction(t);
                                //objOut.writeBoolean(true);
                                objOut.writeBoolean(addTransaction(t));
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
                        this.logger.error("Unknown type of ClientMessageType");
                }
            } else if (input.getClass() == ReplicaMessage.class) {
                ReplicaMessage req = (ReplicaMessage) input;

                switch (req.getType()) {
                    case SYNC_BLOCKS:
                        // TODO
                        break;
                    case NEW_BLOCK:

                        Block proposedBlock;
                        try (ByteArrayInputStream byteIn2 = new ByteArrayInputStream(req.getContent());
                             ObjectInput objIn2 = new ObjectInputStream(byteIn2)) {
                            proposedBlock = (Block) objIn2.readObject();
                        }

                        if (this.proposedBlock == null) {

                        }

                        break;
                    default:
                        this.logger.error("Unknown type of ReplicaMessageType");
                }
            } else
                this.logger.error("Unknown message class, ignoring");

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
    public void Op(int CID, byte[] requests, MessageContext msgCtx) {
        // TODO: ANALYZE
        int test = 0;
    }
}
