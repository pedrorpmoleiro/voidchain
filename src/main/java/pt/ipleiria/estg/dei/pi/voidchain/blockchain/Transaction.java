package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.util.encoders.Base64;
import pt.ipleiria.estg.dei.pi.voidchain.Util;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The transaction contains data of the operations performed by the replicas.
 * In the 'transactions' section of a block, transactions get recorded.
 */
public class Transaction implements Serializable {
    /* Attributes */
    private final long timestamp;
    private final byte[] data;
    private final int size;
    private final float protocolVersion;
    private byte[] hash;

    /**
     * Instantiates a new Transaction.
     *
     * @param data            the data
     * @param protocolVersion the protocol version
     */
    public Transaction(byte[] data, float protocolVersion) {
        this.timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        this.data = data;
        this.protocolVersion = protocolVersion;
        this.size = Long.SIZE + this.data.length + Integer.SIZE + Float.SIZE;

        byte[] dataBytes = getDataBytes(this.timestamp, this.data, this.size, this.protocolVersion);

        if (dataBytes == null) {
            // TODO: ERROR
            return;
        }

        this.hash = Util.calculateHash(dataBytes);
    }


    /**
     * Instantiates a new Transaction.
     *
     * @param data            the data
     * @param protocolVersion the protocol version
     * @param timestamp       the timestamp
     */
    public Transaction(byte[] data, float protocolVersion, long timestamp) {
        this.timestamp = timestamp;
        this.data = data;
        this.protocolVersion = protocolVersion;
        this.size = Long.SIZE + this.data.length + Integer.SIZE + Float.SIZE;

        byte[] dataBytes = getDataBytes(this.timestamp, this.data, this.size, this.protocolVersion);

        if (dataBytes == null) {
            // TODO: ERROR
            return;
        }

        this.hash = Util.calculateHash(dataBytes);
    }

    /* Methods */
    private static byte[] getDataBytes(long timestamp, byte[] data, int size, float protocolVersion) {
        byte[] protocolVersionBytes;
        byte[] timestampBytes;
        byte[] sizeBytes;

        try {
            protocolVersionBytes = Util.floatToByteArray(protocolVersion);
            timestampBytes = Util.longToByteArray(timestamp);
            sizeBytes = Util.intToByteArray(size);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        int sizeAux = protocolVersionBytes.length + timestampBytes.length + sizeBytes.length + data.length;
        byte[] dataBytes = new byte[sizeAux];
        int i = 0;

        for (byte b : protocolVersionBytes) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : timestampBytes) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : sizeBytes) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : data) {
            dataBytes[i] = b;
            i++;
        }

        if (i != sizeAux) {
            System.out.println("THIS SHOULDN'T RUN");

            return null;
        }

        return dataBytes;
    }

    /* Getters */

    /**
     * Gets Epoch time of when a transaction was created.
     *
     * @return the timestamp (long)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the data of a transaction.
     *
     * @return the data (byte[])
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the size of a transaction in Bytes.
     *
     * @return the size (int)
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the hash of the transaction ( SHA3_512(RIPEMD160(TX)) ).
     *
     * @return the hash (byte[])
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version
     */
    public float getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String toString() {
        return "Transaction: {" + System.lineSeparator() +
                "timestamp: " + timestamp + System.lineSeparator() +
                "data: " + Base64.toBase64String(data) + System.lineSeparator() +
                "size: " + size + System.lineSeparator() +
                "protocol version: " + protocolVersion + System.lineSeparator() +
                "hash: " + Base64.toBase64String(hash) + System.lineSeparator() +
                "}" + System.lineSeparator();
    }
}
