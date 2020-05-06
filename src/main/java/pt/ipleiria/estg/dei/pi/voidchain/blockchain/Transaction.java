package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;
import pt.ipleiria.estg.dei.pi.voidchain.util.Hash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * The transaction contains data of the operations performed by the replicas.
 * In the 'transactions' section of a block, transactions get recorded.
 * The structure of a transaction is: timestamp (when it was created), its size
 * , the version of the protocol when the transaction was created and the hash of the transaction (trasaction ID).
 */
public class Transaction implements Serializable {
    /* Attributes */
    private final Logger logger = LoggerFactory.getLogger(Transaction.class.getName());
    private final long timestamp;
    private final byte[] data;
    private final int size;
    private final String protocolVersion;

    /**
     * Instantiates a new Transaction.
     *
     * @param data            the data
     * @param protocolVersion the protocol version
     * @param timestamp       the timestamp
     */
    public Transaction(byte[] data, String protocolVersion, long timestamp) {
        this.size = Long.BYTES + data.length + Integer.BYTES + Float.BYTES;

        int transactionMaxSize = Configuration.getInstance().getTransactionMaxSize();
        if (this.size > transactionMaxSize) {
            throw new IllegalArgumentException("Transaction size is " + this.size + " but max transaction size is "
                    + transactionMaxSize);
        }

        this.timestamp = timestamp;
        this.data = data;
        this.protocolVersion = protocolVersion;
    }

    /* Methods */
    public byte[] getDataBytes() {
        byte[] protocolVersionBytes = this.protocolVersion.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes;
        byte[] sizeBytes;

        try {
            timestampBytes = Converters.longToByteArray(this.timestamp);
            sizeBytes = Converters.intToByteArray(this.size);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        int sizeAux = protocolVersionBytes.length + timestampBytes.length + sizeBytes.length + this.data.length;
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
        for (byte b : this.data) {
            dataBytes[i] = b;
            i++;
        }

        if (i != sizeAux) {
            // THIS SHOULDN'T RUN
            this.logger.error("Could not write all bytes to array");
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
        return Hash.calculateSHA3512RIPEMD160(this.getDataBytes());
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String toString() {
        return "Transaction: {" + System.lineSeparator() +
                "timestamp: " + timestamp + System.lineSeparator() +
                "data: " + Base64.toBase64String(data) + System.lineSeparator() +
                "size: " + size + System.lineSeparator() +
                "protocol version: " + protocolVersion + System.lineSeparator() +
                "hash: " + Base64.toBase64String(getHash()) + System.lineSeparator() +
                "}" + System.lineSeparator();
    }
}
