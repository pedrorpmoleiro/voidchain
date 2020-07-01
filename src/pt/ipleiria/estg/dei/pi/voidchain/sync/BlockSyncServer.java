package pt.ipleiria.estg.dei.pi.voidchain.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// TODO: JAVADOC
public class BlockSyncServer {
    private static final Logger logger = LoggerFactory.getLogger(BlockSyncServer.class.getName());

    private ServerSocket serverSocket;
    private boolean running;

    public void run() throws IOException, ClassNotFoundException {
        if (running)
            return;

        Configuration config = Configuration.getInstance();

        this.serverSocket = new ServerSocket(config.getBlockSyncPort());
        this.running = true;
        logger.info("Block Sync Server Running");

        while (true) {
            Socket s = this.serverSocket.accept();
            ObjectInputStream objIn = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream objOut = new ObjectOutputStream(s.getOutputStream());

            int bottom = objIn.readInt();
            int top = objIn.readInt();

            for (int i = top; i >= bottom ; i--) {
                objOut.writeObject(Block.fromDisk(i));
            }
        }
    }

    public void stop() throws IOException {
        if (running) {
            this.serverSocket.close();
            this.running = false;
            logger.info("Block Sync server has stopped");
        }

    }

    public void restart() throws IOException, ClassNotFoundException {
        logger.info("Block Sync server restarting");
        this.stop();
        this.run();
    }

    public boolean isRunning() {
        return running;
    }
}
