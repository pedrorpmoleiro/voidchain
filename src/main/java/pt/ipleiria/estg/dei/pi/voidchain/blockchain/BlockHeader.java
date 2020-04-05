package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.Util;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Random;

/**
 * The block header is a section of a block.
 * The hash of the block header is what IDs a block in the blockchain
 */
public class BlockHeader {
    /* Attributes */
    private long timestamp;
    private byte[] previousBlockHash;
    private float protocolVersion;
    private int nonce;

    /**
     * Instantiates a new Block header.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     */
    public BlockHeader(byte[] previousBlockHash, float protocolVersion) {
        this.previousBlockHash = previousBlockHash;
        this.protocolVersion = protocolVersion;
        this.timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        this.nonce = new Random().nextInt();
    }

    /* Methods */
    /* Getters */

    /**
     * Gets the Epoch time the block was created
     *
     * @return the timestamp (long)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the hash of the previous block in the chain.
     *
     * @return the previous block hash (byte[])
     */
    public byte[] getPreviousBlockHash() {
        return previousBlockHash;
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version (float)
     */
    public float getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Gets nonce. A random int
     *
     * @return the nonce (int)
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * Calculates the size of the block header in bytes.
     *
     * @return the size (int)
     */
    public int getSize() {
        return Long.SIZE + previousBlockHash.length + Float.SIZE + Integer.SIZE;
    }

    /**
     * Calculates all the attributes in byte array format.
     *
     * @return the data (byte[])
     */
    public byte[] getData() {
        byte[] protocolVersionBytes;
        byte[] timestampBytes;
        byte[] nonceBytes;

        try {
            protocolVersionBytes = Util.floatToByteArray(this.protocolVersion);
            timestampBytes = Util.longToByteArray(this.timestamp);
            nonceBytes = Util.intToByteArray(this.nonce);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }

        int size = protocolVersionBytes.length + timestampBytes.length + this.previousBlockHash.length + nonceBytes.length;
        byte[] dataBytes = new byte[size];
        int i = 0;

        for (byte b : protocolVersionBytes) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : timestampBytes) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : this.previousBlockHash) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : nonceBytes) {
            dataBytes[i] = b;
            i++;
        }

        if (i != size) {
            System.out.println("THIS SHOULDN'T RUN");

            return null;
        }

        return dataBytes;
    }
}
