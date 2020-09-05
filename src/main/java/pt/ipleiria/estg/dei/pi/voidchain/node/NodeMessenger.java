package pt.ipleiria.estg.dei.pi.voidchain.node;

import bftsmart.tom.ServiceProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;

import java.io.*;

/**
 * The Replica messenger is a replica "client" that is tasked with communicating with allowing a replica to communicate
 * with other replicas in the network.
 */
public class NodeMessenger {
    private static final Logger logger = LoggerFactory.getLogger(NodeMessenger.class);

    private final ServiceProxy serviceProxy;

    /**
     * Instantiates a new Replica messenger.
     *
     * @param id the id
     */
    public NodeMessenger(int id) {
        this.serviceProxy = new ServiceProxy(id);
    }

    /**
     * Proposes a new block to the network.
     *
     * @param block the block
     * @return true if the proposed block was accepted or false otherwise
     */
    public boolean proposeBlock(Block block) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            NodeMessage req;

            logger.info("Proposing new block (" + block.getBlockHeight() + ") to network");

            try (ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
                 ObjectOutput objOut2 = new ObjectOutputStream(byteOut2)) {
                objOut2.writeObject(block);

                objOut2.flush();
                byteOut2.flush();

                req = new NodeMessage(this.serviceProxy.getProcessId(), NodeMessageType.NEW_BLOCK, byteOut2.toByteArray());
            }

            objOut.writeObject(req);

            objOut.flush();
            byteOut.flush();

            byte[] reply;
            try {
                reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
            } catch (RuntimeException e) {
                logger.error("Error while sending proposed block to network", e);
                return false;
            }

            if (reply == null ||reply.length == 0) {
                System.err.println("No reply from network");
                return false;
            }

            boolean accepted;

            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                accepted = objIn.readBoolean();
            }

            logger.info("Block proposal " + (accepted ? "accepted" : "failed"));

            return accepted;

        } catch (IOException ex) {
            logger.error("Error while processing proposal of new block", ex);
            return false;
        }
    }

    /**
     * Returns the bft-smart service proxy used to communicate with the network.
     *
     * @return the service proxy
     */
    protected ServiceProxy getServiceProxy() {
        return serviceProxy;
    }
}
