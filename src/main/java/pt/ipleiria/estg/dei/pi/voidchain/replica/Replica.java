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
import pt.ipleiria.estg.dei.pi.voidchain.sync.BlockSyncClient;
import pt.ipleiria.estg.dei.pi.voidchain.sync.BlockSyncServer;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;
import pt.ipleiria.estg.dei.pi.voidchain.util.SignatureKeyGenerator;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import java.io.*;
import java.security.Security;
import java.util.*;

public class Replica extends DefaultSingleRecoverable {
    private static final Logger logger = LoggerFactory.getLogger(Replica.class);

    private Blockchain blockchain;
    private List<Transaction> transactionPool;
    private final ReplicaMessenger messenger;
    private final BlockSyncServer blockSyncServer;
    private final BlockSyncClient blockSyncClient;
    private Block proposedBlock = null;
    private Thread blockProposalThread = null;
    private int leader = -1;

    /**
     * Instantiates a new Replica.
     *
     * @param id the id
     */
    public Replica(int id, boolean sync) {
        this.blockchain = Blockchain.getInstance();
        this.transactionPool = new ArrayList<>();
        this.messenger = new ReplicaMessenger(id);
        this.blockSyncClient = new BlockSyncClient(this.messenger.getServiceProxy());

        new Thread(() -> {
            this.blockSyncClient.sync(false);
            this.blockchain.reloadBlocksFromDisk();
        }).start();

        this.blockSyncServer = new BlockSyncServer();
        if (sync)
            this.blockSyncServer.run();
        else
            logger.warn("Block sync server is not running on this replica, make sure at least one replica on this " +
                    "machine is running the service (-s option)");

        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            String args1 = args[0];
            args = new String[2];
            args[0] = args1;
            args[1] = "--help";
        }

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        boolean sync = false;
        for (int i = 1; i < args.length; i++)
            if (args[i].startsWith("-"))
                switch (args[i]) {
                    case "-s":
                    case "--sync":
                        sync = true;
                        break;
                    case "--help":
                        System.out.println("USAGE: voidchain_replica <id> [OPTIONS]" + System.lineSeparator());
                        System.out.println("OPTIONS:");
                        System.out.println("\t * Use --sync (-s) to enable block sync server.");
                        System.out.println("\t Note: If more than one replica is running in the same system, " +
                                "only one should have this option enabled");
                        System.out.println("\t * Use --help to display this information");
                        return;
                    default:
                        logger.info("Unknown option '" + args[i] + "'");
                }

        Storage.createDefaultConfigFiles();

        int id = Integer.parseInt(args[0]);
        SignatureKeyGenerator.generatePubAndPrivKeys(id);
        SignatureKeyGenerator.generateSSLKey(id);

        SignatureKeyGenerator.generatePubAndPrivKeys(-42); // Genesis Block Priv & Pub Key

        new Replica(id, sync);
    }

    private void createProposedBlock() {
        if (this.proposedBlock != null) return;

        Configuration config = Configuration.getInstance();
        int transactionsPerBlock = config.getNumTransactionsInBlock();
        if (this.transactionPool.size() < transactionsPerBlock) return;

        logger.info("Creating block to be proposed from memory pool transactions");

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
            logger.error("Error creating new proposed block instance", e);
            this.transactionPool.addAll(transactions);
        }

        logger.info("Proposed block created");
    }

    private void processNewBlock() {
        if (this.replicaContext.getStaticConfiguration().getProcessId() == leader) {
            createProposedBlock();

            if (this.proposedBlock == null) return;

            logger.info("Proposing block to the network");
            if (this.messenger.proposeBlock(this.proposedBlock))
                if (this.proposedBlock != null) {
                    this.blockchain.addBlock(this.proposedBlock);
                    logger.info("Block proposal accepted, adding block to local blockchain");
                } else {
                    this.transactionPool.addAll(this.proposedBlock.getTransactions().values());
                    logger.info("Block proposal failed");
                }
            this.proposedBlock = null;
        }

        try {
            Thread.sleep(5000);
            processNewBlock();
        } catch (InterruptedException e) {
            logger.error("Block Proposal Thread error while waiting", e);
            this.blockProposalThread = null;
        }
    }

    /**
     * Adds a single transaction to the memory pool. Starts the process of proposing new blocks if conditions are met.
     *
     * @param transaction the transaction
     * @return true if the transaction was successfully added to the memory pool or false otherwise
     */
    public boolean addTransaction(Transaction transaction) {
        int aux = this.transactionPool.size();
        this.transactionPool.add(transaction);

        if (aux == this.transactionPool.size() || (aux + 1) != this.transactionPool.size()) {
            logger.error("Error occurred while adding transaction to memory pool", transaction, this.transactionPool);
            return false;
        }

        logger.info("Transaction added to memory pool");
        return true;
    }

    /**
     * Adds a list of transactions to the memory pool. Starts the process of proposing new blocks if conditions are met.
     *
     * @param transactions the transactions
     * @return true if the transactions were successfully added to the memory pool or false otherwise
     */
    public boolean addTransactions(List<Transaction> transactions) {
        int aux = this.transactionPool.size();
        this.transactionPool.addAll(transactions);

        if (aux == this.transactionPool.size() || (aux + transactions.size()) != this.transactionPool.size()) {
            logger.error("Error occurred while adding transactions to memory pool", transactions, this.transactionPool);
            return false;
        }

        logger.info("Transactions added to memory pool");
        return true;
    }

    @Override
    public void installSnapshot(byte[] state) {
        if (Arrays.equals(state, new byte[0]))
            return;

        logger.info("Installing snapshot from network");
        List<Transaction> transactionPool2 = this.transactionPool;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {

            this.transactionPool = (List<Transaction>) objIn.readObject();
            this.blockSyncClient.sync(false);
            this.blockchain.reloadBlocksFromDisk();

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error installing snapshot", e);
            this.transactionPool = transactionPool2;
        }
    }

    @Override
    public byte[] getSnapshot() {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(this.transactionPool);

            objOut.flush();
            byteOut.flush();

            return byteOut.toByteArray();

        } catch (IOException e) {
            logger.error("Error getting snapshot", e);
            return new byte[0];
        }
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

        if (msgCtx.getLeader() != -1 && msgCtx.getLeader() != this.leader)
            this.leader = msgCtx.getLeader();

        if (this.blockProposalThread == null) {
            this.blockProposalThread = new Thread(this::processNewBlock);
            this.blockProposalThread.start();
        }

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            Object input = objIn.readObject();
            Configuration config = Configuration.getInstance();

            if (input.getClass() == ClientMessage.class) {
                Block currentBlock = this.blockchain.getMostRecentBlock();
                ClientMessage req = (ClientMessage) input;

                switch (req.getType()) {
                    case GET_MOST_RECENT_BLOCK:
                        logger.info("Returning Most Recently Created Block data to client");
                        objOut.writeObject(currentBlock.getBlockNoTransactions());
                        hasReply = true;
                        break;
                    case GET_MOST_RECENT_BLOCK_HEIGHT:
                        logger.info("Returning Most Recently Created Block Height to client");
                        objOut.writeInt(currentBlock.getBlockHeight());
                        hasReply = true;
                        break;
                    case GET_BLOCK:
                        if (req.hasContent()) {
                            int bh = Converters.convertByteArrayToInt(req.getContent());
                            logger.info("Returning Block " + bh + " data to client");
                            objOut.writeObject(this.blockchain.getBlock(bh)
                                    .getBlockNoTransactions());
                            hasReply = true;
                        } else
                            logger.error("Message has no content, ignoring");
                        break;
                    case ADD_TRANSACTION:
                        if (ordered) {
                            if (req.hasContent()) {
                                logger.info("Processing ADD_TRANSACTION request");

                                ByteArrayInputStream byteIn2 = new ByteArrayInputStream(req.getContent());
                                ObjectInput objIn2 = new ObjectInputStream(byteIn2);

                                Transaction t = (Transaction) objIn2.readObject();

                                objIn2.close();
                                byteIn2.close();

                                objOut.writeBoolean(this.addTransaction(t));
                                hasReply = true;
                            } else
                                logger.error("Message has no content, ignoring");
                        } else
                            logger.info("Received unordered ADD_TRANSACTION message, ignoring");
                        break;
                    case ADD_TRANSACTIONS:
                        if (ordered) {
                            if (req.hasContent()) {
                                logger.info("Processing ADD_TRANSACTIONS request");

                                ByteArrayInputStream byteIn2 = new ByteArrayInputStream(req.getContent());
                                ObjectInput objIn2 = new ObjectInputStream(byteIn2);

                                List<Transaction> tl = (List<Transaction>) objIn2.readObject();

                                objIn2.close();
                                byteIn2.close();

                                objOut.writeBoolean(this.addTransactions(tl));
                                hasReply = true;
                            } else
                                logger.error("Message has no content, ignoring");
                        } else
                            logger.info("Received unordered ADD_TRANSACTIONS message, ignoring");
                        break;
                    case IS_CHAIN_VALID:
                        logger.info("Returning Blockchain validity to client");
                        objOut.writeBoolean(this.blockchain.isChainValid());
                        hasReply = true;
                        break;
                    case GET_LEADER:
                        logger.info("Returning last consensus leader to client");
                        objOut.writeInt(msgCtx.getLeader());
                        hasReply = true;
                        break;
                    default:
                        logger.error("Unknown type of ClientMessageType");
                }
            } else if (input.getClass() == ReplicaMessage.class) {
                ReplicaMessage req = (ReplicaMessage) input;

                switch (req.getType()) {
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

                        boolean aux = recvBlock.equals(this.blockchain.getMostRecentBlock());
                        if (this.proposedBlock == null || aux) {
                            objOut.writeBoolean(aux);
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
