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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * The transaction contains data of the operations performed by the replicas.
 * In the 'transactions' section of a block, transactions get recorded.
 * The structure of a transaction is: timestamp (when it was created), its size
 * , the version of the protocol when the transaction was created and the hash of the transaction (trasaction ID).
 */
public class Transaction implements Serializable {
    /* Attributes */
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class.getName());

    /**
     * The constant LIST_COMPARATOR provides a comparator to order transaction lists by timestamp.
     */
    public static final Comparator<Transaction> LIST_COMPARATOR = (o1, o2) ->
            Long.compare(o2.getTimestamp(), o1.getTimestamp());
    /**
     * The constant MAP_COMPARATOR provides a comparator to order a list of map entries of transaction hashes and
     * transactions by timestamp.
     */
    public static final Comparator<Map.Entry<byte[], Transaction>> MAP_COMPARATOR = (o1, o2) ->
            Long.compare(o2.getValue().getTimestamp(), o1.getValue().getTimestamp());

    private final long timestamp;
    private final byte[] data;
    private final String protocolVersion;

    /**
     * Instantiates a new Transaction.
     *
     * @param data            the data
     * @param protocolVersion the protocol version
     * @param timestamp       the timestamp
     * @throws IllegalArgumentException illegal argument exception will be thrown if transaction size exceeds max value of transacion
     */
    public Transaction(byte[] data, String protocolVersion, long timestamp) {
        int size = Long.BYTES + data.length + Integer.BYTES + Float.BYTES;

        int transactionMaxSize = Configuration.getInstance().getTransactionMaxSize();
        if (size > transactionMaxSize)
            throw new IllegalArgumentException("Transaction size is " + size + " but max transaction size is "
                    + transactionMaxSize);

        this.timestamp = timestamp;
        this.data = data;
        this.protocolVersion = protocolVersion;
    }

    /* Methods */

    /**
     * Calculates all the attributes in byte array format.
     * <p>
     * Will return byte[0] if an error occurs.
     *
     * @return the data (byte[])
     */
    public byte[] getDataBytes() {
        byte[] protocolVersionBytes = this.protocolVersion.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes;

        try {
            timestampBytes = Converters.longToByteArray(this.timestamp);
        } catch (IOException e) {
            logger.error("Error converting timestamp [" + this.timestamp + "] into byte array");
            return new byte[0];
        }

        int sizeAux = protocolVersionBytes.length + timestampBytes.length + this.data.length;
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
        for (byte b : this.data) {
            dataBytes[i] = b;
            i++;
        }

        if (i != sizeAux)
            // THIS SHOULDN'T RUN
            return new byte[0];

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
     * Calculates the size of a transaction in Bytes.
     *
     * @return the size (int)
     */
    public int getSize() {
        return Long.BYTES + this.data.length + this.protocolVersion.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Calculates the hash of the transaction.
     * To calculate the hash of a transaction, we double hash all it's attributes.
     * <p>
     * SHA3_512(RIPEMD160(transaction))
     * <p>
     * Will return byte[0] if error occurred while calculating hash.
     *
     * @return the block hash (byte[])
     */
    public byte[] getHash() {
        byte[] dataBytes = this.getDataBytes();
        byte[] aux = new byte[0];

        if (Arrays.equals(dataBytes, aux))
            return aux;

        return Hash.calculateSHA3512RIPEMD160(dataBytes);
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version (String)
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String toString() {
        return "Transaction: {" + System.lineSeparator() +
                "timestamp: " + this.timestamp + System.lineSeparator() +
                "data: " + Base64.toBase64String(this.data) + System.lineSeparator() +
                "size: " + this.getSize() + System.lineSeparator() +
                "protocol version: " + this.protocolVersion + System.lineSeparator() +
                "hash: " + Base64.toBase64String(this.getHash()) + System.lineSeparator() +
                "}" + System.lineSeparator();
    }
}
