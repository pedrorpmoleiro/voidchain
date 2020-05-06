package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.util.Converters;

import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * The block header is a section of a block.
 * The hash of the block header is what IDs a block in the blockchain. It is the block ID.
 * The structure of the header is: a timestamp, parents block (the block header) hash, the version of the protocol when the block was created,
 *  a nonce (random byte array) and the root of the merkle tree (which resumes all the transactions stored in the block).
 */
public class BlockHeader implements Serializable {
    /* Attributes */
    protected final long timestamp;
    protected final byte[] previousBlockHash;
    protected final String protocolVersion;
    protected final byte[] nonce;
    protected byte[] merkleRoot;

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

    // FOR USE WITH CLONE
    // TODO: REMOVE ?
    private BlockHeader (byte[] previousBlockHash, String protocolVersion, long timestamp, byte[] nonce, byte[] merkleRoot) {
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
     *
     * @return the data (byte[])
     */
    public byte[] getData() {
        byte[] protocolVersionBytes = this.protocolVersion.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes;

        try {
            timestampBytes = Converters.longToByteArray(this.timestamp);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
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

        if (i != size) {
            System.out.println("THIS SHOULDN'T RUN");

            return null;
        }

        return dataBytes;
    }

    /**
     * Creates a clone/copy of the current (instance) block header.
     *
     * @return a clone/copy of the block header
     */
    // TODO: REMOVE ?
    protected BlockHeader clone() {
        return new BlockHeader(this.previousBlockHash, this.protocolVersion, this.timestamp,
                this.nonce, this.merkleRoot);
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
