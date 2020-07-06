package pt.ipleiria.estg.dei.pi.voidchain.sync;

import bftsmart.tom.ServiceProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessage;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessageType;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

// TODO: JAVADOC
public class BlockSyncClient {
    private static final Logger logger = LoggerFactory.getLogger(BlockSyncClient.class);

    private final ServiceProxy serviceProxy;

    public BlockSyncClient(ServiceProxy serviceProxy) {
        this.serviceProxy = serviceProxy;
    }

    public void sync(boolean allBlocks) {
        int highestBlockHeight = 0;
        try {
            highestBlockHeight = this.getHighestBlockHeight();
        } catch (IOException e) {
            logger.error("Error while retrieving highest block height in the chain", e);
            return;
        }
        if (highestBlockHeight == -1)
            return;

        int leader = 0;
        try {
            leader = this.getLeader();
        } catch (IOException e) {
            logger.error("Error while retrieving consensus leader", e);
        }
        if (leader == -1)
            return;

        if (!this.serviceProxy.getViewManager().isCurrentViewMember(leader))
            return;

        int bottom;
        int top;

        if (allBlocks) {
            bottom = 1;
            top = highestBlockHeight;
        } else {
            // TODO: DEFINE TOP & BOTTOM WITH BLOCKS IN DISK
            bottom = 0;
            top = 0;
        }

        int blockNum = top - bottom + 1;

        InetSocketAddress ipLeader = this.serviceProxy.getViewManager().getStaticConf().getLocalAddress(leader);
        Configuration config = Configuration.getInstance();
        Socket s = null;
        try {
            s = new Socket(ipLeader.getAddress(), config.getBlockSyncPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectOutputStream objOut = null;
        ObjectInputStream objIn = null;
        try {
            objOut = new ObjectOutputStream(s.getOutputStream());
            objIn = new ObjectInputStream(s.getInputStream());

            objOut.writeInt(bottom);
            objOut.writeInt(top);

            objOut.flush();
        } catch (IOException e) {
            logger.error("Error", e);
            try {
                objIn.close();
                objOut.close();
                s.close();
            } catch (IOException ioException) {
                logger.error("Unable to close socket to server", ioException);
            }
        }

        for (int i = 0; i <= blockNum; i++) {
            Block b = null;
            try {
                b = (Block) objIn.readObject();

                if (b == null)
                    throw new IllegalArgumentException();

                b.toDisk();
            } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {
                e.printStackTrace();
                logger.error("Error while retrieving block from server", e);
                break;
            }
        }

        try {
            objIn.close();
            objOut.close();
            s.close();
            logger.debug("Server socket closed");
        } catch (IOException e) {
            logger.error("Unable to close socket to server", e);
        }
    }

    private int getHighestBlockHeight() throws IOException {
        ClientMessage cm = new ClientMessage(ClientMessageType.GET_MOST_RECENT_BLOCK_HEIGHT);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutput objOut = new ObjectOutputStream(byteOut);

        objOut.writeObject(cm);

        objOut.flush();
        byteOut.flush();

        objOut.close();
        byteOut.close();

        byte[] reply = this.serviceProxy.invokeUnordered(byteOut.toByteArray());

        if (reply.length == 0) {
            logger.error("Empty reply from replicas");
            return -1;
        }

        ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
        ObjectInput objIn = new ObjectInputStream(byteIn);

        int hbh = objIn.readInt();

        objIn.close();
        byteIn.close();

        return hbh;
    }

    private int getLeader() throws  IOException {
        ClientMessage cm = new ClientMessage(ClientMessageType.GET_LEADER);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutput objOut = new ObjectOutputStream(byteOut);

        objOut.writeObject(cm);

        objOut.flush();
        byteOut.flush();

        objOut.close();
        byteOut.close();

        byte[] reply = this.serviceProxy.invokeUnordered(byteOut.toByteArray());

        if (reply.length == 0) {
            logger.error("Empty reply from replicas");
            return -1;
        }

        ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
        ObjectInput objIn = new ObjectInputStream(byteIn);

        int l = objIn.readInt();

        objIn.close();
        byteIn.close();

        return l;
    }
}
