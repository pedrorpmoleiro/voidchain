package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import bftsmart.tom.server.defaultservices.CommandsInfo;
import bftsmart.tom.server.defaultservices.DefaultApplicationState;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import bftsmart.reconfiguration.ServerViewController;
import bftsmart.reconfiguration.util.TOMConfiguration;
import bftsmart.statemanagement.ApplicationState;
import bftsmart.statemanagement.StateManager;
import bftsmart.statemanagement.strategy.StandardStateManager;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ReplicaContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.Recoverable;
import bftsmart.tom.server.SingleExecutable;
import bftsmart.tom.server.defaultservices.DiskStateLog;
import bftsmart.tom.server.defaultservices.StateLog;

public class Node implements Recoverable, SingleExecutable {
    /* Attributes */
    private Blockchain blockchain;
    private Logger logger;

    // FROM DefaultSingleRecoverable
    protected ReplicaContext replicaContext;
    private TOMConfiguration config;
    private ServerViewController controller;
    private int checkpointPeriod;

    private ReentrantLock logLock;
    private ReentrantLock hashLock;
    private ReentrantLock stateLock;

    private StateLog log;
    private List<byte[]> commands;
    private List<MessageContext> msgContexts;

    private StateManager stateManager;

    public Node(int id) {
        this.blockchain = new Blockchain();

        this.logLock = new ReentrantLock();
        this.hashLock = new ReentrantLock();
        this.stateLock = new ReentrantLock();
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.commands = new ArrayList<>();
        this.msgContexts = new ArrayList<>();

        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain.Node <node_id>");
            System.exit(-1);
        }

        new Node(Integer.parseInt(args[0]));
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
                            currentBlock.addTransaction(new Transaction(input.getData(), currentBlock.getProtocolVersion(), msgCtx.getTimestamp()));
                        }

                        objOut.writeBoolean(true);
                        hasReply = true;
                    }
                    break;
                case 6:
                    if (ordered) {
                        // TODO: IMPROVE SECURITY
                        this.blockchain.createBlock(msgCtx.getTimestamp(), new Random().nextInt());

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

    private void saveCommands(byte[][] commands, MessageContext[] msgCtx) {
        if (commands.length != msgCtx.length) {
            this.logger.debug("----SIZE OF COMMANDS AND MESSAGE CONTEXTS IS DIFFERENT----");
            this.logger.debug("----COMMANDS: " + commands.length + ", CONTEXTS: " + msgCtx.length + " ----");
        }
        this.logLock.lock();

        int cid = msgCtx[0].getConsensusId();
        int batchStart = 0;

        for (int i = 0; i < msgCtx.length; i++) {
            if (i == msgCtx.length) {
                byte[][] batchCommands = Arrays.copyOfRange(commands, batchStart, i);
                MessageContext[] batchMsgCtx = Arrays.copyOfRange(msgCtx, batchStart, i);

                this.log.addMessageBatch(batchCommands, batchMsgCtx, cid);

            } else {
                if (msgCtx[i].getConsensusId() > cid) {
                    byte[][] batchCommands = Arrays.copyOfRange(commands, batchStart, i);
                    MessageContext[] batchMsgCtx = Arrays.copyOfRange(msgCtx, batchStart, i);

                    this.log.addMessageBatch(batchCommands, batchMsgCtx, cid);

                    cid = msgCtx[i].getConsensusId();
                    batchStart = i;
                }
            }
        }

        this.logLock.unlock();
    }

    private void makeCheckpoint(int lastCid) {
        byte[] snapshot = null;

        this.stateLock.lock();
        snapshot = getSnapshot();
        this.stateLock.unlock();

        StateLog log = getLog();
        this.logger.debug("Saving state of CID " + lastCid);

        this.logLock.lock();

        log.newCheckpoint(snapshot, computeHash(snapshot), lastCid);
        log.setLastCID(-1);
        log.setLastCheckpointCID(lastCid);

        this.logLock.unlock();

        this.logger.debug("Finished saving state of CID " + lastCid);
    }

    private StateLog getLog() {
        initLog();

        return this.log;
    }

    private void initLog() {
        // TODO: REVIEW
        if (this.log == null) {
            this.checkpointPeriod = this.config.getCheckpointPeriod();
            byte[] state = getSnapshot();

            if (this.config.isToLog() && this.config.logToDisk()) {
                this.log = new DiskStateLog(this.config.getProcessId(), state,
                        computeHash(state), this.config.isToLog(),
                        this.config.isToWriteSyncLog(), this.config.isToWriteSyncCkp());
            } else {
                this.log = new StateLog(this.controller.getStaticConf().getProcessId(),
                        this.checkpointPeriod, state, computeHash(state));
            }
        }
    }

    private byte[] computeHash(byte[] data) {
        byte[] hash = null;

        SHA3.Digest512 sha3_512 = new SHA3.Digest512();
        RIPEMD160.Digest ripemd160 = new RIPEMD160.Digest();

        this.hashLock.lock();

        hash = ripemd160.digest(sha3_512.digest(data));

        this.hashLock.unlock();

        return hash;
    }

    private byte[] getSnapshot() {
        byte[] snapshot = null;

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
    public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
        return execute(command, msgCtx, false);
    }

    @Override
    public byte[] executeOrdered(byte[] command, MessageContext msgCtx) {
        int cid = msgCtx.getConsensusId();
        byte[] reply = null;

        // !NOOP (??)
        this.stateLock.lock();
        reply = execute(command, msgCtx, true);
        this.stateLock.unlock();

        this.commands.add(command);
        this.msgContexts.add(msgCtx);

        if (msgCtx.isLastInBatch()) {
            if ((cid > 0) && ((cid % this.checkpointPeriod) == 0)) {
                this.logger.debug("Performing checkpoint for consensus " + cid);
                makeCheckpoint(cid);
            } else {
                saveCommands(this.commands.toArray(new byte[0][]), this.msgContexts.toArray(new MessageContext[0]));
            }
            getStateManager().setLastCID(cid);

            this.commands = new ArrayList<>();
            this.msgContexts = new ArrayList<>();
        }

        return reply;
    }

    @Override
    public ApplicationState getState(int cid, boolean sendState) {
        this.logLock.lock();

        ApplicationState state = null;
        if (cid > -1) {
            state = getLog().getApplicationState(cid, sendState);
        } else {
            state = new DefaultApplicationState();
        }

        if (state == null ||
                (this.config.isBFT() && state.getCertifiedDecision(this.controller) == null))
            state = new DefaultApplicationState();

        this.logger.info("Getting log until CID " + cid);
        this.logLock.unlock();

        return state;
    }

    @Override
    public int setState(ApplicationState recvState) {
        int lastCID = -1;

        if (recvState instanceof DefaultApplicationState) {
            DefaultApplicationState state = (DefaultApplicationState) recvState;

            this.logger.info("Last CID in state: " + state.getLastCID());
            this.logLock.lock();

            initLog();
            this.log.update(state);

            this.logLock.unlock();

            int lastCheckpointCID = state.getLastCheckpointCID();

            lastCID = state.getLastCID();

            this.logger.debug("I'm going to update myself from CID "
                    + lastCheckpointCID + " to CID " + lastCID);

            this.stateLock.lock();

            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state.getState());
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {

                this.blockchain = (Blockchain) objIn.readObject();

            } catch (IOException | ClassNotFoundException e) {
                this.logger.error("Error installing snapshot", e);
            }

            for (int cid = lastCheckpointCID + 1; cid <= lastCID; cid++) {
                this.logger.debug("Processing and verifying batched requests for CID " + cid);

                try {
                    CommandsInfo cmdInfo = state.getMessageBatch(cid);
                    byte[][] cmds = cmdInfo.commands;
                    MessageContext[] msgCtxs = cmdInfo.msgCtx;

                    if (cmds == null || msgCtxs == null || msgCtxs[0].isNoOp()) {
                        continue;
                    }

                    for (int i = 0; i < cmds.length; i++) {
                        execute(cmds[i], msgCtxs[i], true);
                    }
                } catch (Exception e) {
                    this.logger.error("Failed to process and verify batched requests", e);

                    if (e instanceof ArrayIndexOutOfBoundsException) {
                        this.logger.info("Last checkpoint, last consensus ID (CID): " + state.getLastCheckpointCID());
                        this.logger.info("Last CID: " + state.getLastCID());
                        this.logger.info("number of messages expected to be in the batch: " + (state.getLastCID() - state.getLastCheckpointCID() + 1));
                        this.logger.info("number of messages in the batch: " + state.getMessageBatches().length);
                    }
                }
            }

            this.stateLock.unlock();
        }

        return lastCID;
    }

    @Override
    public void setReplicaContext(ReplicaContext replicaContext) {
        this.replicaContext = replicaContext;
        this.config = replicaContext.getStaticConfiguration();
        this.controller = replicaContext.getSVController();

        if (this.log == null) {
            this.checkpointPeriod = config.getCheckpointPeriod();
            byte[] state = getSnapshot();

            if (config.isToLog() && config.logToDisk()) {
                this.log = new DiskStateLog(this.config.getProcessId(), state,
                        computeHash(state), this.config.isToLog(),
                        this.config.isToWriteSyncLog(), this.config.isToWriteSyncCkp());

                ApplicationState storedState = ((DiskStateLog) log).loadDurableState();

                if (storedState.getLastCID() > 0) {
                    setState(storedState);
                    getStateManager().setLastCID(storedState.getLastCID());
                }
            } else {
                this.log = new StateLog(this.config.getProcessId(), this.checkpointPeriod, state, computeHash(state));
            }
        }

        getStateManager().askCurrentConsensusId();
    }

    @Override
    public StateManager getStateManager() {
        if(this.stateManager == null)
            this.stateManager = new StandardStateManager();

        return this.stateManager;
    }

    @Override
    public void Op(int CID, byte[] requests, MessageContext msgCtx) {
        // TODO
    }

    @Override
    public void noOp(int CID, byte[][] operations, MessageContext[] msgCtx) {
        // TODO
    }
}
