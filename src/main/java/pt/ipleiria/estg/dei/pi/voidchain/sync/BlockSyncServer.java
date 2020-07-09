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
    private static final Logger logger = LoggerFactory.getLogger(BlockSyncServer.class);

    private ServerSocket serverSocket;
    private boolean running;

    public void run() {
        if (running)
            return;

        Configuration config = Configuration.getInstance();

        try {
            this.serverSocket = new ServerSocket(config.getBlockSyncPort());
        } catch (IOException e) {
            logger.error("Error while starting Server socket", e);
            logger.error("Block sync server not working");
            return;
        }

        this.running = true;
        logger.info("Block Sync Server Running");

        while (true) {
            Socket s = null;
            try {
                s = this.serverSocket.accept();
            } catch (IOException e) {
                logger.error("Error while accepting client", e);
                continue;
            }

            ObjectOutputStream objOut = null;
            ObjectInputStream objIn = null;

            int bottom = 0;
            int top = 0;

            try {
                objOut = new ObjectOutputStream(s.getOutputStream());
                objIn = new ObjectInputStream(s.getInputStream());

                bottom = objIn.readInt();
                top = objIn.readInt();
            } catch (IOException e) {
                logger.error("Error", e);
                try {
                    objIn.close();
                    objOut.close();
                    s.close();
                    continue;
                } catch (IOException ioException) {
                    logger.error("Error while closing client socket", ioException);
                    continue;
                }
            }

            for (int i = top; i >= bottom; i--) {
                try {
                    objOut.writeObject(Block.fromDisk(i));
                    objOut.flush();
                } catch (IOException | ClassNotFoundException e) {
                    logger.error("Error while trying to retrieve block", e);
                    try {
                        objOut.writeObject(null);
                    } catch (IOException ioException) {
                        logger.error("Unable to write null to client after error", ioException);
                    }
                    break;
                }
            }

            try {
                objIn.close();
                objOut.flush();
                objOut.close();
                s.close();
            } catch (IOException e) {
                logger.error("Error while closing client socket", e);
            }
        }
    }

    public void stop() {
        if (running)
            try {
                this.serverSocket.close();
                this.running = false;
                logger.info("Block Sync server has stopped");
            } catch (IOException e) {
                logger.error("Unable to stop the service", e);
            }
    }

    public void restart() {
        logger.info("Block Sync server restarting");
        this.stop();
        this.run();
    }

    public boolean isRunning() {
        return running;
    }
}
