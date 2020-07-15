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

/**
 * The Block sync server is a service that is responsible for sending block files located in disk to other replicas
 * in order to maintain synchronicity between replicas.
 */
public class BlockSyncServer {
    private static final Logger logger = LoggerFactory.getLogger(BlockSyncServer.class);

    private ServerSocket serverSocket;
    private boolean running;
    private Thread thread;
    private boolean stop;

    /**
     * This will start a new thread to execute the server and wait for client connections.
     */
    public void run() {
        if (running)
            return;

        logger.info("Block Sync server is starting.");
        this.thread = new Thread(this::execute);
        this.thread.start();
    }

    private void execute() {
        Configuration config = Configuration.getInstance();

        try {
            this.serverSocket = new ServerSocket(config.getBlockSyncPort());
        } catch (IOException e) {
            logger.error("Error while starting Server socket", e);
            logger.error("Block sync server not working");
            return;
        }

        this.running = true;
        this.stop = false;
        logger.info("Block Sync Server Running");

        while (true) {
            if (stop || !running) {
                this.stop = false;
                this.running = false;
                break;
            }

            Socket s;
            try {
                s = this.serverSocket.accept();
                logger.info("Accepted client with ip: " + s.getInetAddress());
            } catch (IOException e) {
                logger.error("Error while accepting client", e);
                continue;
            }

            ObjectOutputStream objOut;
            ObjectInputStream objIn;

            int bottom;
            int top;

            try {
                objOut = new ObjectOutputStream(s.getOutputStream());
                objIn = new ObjectInputStream(s.getInputStream());

                bottom = objIn.readInt();
                top = objIn.readInt();
                logger.info("Read interval of blocks to send [" + bottom + "," + top + "]");
            } catch (IOException e) {
                logger.error("Error", e);
                try {
                    s.close();
                    continue;
                } catch (IOException ioException) {
                    logger.error("Error while closing client socket", ioException);
                    continue;
                }
            }

            for (int i = top; i >= bottom; i--) {
                try {
                    logger.info("Sending block " + i + " to client");
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
                logger.info("Order completed, closing connection");
                objIn.close();
                objOut.flush();
                objOut.close();
                s.close();
            } catch (IOException e) {
                logger.error("Error while closing client socket", e);
            }
        }

        try {
            this.serverSocket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            logger.error("Error while closing server socket", ioException);
        }
    }

    /**
     * This will stop the the server from receiving any more client connections and shutdown the execution.
     */
    public void stop() {
        if (running) {
            logger.info("Stopping Block Sync server");
            try {
                this.stop = true;
                this.serverSocket.close();
                this.running = false;
                this.thread.join();
                this.thread = null;
                logger.info("Block Sync server has stopped");
            } catch (IOException e) {
                logger.error("Unable to stop the service", e);
            } catch (InterruptedException e) {
                logger.error("Error while joining threads", e);
            }
        }
    }

    /**
     * This will stop the exection of the server and restart it right after.
     */
    public void restart() {
        if (running) {
            logger.info("Block Sync server restarting");
            this.stop();
        } else
            logger.info("Block Sync server is not running. Starting the server");
        this.run();
    }

    /**
     * Returns if the server is currently running or not.
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }

    public static void main(String[] args) {
        new BlockSyncServer().run();
    }
}
