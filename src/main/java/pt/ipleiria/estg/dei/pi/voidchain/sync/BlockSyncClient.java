package pt.ipleiria.estg.dei.pi.voidchain.sync;

import bftsmart.tom.ServiceProxy;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessage;
import pt.ipleiria.estg.dei.pi.voidchain.client.ClientMessageType;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.Security;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BlockSyncClient {
    private static final Logger logger = LoggerFactory.getLogger(BlockSyncClient.class);

    private final ServiceProxy serviceProxy;

    /**
     * Instantiates a new Block sync client.
     *
     * @param serviceProxy the service proxy
     */
    public BlockSyncClient(ServiceProxy serviceProxy) {
        this.serviceProxy = serviceProxy;
    }

    /**
     * Syncs blocks from other replicas to the current machine.
     *
     * @param allBlocks pass true if you wish to overwrite the blocks already stored in disk and false to only sync the missing blocks
     */
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

        List<Integer> heightArray = Blockchain.getBlockFileHeightArray();
        if (heightArray == null) {
            logger.error("Error retriving block file heights from disk");
            return;
        }

        int top= highestBlockHeight;
        if (heightArray.get(heightArray.size() - 1) == highestBlockHeight) {
            logger.info("Highest block from network is equal to the highest block in disk");
            return;
        }

        int bottom;
        if (allBlocks)
            bottom = 0;
        else
            bottom = heightArray.get(heightArray.size() - 1);

        logger.info("Requesting [" + bottom + "," + top + "] blocks");

        Configuration config = Configuration.getInstance();

        int blockNum;
        if (bottom == 0)
            blockNum = top;
        else
            blockNum = top - bottom + 1;

        int[] processes = this.serviceProxy.getViewManager().getCurrentView().getProcesses();
        Map<Long, InetAddress> pingTimesAdd = new TreeMap<>();

        for (int p : processes) {
            InetAddress address = this.serviceProxy.getViewManager().getCurrentView().getAddress(p).getAddress();
            logger.info("Pinging replica " + p + " on " + address.toString());
            try {
                long time = Instant.now().toEpochMilli();
                // 2 second timeout
                if (address.isReachable(2000)) {
                    time = Instant.now().toEpochMilli() - time;
                    logger.info("Pinged replica " + p + " in " + time + " milliseconds");
                    pingTimesAdd.put(time, address);
                } else
                    logger.error("Replica " + p + " is unreachable");
            } catch (IOException ioException) {
                logger.error("Error while pinging replica " + p + " on " + address.toString());
            }
        }

        if (pingTimesAdd.size() == 0)
            return;

        ArrayList<Long> pingTimes = new ArrayList<>(pingTimesAdd.keySet());
        pingTimes.sort(Long::compare);

        InetSocketAddress replicaAdd = new InetSocketAddress(pingTimesAdd.get(pingTimes.get(0)),
                config.getBlockSyncPort());
        //InetSocketAddress replicaAdd = new InetSocketAddress(this.serviceProxy.getViewManager().getCurrentView().getAddress(0).getAddress(), config.getBlockSyncPort()); // For testing
        //InetSocketAddress ipLeader = new InetSocketAddress("127.0.0.1", config.getBlockSyncPort()); // For testing

        Socket s;
        try {
            s = new Socket(replicaAdd.getAddress(), config.getBlockSyncPort());
        } catch (IOException e) {
            logger.error("Error while opening socket to server", e);
            return;
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
            return;
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

    // TESTING MAIN
    /*
     * Before running make sure either to create a bft-smart service proxy and have a replicas running or
     * comment the try catch blocks of getHighestBlockHeigh and getLeader and replace the IP of the leader replica
     * with a static IP
     */
    public static void main(String[] args) {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        BlockSyncClient client = new BlockSyncClient(new ServiceProxy(100));
        client.sync(true);
    }
}