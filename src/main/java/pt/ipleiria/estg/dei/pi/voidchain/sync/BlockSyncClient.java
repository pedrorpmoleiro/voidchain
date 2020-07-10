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
        if (this.serviceProxy == null) {
            logger.error("BFT-SMaRt service proxy is not defined");
            return;
        }

        int highestBlockHeight;
        try {
            highestBlockHeight = this.getHighestBlockHeight();
            logger.info("Highest block in the chain: " + highestBlockHeight);
        } catch (IOException e) {
            logger.error("Error while retrieving highest block height in the chain", e);
            return;
        }
        if (highestBlockHeight == -1)
            return;

        int leader;
        try {
            leader = this.getLeader();
            logger.info("Last consensus leader: " + leader);
        } catch (IOException e) {
            logger.error("Error while retrieving consensus leader", e);
            return;
        }
        if (leader == -1)
            return;

        if (!this.serviceProxy.getViewManager().isCurrentViewMember(leader))
            return;

        int bottom;
        int top;

        if (allBlocks) {
            bottom = 0;
            top = highestBlockHeight;
        } else {
            // TODO: DEFINE TOP & BOTTOM WITH BLOCKS IN DISK
            bottom = 0;
            top = 0;
        }

        Configuration config = Configuration.getInstance();

        int blockNum;
        if (bottom == 0)
            blockNum = top;
        else
            blockNum = top - bottom + 1;

        InetSocketAddress ipLeader = new InetSocketAddress(
                this.serviceProxy.getViewManager().getStaticConf().getRemoteAddress(leader).getAddress(),
                config.getBlockSyncPort());
        //InetSocketAddress ipLeader = new InetSocketAddress("127.0.0.1", config.getBlockSyncPort()); // For testing

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
            Block b;
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
        logger.info("Retrieving highest block in the chain from network");

        ClientMessage cm = new ClientMessage(ClientMessageType.GET_MOST_RECENT_BLOCK_HEIGHT);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutput objOut = new ObjectOutputStream(byteOut);

        objOut.writeObject(cm);

        objOut.flush();
        byteOut.flush();

        byte[] reply = this.serviceProxy.invokeUnordered(byteOut.toByteArray());

        objOut.close();
        byteOut.close();

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

    private int getLeader() throws IOException {
        logger.info("Retrieving last consensus leader from network");

        ClientMessage cm = new ClientMessage(ClientMessageType.GET_LEADER);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutput objOut = new ObjectOutputStream(byteOut);

        objOut.writeObject(cm);

        objOut.flush();
        byteOut.flush();

        byte[] reply = this.serviceProxy.invokeUnordered(byteOut.toByteArray());

        objOut.close();
        byteOut.close();

        if (reply.length == 0) {
            logger.error("Empty reply from replicas");
            return -1;
        }

        ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
        ObjectInput objIn = new ObjectInputStream(byteIn);

        // ERROR ALWAYS READING -1
        int l = objIn.readInt();

        objIn.close();
        byteIn.close();

        return l;
    }

    // TESTING MAIN
    /*
     * Before running make sure either to create a bft-smart service proxy and have a replicas running or
     * comment the try catch blocks of getHighestBlockHeigh and getLeader and replace the IP of the leader replica
     * with a static IP
     */
    public static void main(String[] args) {
        BlockSyncClient client = new BlockSyncClient(null);
        client.sync(true);
    }
}
