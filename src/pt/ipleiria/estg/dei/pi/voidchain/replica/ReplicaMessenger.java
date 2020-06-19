package pt.ipleiria.estg.dei.pi.voidchain.replica;

import bftsmart.tom.ServiceProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;

import java.io.*;

// TODO: JAVADOC
public class ReplicaMessenger {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final ServiceProxy serviceProxy;

    public ReplicaMessenger(int id) {
        this.serviceProxy = new ServiceProxy(id);
    }

    public boolean proposeBlock(Block block) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            ReplicaMessage req;

            try (ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
                 ObjectOutput objOut2 = new ObjectOutputStream(byteOut2)) {
                objOut2.writeObject(block);

                objOut2.flush();
                byteOut2.flush();

                req = new ReplicaMessage(this.serviceProxy.getProcessId(), ReplicaMessageType.NEW_BLOCK, byteOut2.toByteArray());
            }

            objOut.writeObject(req);

            objOut.flush();
            byteOut.flush();

            byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

            if (reply.length == 0) {
                System.err.println("ERROR");
                return false;
            }

            boolean accepted;

            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                accepted = objIn.readBoolean();
            }

            return accepted;

        } catch (IOException ex) {
            logger.error("Error while processing proposal of new block", ex);
            return false;
        }
    }

    // TODO
    public void syncBlocksDisk() {}
}
