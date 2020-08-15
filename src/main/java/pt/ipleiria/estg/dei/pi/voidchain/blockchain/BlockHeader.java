package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;

import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

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
     * The merkle root of the block.
     */
    protected final byte[] merkleRoot;

    private final byte[] nonce; // much power, unseen. nonce is sith

    private static final transient Logger logger = LoggerFactory.getLogger(BlockHeader.class);

    /* Constructors */

    /**
     * Instantiates a new Block header.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     * @param timestamp         the timestamp
     * @param nonce             the nonce
     * @param merkleRoot        the merkle root
     */
    public BlockHeader(byte[] previousBlockHash, String protocolVersion, long timestamp, byte[] nonce,
                       byte[] merkleRoot) {
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
     * @return the size
     */
    public int getSize() {
        return Long.BYTES + Float.BYTES + this.previousBlockHash.length + this.nonce.length + this.merkleRoot.length;
    }

    /**
     * Calculates all the attributes in byte array format.
     * <p>
     * Will return byte[0] if an error occurs.
     *
     * @return the data
     */
    public byte[] getData() {
        byte[] protocolVersionBytes = this.protocolVersion.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes;

        try {
            timestampBytes = Converters.longToByteArray(this.timestamp);
        } catch (IOException e) {
            logger.error("Error converting timestamp [" + this.timestamp + "] into byte array");
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

    protected BlockHeader clone() {
        return new BlockHeader(this.previousBlockHash, this.protocolVersion, this.timestamp,
                this.nonce, this.merkleRoot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockHeader that = (BlockHeader) o;
        return Arrays.equals(previousBlockHash, that.previousBlockHash) &&
                protocolVersion.equals(that.protocolVersion) &&
                Arrays.equals(merkleRoot, that.merkleRoot);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(protocolVersion);
        result = 31 * result + Arrays.hashCode(previousBlockHash);
        result = 31 * result + Arrays.hashCode(merkleRoot);
        return result;
    }

    @Override
    public String toString() {
        return "Block Header: {" + System.lineSeparator() +
                "timestamp: " + timestamp + System.lineSeparator() +
                "previous block hash: " + Base64.toBase64String(previousBlockHash) + System.lineSeparator() +
                "protocol version: " + protocolVersion + System.lineSeparator() +
                "merkle root: " + Base64.toBase64String(merkleRoot) + System.lineSeparator() +
                "}";
    }
}
