package pt.ipleiria.estg.dei.pi.voidchain.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessage;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;
import pt.ipleiria.estg.dei.pi.voidchain.util.SignatureKeyGenerator;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import java.io.*;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Faulty Replica class that proposes a malicious block.
 */
public class FaultyReplica4 extends DefaultSingleRecoverable {
    private Blockchain blockchain;
    private List<Transaction> transactionPool;
    private final ReplicaMessenger messenger;
    private Block proposedBlock = null;
    private Thread blockProposalThread = null;
    private int leader = -1;

    public FaultyReplica4(int id) {
        this.blockchain = Blockchain.getInstance();
        this.transactionPool = new ArrayList<>();
        this.messenger = new ReplicaMessenger(id);

        new ServiceReplica(id, this, this);
    }

    private void createProposedBlock() {
        if (this.proposedBlock != null) return;

        try {
            this.proposedBlock = new Block(new byte[10], "BLABLABLA", -546, new ArrayList<>(), -1000000000L, new byte[10]);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void processNewBlock() {
        if (this.replicaContext.getStaticConfiguration().getProcessId() == leader) {
            createProposedBlock();

            if (this.proposedBlock == null) return;

            if (this.messenger.proposeBlock(this.proposedBlock))
                if (this.proposedBlock != null) {
                    this.blockchain.addBlock(this.proposedBlock);
                } else {
                    this.transactionPool.addAll(this.proposedBlock.getTransactions().values());
                }
            this.proposedBlock = null;
        }

        try {
            Thread.sleep(5000);
            processNewBlock();
        } catch (InterruptedException e) {
            this.blockProposalThread = null;
        }
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

    @Override
    public void installSnapshot(byte[] bytes) {
        if (Arrays.equals(bytes, new byte[0]))
            return;

        List<Transaction> transactionPool2 = this.transactionPool;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {
            this.transactionPool = (List<Transaction>) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
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
                        objOut.writeObject(currentBlock.getBlockNoTransactions());
                        hasReply = true;
                        break;
                    case GET_MOST_RECENT_BLOCK_HEIGHT:
                        objOut.writeInt(currentBlock.getBlockHeight());
                        hasReply = true;
                        break;
                    case GET_BLOCK:
                        if (req.hasContent()) {
                            int bh = Converters.convertByteArrayToInt(req.getContent());
                            objOut.writeObject(this.blockchain.getBlock(bh)
                                    .getBlockNoTransactions());
                            hasReply = true;
                        }
                        break;
                    case ADD_TRANSACTION:
                        if (ordered) {
                            if (req.hasContent()) {
                                ByteArrayInputStream byteIn2 = new ByteArrayInputStream(req.getContent());
                                ObjectInput objIn2 = new ObjectInputStream(byteIn2);

                                Transaction t = (Transaction) objIn2.readObject();

                                objIn2.close();
                                byteIn2.close();

                                objOut.writeBoolean(this.addTransaction(t));
                                hasReply = true;
                            }
                        }
                        break;
                    case ADD_TRANSACTIONS:
                        if (ordered) {
                            if (req.hasContent()) {
                                ByteArrayInputStream byteIn2 = new ByteArrayInputStream(req.getContent());
                                ObjectInput objIn2 = new ObjectInputStream(byteIn2);

                                List<Transaction> tl = (List<Transaction>) objIn2.readObject();

                                objIn2.close();
                                byteIn2.close();

                                objOut.writeBoolean(this.addTransactions(tl));
                                hasReply = true;
                            }
                        }
                        break;
                    case IS_CHAIN_VALID:
                        objOut.writeBoolean(this.blockchain.isChainValid());
                        hasReply = true;
                        break;
                    case GET_LEADER:
                        objOut.writeInt(msgCtx.getLeader());
                        hasReply = true;
                        break;
                    default:
                        System.out.println("blablabla");
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
                        System.out.println("blablabla");
                }
            }

            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            }

        } catch (IOException | ClassNotFoundException | InstantiationException e) {
            e.printStackTrace();
        }

        return reply;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: FaultyReplica4 <id>");
            System.exit(-1);
        }

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        Storage.createDefaultConfigFiles();

        int id = Integer.parseInt(args[0]);
        SignatureKeyGenerator.generatePubAndPrivKeys(id);
        SignatureKeyGenerator.generateSSLKey(id);

        SignatureKeyGenerator.generatePubAndPrivKeys(-42); // Genesis Block Priv & Pub Key

        new FaultyReplica4(id);
    }
}
