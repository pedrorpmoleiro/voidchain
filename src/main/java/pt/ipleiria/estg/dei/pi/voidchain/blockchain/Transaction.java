package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import bftsmart.reconfiguration.util.TOMConfiguration;

import bitcoinj.Base58;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;
import pt.ipleiria.estg.dei.pi.voidchain.util.Hash;
import pt.ipleiria.estg.dei.pi.voidchain.util.Keys;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * The transaction contains data of the operations performed by the replicas.
 * In the 'transactions' section of a block, transactions get recorded.
 * The structure of a transaction is: timestamp (when it was created), its size
 * , the version of the protocol when the transaction was created and the hash of the transaction (transaction ID).
 */
public class Transaction implements Serializable {
    /* Attributes */
    private static final transient Logger logger = LoggerFactory.getLogger(Transaction.class.getName());

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
     * @throws SignatureException       signature exception will be thrown if error occurs while signing the transaction data
     * @throws NoSuchAlgorithmException no such algorithm exception will be thrown if algorithm for signing the transaction
     * @throws InvalidKeyException      invalid key exception will be thrown if private key is invalid
     */
    public Transaction(byte[] data, String protocolVersion, long timestamp, TOMConfiguration smartConf)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        this.timestamp = timestamp;
        this.data = data;
        this.protocolVersion = protocolVersion;

        Signature signature = Signature.getInstance(smartConf.getSignatureAlgorithm());
        signature.initSign(smartConf.getPrivateKey());
        signature.update(data);
        this.signature = signature.sign();

        int size = Long.BYTES + data.length + Integer.BYTES + Float.BYTES + this.signature.length;
        int transactionMaxSize = Configuration.getInstance().getTransactionMaxSize();
        if (size > transactionMaxSize)
            throw new IllegalArgumentException("Transaction size is " + size + " but max transaction size is "
                    + transactionMaxSize);
    }

    /**
     * Instantiates a new Transaction.
     *
     * @param data            the data
     * @param protocolVersion the protocol version
     * @param timestamp       the timestamp
     * @param signature       the signature of data
     * @throws IllegalArgumentException illegal argument exception will be thrown if transaction size exceeds max transaction size
     */
    public Transaction(byte[] data, String protocolVersion, long timestamp, byte[] signature) {
        this.timestamp = timestamp;
        this.data = data;
        this.protocolVersion = protocolVersion;
        this.signature = signature;

        int size = Long.BYTES + data.length + Integer.BYTES + Float.BYTES + this.signature.length;
        int transactionMaxSize = Configuration.getInstance().getTransactionMaxSize();
        if (size > transactionMaxSize)
            throw new IllegalArgumentException("Transaction size is " + size + " but max transaction size is "
                    + transactionMaxSize);
    }

    /* Methods */

    /**
     * Calculates all the attributes in byte array format.
     * <br>
     * Will return byte[0] if an error occurs.
     *
     * @return the transaction bytes
     */
    public byte[] getBytes() {
        byte[] protocolVersionBytes = protocolVersion.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes;

        try {
            timestampBytes = Converters.longToByteArray(timestamp);
        } catch (IOException e) {
            logger.error("Error converting timestamp [" + timestamp + "] into byte array");
            return new byte[0];
        }

        int size = protocolVersionBytes.length + timestampBytes.length + data.length + this.signature.length;
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
        for (byte b : data) {
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
     * Verifies the signature of the transaction.
     *
     * @param pubKey the public key to test
     * @return true if the transaction belongs to the owner of the key, false other wise
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws NoSuchProviderException  the no such provider exception
     * @throws InvalidKeySpecException  the invalid key spec exception
     * @throws InvalidKeyException      the invalid key exception
     * @throws SignatureException       the signature exception
     */
    public boolean verifySignature(byte[] pubKey) throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidKeySpecException, InvalidKeyException, SignatureException, IOException {

        TOMConfiguration tomConf = new TOMConfiguration(-100, Configuration.CONFIG_DIR, null);
        String signatureAlgorithm = tomConf.getSignatureAlgorithm();

        Signature signature = Signature.getInstance(signatureAlgorithm);
        signature.initVerify(Keys.getPubKey(pubKey, tomConf));
        signature.update(this.data);

        return signature.verify(this.signature);
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
        byte[] dataBytes = this.getBytes();
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
                "signature: " + Base58.encode(this.signature) + System.lineSeparator() +
                "data: " + Base58.encode(this.data) + System.lineSeparator() +
                "size: " + this.getSize() + System.lineSeparator() +
                "protocol version: " + this.protocolVersion + System.lineSeparator() +
                "hash: " + Base58.encode(this.getHash()) + System.lineSeparator() +
                "}" + System.lineSeparator();
    }
}
