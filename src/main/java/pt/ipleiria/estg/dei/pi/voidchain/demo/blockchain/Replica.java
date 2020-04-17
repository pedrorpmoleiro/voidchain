package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import bftsmart.demo.map.MapServer;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Replica extends DefaultSingleRecoverable {
    private final Blockchain blockchain;
    private final Logger logger;

    public Replica(int id) {
        this.blockchain = new Blockchain();
        logger = Logger.getLogger(MapServer.class.getName());
        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain.Replica <server id>");
            System.exit(-1);
        }
        new Replica(Integer.parseInt(args[0]));
    }

    @Override
    public void installSnapshot(byte[] state) {
        // NOT YET IMPLEMENTED
    }

    @Override
    public byte[] getSnapshot() {
        // NOT YET IMPLEMENTED

        return new byte[0];
    }

    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        boolean hasReply = false;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            OrderedInputData input = (OrderedInputData) objIn.readObject();

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
                        currentBlock.addTransaction(new Transaction(input.getData(), currentBlock.getProtocolVersion()));
                    }
                    objOut.writeBoolean(true);
                    hasReply = true;
                    break;
                case 6:
                    this.blockchain.createBlock();
                    objOut.writeBoolean(true);
                    hasReply = true;
                    break;
                default:
                    System.err.println("Error on request");
            }

            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "ERROR", e);
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

            int req = objIn.readInt();

            Block currentBlock = this.blockchain.getCurrentBlock();

            switch (req) {
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
                    System.err.println("Error on request");
            }

            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "ERROR", e);
        }

        return reply;
    }
}
