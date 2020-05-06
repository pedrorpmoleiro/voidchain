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

import java.io.*;
import java.security.Security;

/*
    TODO: READ BELOW
    ON DISK:
        SAVE BLOCK/(S)
        LOAD BLOCK/(S)
    API
    AUTOMATION OF MANAGEMENT OF THE BLOCKCHAIN:
        I.   COMMUNICATION BETWEEN REPLICAS (MAKE OWN SystemMessage)
        II.  CREATE BLOCKS VIA TRANSACTION POOL
        III. TRANSACTION POOL ON REPLICA (MOVE FROM BLOCKCHAIN (?)/CHANGE GET & INSTALL SNAPSHOT)
        IV.  VALIDATE BLOCKS
        V.
*/
public class Replica extends DefaultSingleRecoverable {
    private Blockchain blockchain;
    private Logger logger;

    public Replica(int id) {
        this.blockchain = new Blockchain();
        this.logger = LoggerFactory.getLogger(this.getClass().getName());

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

    @Override
    public void installSnapshot(byte[] state) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(state);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {

            this.blockchain = (Blockchain) objIn.readObject();

        } catch (IOException | ClassNotFoundException e) {
            this.logger.error("Error installing snapshot", e);
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
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
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
                    if (input.hasData()) {
                        currentBlock.addTransaction(new Transaction(input.getData(), currentBlock.getProtocolVersion(),
                                msgCtx.getTimestamp()));
                    }

                    objOut.writeBoolean(true);
                    hasReply = true;
                    break;
                case 6:
                    this.blockchain.createBlock(msgCtx.getTimestamp(), msgCtx.getNonces());

                    objOut.writeBoolean(true);
                    hasReply = true;
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
}
