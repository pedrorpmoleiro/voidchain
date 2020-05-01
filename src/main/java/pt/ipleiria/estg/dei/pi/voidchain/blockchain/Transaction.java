package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;
import pt.ipleiria.estg.dei.pi.voidchain.util.Hash;

import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.Serializable;

/**
 * The transaction contains data of the operations performed by the replicas.
 * In the 'transactions' section of a block, transactions get recorded.
 * The structure of a transaction is: timestamp (when it was created), its size
 *  , the version of the protocol when the transaction was created and the hash of the transaction (trasaction ID).
 */

// TODO: Ver se transaction Hash pertecen à estrutura da transacção.

public class Transaction implements Serializable {
    /* Attributes */
    public static int MAX_SIZE = 1024;
    private final long timestamp;
    private final byte[] data;
    private final int size;
    private final float protocolVersion;
    private final byte[] hash;

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
        this.size = Long.BYTES + this.data.length + Integer.BYTES + Float.BYTES;

        byte[] dataBytes = getDataBytes(this.timestamp, this.data, this.size, this.protocolVersion);

        if (dataBytes == null) {
            // TODO: ERROR
        }

        this.hash = Hash.calculateSHA3512RIPEMD160(dataBytes);
    }

    /* Methods */
    private static byte[] getDataBytes(long timestamp, byte[] data, int size, float protocolVersion) {
        byte[] protocolVersionBytes;
        byte[] timestampBytes;
        byte[] sizeBytes;

        try {
            protocolVersionBytes = Converters.floatToByteArray(protocolVersion);
            timestampBytes = Converters.longToByteArray(timestamp);
            sizeBytes = Converters.intToByteArray(size);
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
            // TODO: ERROR
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
