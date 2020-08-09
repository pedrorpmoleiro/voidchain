package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import bftsmart.reconfiguration.util.TOMConfiguration;

import org.bouncycastle.util.encoders.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;
import pt.ipleiria.estg.dei.pi.voidchain.util.Hash;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * The transaction contains data of the operations performed by the replicas.
 * In the 'transactions' section of a block, transactions get recorded.
 * The structure of a transaction is: timestamp (when it was created), its size
 * , the version of the protocol when the transaction was created and the hash of the transaction (transaction ID).
 */
public class Transaction implements Serializable {
    /* Attributes */
    private static final transient Logger logger = LoggerFactory.getLogger(Transaction.class.getName());

    /*
     * TODO
     *  NONCE
     */

    /**
     * The constant LIST_COMPARATOR provides a comparator to order transaction lists by timestamp.
     */
    public static final transient Comparator<Transaction> LIST_COMPARATOR = (o1, o2) ->
            Long.compare(o2.getTimestamp(), o1.getTimestamp());
    /**
     * The constant MAP_COMPARATOR provides a comparator to order a list of map entries of transaction hashes and
     * transactions by timestamp.
     */
    public static final transient Comparator<Map.Entry<byte[], Transaction>> MAP_COMPARATOR = (o1, o2) ->
            Long.compare(o2.getValue().getTimestamp(), o1.getValue().getTimestamp());

    private final long timestamp;
    private final byte[] data;
    private final String protocolVersion;
    private final byte[] signature;

    /* Constructors */

    /**
     * Instantiates a new Transaction.
     *
     * @param data            the data
     * @param protocolVersion the protocol version
     * @param timestamp       the timestamp
     * @param smartConf       the instance of bft-smart configuration
     * @throws IllegalArgumentException illegal argument exception will be thrown if transaction size exceeds max
     *                                  value of transaction
     * @throws SignatureException       signature exception will be thrown if error occurs while signing the transaction
     *                                  data
     * @throws NoSuchAlgorithmException no such algorithm exception will be thrown if algorithm for signing the
     *                                  transaction
     * @throws InvalidKeyException      invalid key exception will be thrown if private key is invalid
     */
    public Transaction(byte[] data, String protocolVersion, long timestamp, TOMConfiguration smartConf)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        int size = Long.BYTES + data.length + Integer.BYTES + Float.BYTES;

        int transactionMaxSize = Configuration.getInstance().getTransactionMaxSize();
        if (size > transactionMaxSize)
            throw new IllegalArgumentException("Transaction size is " + size + " but max transaction size is "
                    + transactionMaxSize);

        this.timestamp = timestamp;
        this.data = data;
        this.protocolVersion = protocolVersion;

        Signature signature = Signature.getInstance(smartConf.getSignatureAlgorithm());
        signature.initSign(smartConf.getPrivateKey());
        signature.update(getBytesNoSignatureStatic(data, protocolVersion, timestamp));
        this.signature = signature.sign();
    }

    /* Methods */

    private static byte[] getBytesNoSignatureStatic(byte[] data, String protocolVersion, long timestamp) {
        byte[] protocolVersionBytes = protocolVersion.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes;

        try {
            timestampBytes = Converters.longToByteArray(timestamp);
        } catch (IOException e) {
            logger.error("Error converting timestamp [" + timestamp + "] into byte array");
            return new byte[0];
        }

        int sizeAux = protocolVersionBytes.length + timestampBytes.length + data.length;
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
        for (byte b : data) {
            dataBytes[i] = b;
            i++;
        }

        if (i != sizeAux)
            // THIS SHOULDN'T RUN
            return new byte[0];

        return dataBytes;
    }

    /**
     * Calculates all the attributes in byte array format, excluding the transaction signature.
     * <br>
     * The output of this method is the input for the calculation of the signature.
     * <br>
     * Will return byte[0] if an error occurs.
     *
     * @return the transaction bytes without the signature
     */
    public byte[] getBytesNoSignature() {
        return getBytesNoSignatureStatic(this.data, this.protocolVersion, this.timestamp);
    }

    /**
     * Calculates all the attributes in byte array format.
     * <br>
     * Will return byte[0] if an error occurs.
     *
     * @return the transaction bytes
     */
    public byte[] getAllBytes() {
        byte[] aux = getBytesNoSignatureStatic(this.data, this.protocolVersion, this.timestamp);
        byte[] byte0 = new byte[0];

        if (Arrays.equals(byte0, aux))
            return byte0;

        final int size = aux.length + this.signature.length;
        byte[] dataBytes = new byte[size];
        int i = 0;

        for (byte b : aux) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : this.signature) {
            dataBytes[i] = b;
            i++;
        }

        if (i != size)
            // THIS SHOULDN'T RUN
            return new byte[0];

        return dataBytes;
    }

    /* Getters */

    /**
     * Gets Epoch time of when a transaction was created.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the data of a transaction.
     *
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the owner's signature of the transaction.
     *
     * @return the signature of the transaction
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Calculates the size of a transaction in Bytes.
     *
     * @return the size
     */
    public int getSize() {
        return Long.BYTES + this.data.length + this.signature.length +
                this.protocolVersion.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Calculates the hash of the transaction.
     * To calculate the hash of a transaction, we double hash all it's attributes.
     * <br>
     * SHA3_512(RIPEMD160(transaction))
     * <br>
     * Will return byte[0] if error occurred while calculating hash.
     *
     * @return the block hash
     */
    public byte[] getHash() {
        byte[] dataBytes = this.getAllBytes();
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
                "owner: " + Base64.toBase64String(this.signature) + System.lineSeparator() +
                "data: " + Base64.toBase64String(this.data) + System.lineSeparator() +
                "size: " + this.getSize() + System.lineSeparator() +
                "protocol version: " + this.protocolVersion + System.lineSeparator() +
                "hash: " + Base64.toBase64String(this.getHash()) + System.lineSeparator() +
                "}" + System.lineSeparator();
    }
}
