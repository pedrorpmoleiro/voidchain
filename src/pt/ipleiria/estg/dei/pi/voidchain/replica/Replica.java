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
import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;

import java.io.*;
import java.security.Security;
import java.util.*;

/*
    TODO: READ BELOW
    JAVA DOC
    API
    BLOCKS IN DISK SYNC
    VALIDATE BLOCKS
    ? MEMORY POOL SYNC
*/
public class Replica extends DefaultSingleRecoverable {
    private static final Logger logger = LoggerFactory.getLogger(Replica.class.getName());

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

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        new Replica(Integer.parseInt(args[0]));
    }

    private void createProposedBlock() {
        if (this.proposedBlock != null) return;

        Configuration config = Configuration.getInstance();
        int transactionsPerBlock = config.getNumTransactionsInBlock();

        if (this.transactionPool.size() < transactionsPerBlock) return;

        List<Transaction> transactions = new ArrayList<>();

        while (transactions.size() < transactionsPerBlock) {
            Transaction t = this.transactionPool.get(0);
            transactions.add(t);
            this.transactionPool.remove(0);
        }

        Block previousBlock = this.blockchain.getMostRecentBlock();

        try {
            this.proposedBlock = new Block(previousBlock.getHash(), config.getProtocolVersion(),
                    previousBlock.getBlockHeight() + 1, transactions, -1L, new byte[0]);

        } catch (InstantiationException e) {
            logger.error("Error creating new block instance", e);
            this.transactionPool.addAll(transactions);
        }
    }

    private void processNewBlock() {
        createProposedBlock();

        if (this.proposedBlock == null) return;

        if (this.messenger.proposeBlock(this.proposedBlock)) {
            if (this.proposedBlock != null)
                this.blockchain.addBlock(this.proposedBlock);
        } else
            this.transactionPool.addAll(this.proposedBlock.getTransactions().values());

        this.proposedBlock = null;
    }

    public boolean addTransaction(Transaction transaction) {
        int aux = this.transactionPool.size();
        this.transactionPool.add(transaction);

        if (aux == this.transactionPool.size() || (aux + 1) != this.transactionPool.size())
            return false;

        new Thread(this::processNewBlock).start();

        return true;
    }

    public boolean addTransactions(List<Transaction> transactions) {
        int aux = this.transactionPool.size();
        this.transactionPool.addAll(transactions);

        if (aux == this.transactionPool.size() || (aux + transactions.size()) != this.transactionPool.size())
            return false;

        new Thread(this::processNewBlock).start();

        return true;
    }

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
            logger.error("Error installing snapshot", e);
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
            logger.error("Error getting snapshot", e);
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
                Block currentBlock = this.blockchain.getMostRecentBlock();

                ClientMessage req = (ClientMessage) input;

                switch (req.getType()) {
                    case GET_MOST_RECENT_BLOCK:
                        objOut.writeObject(currentBlock);
                        hasReply = true;
                        break;
                    case GET_MOST_RECENT_BLOCK_HEIGHT:
                        objOut.writeInt(currentBlock.getBlockHeight());
                        hasReply = true;
                        break;
                    case GET_BLOCK:
                        objOut.writeObject(this.blockchain.getBlock(Converters.convertByteArrayToInt(req.getData())));
                        hasReply = true;
                        break;
                    case GET_BLOCK_NO_TRANSACTIONS:
                        objOut.writeObject(this.blockchain.getBlock(Converters.convertByteArrayToInt(req.getData()))
                                .getBlockNoTransactions());
                        hasReply = true;
                        break;
                    case GET_MOST_RECENT_BLOCK_NO_TRANSACTIONS:
                        objOut.writeObject(currentBlock.getBlockNoTransactions());
                        hasReply = true;
                        break;
                    case ADD_TRANSACTION:
                        if (ordered)
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

                                objOut.writeBoolean(addTransaction(t));
                            } else {
                                objOut.writeBoolean(false);
                                objOut.writeUTF("Transaction cannot be created without any data");
                            }
                        hasReply = true;
                        break;
                    case IS_CHAIN_VALID:
                        objOut.writeBoolean(this.blockchain.isChainValid());
                        hasReply = true;
                        break;
                    default:
                        logger.error("Unknown type of ClientMessageType");
                }
            } else if (input.getClass() == ReplicaMessage.class) {
                ReplicaMessage req = (ReplicaMessage) input;

                switch (req.getType()) {
                    case SYNC_BLOCKS:
                        // TODO
                        break;
                    case NEW_BLOCK:
                        if (req.getSender() != msgCtx.getLeader()) {
                            objOut.writeBoolean(false);
                            hasReply = true;
                            break;
                        }

                        Block recvBlock;

                        ByteArrayInputStream byteIn2 = new ByteArrayInputStream(req.getContent());
                        ObjectInput objIn2 = new ObjectInputStream(byteIn2);

                        recvBlock = (Block) objIn2.readObject();

                        objIn2.close();
                        byteIn2.close();

                        recvBlock = new Block(recvBlock.getPreviousBlockHash(), recvBlock.getProtocolVersion(),
                                recvBlock.getBlockHeight(), recvBlock.getTransactions(),
                                msgCtx.getTimestamp(), msgCtx.getNonces());

                        if (req.getSender() == this.replicaContext.getStaticConfiguration().getProcessId()) {
                            this.proposedBlock = recvBlock;

                            objOut.writeBoolean(true);
                            hasReply = true;
                            break;
                        }

                        createProposedBlock();

                        if (this.proposedBlock == null) {
                            objOut.writeBoolean(recvBlock.equals(this.blockchain.getMostRecentBlock()));
                        } else {
                            if (recvBlock.equals(this.proposedBlock)) {
                                this.blockchain.addBlock(recvBlock);
                                this.proposedBlock = null;
                                objOut.writeBoolean(true);
                            } else
                                objOut.writeBoolean(false);
                        }
                        hasReply = true;
                        break;
                    default:
                        logger.error("Unknown type of ReplicaMessageType");
                }
            } else
                logger.error("Unknown message class, ignoring");

            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            }

        } catch (IOException | ClassNotFoundException | InstantiationException e) {
            logger.error("ERROR", e);
        }

        return reply;
    }
}
