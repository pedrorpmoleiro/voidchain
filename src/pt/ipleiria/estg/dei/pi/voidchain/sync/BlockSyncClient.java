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
    private static final Logger logger = LoggerFactory.getLogger(BlockSyncClient.class.getName());

    private final ServiceProxy serviceProxy;

    public BlockSyncClient(ServiceProxy serviceProxy) {
        this.serviceProxy = serviceProxy;
    }

    public void sync(boolean allBlocks) throws IOException, ClassNotFoundException {
        int highestBlockHeight = this.getHighestBlockHeight();
        if (highestBlockHeight == -1)
            return;

        int leader = this.getLeader();
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
        Socket s = new Socket(ipLeader.getAddress(), config.getBlockSyncPort());
        ObjectInputStream objIn = new ObjectInputStream(s.getInputStream());
        ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());

        objOut.writeInt(bottom);
        objOut.writeInt(top);

        for (int i = 0; i <= blockNum; i++) {
            Block b = (Block) objIn.readObject();
            b.toDisk();
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
