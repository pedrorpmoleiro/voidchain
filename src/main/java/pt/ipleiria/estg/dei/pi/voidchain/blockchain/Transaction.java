package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA3;
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

        byte[] protocolVersionBytes;
        byte[] timestampBytes;
        byte[] sizeBytes;

        try {
            protocolVersionBytes = Util.floatToByteArray(this.protocolVersion);
            timestampBytes = Util.longToByteArray(this.timestamp);
            sizeBytes = Util.intToByteArray(this.size);
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

        int size = protocolVersionBytes.length + timestampBytes.length + sizeBytes.length + this.data.length;
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
        for (byte b : sizeBytes) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : this.data) {
            dataBytes[i] = b;
            i++;
        }

        if (i != size) {
            System.out.println("THIS SHOULDN'T RUN");

            return;
        }

        SHA3.Digest512 sha3_512 = new SHA3.Digest512();
        RIPEMD160.Digest ripemd160 = new RIPEMD160.Digest();

        this.hash = ripemd160.digest(sha3_512.digest(dataBytes));
    }

    /* Methods */

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
     * Gets the hash of a transaction ( SHA3_512(RIPEMD160(TX)) ).
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
}
