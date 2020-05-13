package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;

import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * The block header is a section of a block.
 * The hash of the block header is what IDs a block in the blockchain. It is the block ID.
 * The structure of the header is: a timestamp, parents block (the block header) hash, the version of the protocol when the block was created,
 * a nonce (random byte array) and the root of the merkle tree (which resumes all the transactions stored in the block).
 */
public class BlockHeader implements Serializable {
    /* Attributes */
    /**
     * The Timestamp.
     */
    protected final long timestamp;
    /**
     * The Previous block hash.
     */
    protected final byte[] previousBlockHash;
    /**
     * The Protocol version.
     */
    protected final String protocolVersion;
    /**
     * The block nonce.
     */
    protected final byte[] nonce;
    /**
     * The merkle root of the block.
     */
    protected byte[] merkleRoot;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * Instantiates a new Block header.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param timestamp         the timestamp
     * @param nonce             the nonce
     */
    public BlockHeader(byte[] previousBlockHash, String protocolVersion, long timestamp, byte[] nonce) {
        this.previousBlockHash = previousBlockHash;
        this.protocolVersion = protocolVersion;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.merkleRoot = new byte[0];
    }

    /**
     * Instantiates a new Block header.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param timestamp         the timestamp
     * @param nonce             the nonce
     * @param merkleRoot        the merkle root
     */
    public BlockHeader(byte[] previousBlockHash, String protocolVersion, long timestamp, byte[] nonce, byte[] merkleRoot) {
        this.previousBlockHash = previousBlockHash;
        this.protocolVersion = protocolVersion;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.merkleRoot = merkleRoot;
    }

    /* Methods */
    /* Getters */

    /**
     * Calculates the size of the block header in bytes.
     *
     * @return the size (int)
     */
    public int getSize() {
        return Long.BYTES + Float.BYTES + this.previousBlockHash.length + this.nonce.length + this.merkleRoot.length;
    }

    /**
     * Calculates all the attributes in byte array format.
     * <p>
     * Will return byte[0] if an error occurs.
     *
     * @return the data (byte[])
     */
    public byte[] getData() {
        byte[] protocolVersionBytes = this.protocolVersion.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes = new byte[0];

        try {
            timestampBytes = Converters.longToByteArray(this.timestamp);
        } catch (IOException e) {
            this.logger.error("Error converting timestamp [" + this.timestamp + "] into byte array");
            return new byte[0];
        }

        int size = protocolVersionBytes.length + timestampBytes.length + this.previousBlockHash.length +
                this.nonce.length + this.merkleRoot.length;
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
        for (byte b : this.nonce) {
            dataBytes[i] = b;
            i++;
        }
        for (byte b : this.merkleRoot) {
            dataBytes[i] = b;
            i++;
        }

        if (i != size)
            // THIS SHOULDN'T RUN
            return new byte[0];

        return dataBytes;
    }

    @Override
    public String toString() {
        return "Block Header: {" + System.lineSeparator() +
                "timestamp: " + timestamp + System.lineSeparator() +
                "previous block hash: " + Base64.toBase64String(previousBlockHash) + System.lineSeparator() +
                "protocol version: " + protocolVersion + System.lineSeparator() +
                "nonce: " + Base64.toBase64String(nonce) + System.lineSeparator() +
                "merkle root: " + Base64.toBase64String(merkleRoot) + System.lineSeparator() +
                "}";
    }
}
