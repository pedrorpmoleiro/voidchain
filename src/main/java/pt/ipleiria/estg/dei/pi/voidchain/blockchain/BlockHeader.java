package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.Util;

import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.Serializable;

/**
 * The block header is a section of a block.
 * The hash of the block header is what IDs a block in the blockchain
 */
public class BlockHeader implements Serializable {
    /* Attributes */
    protected final long timestamp;
    protected final byte[] previousBlockHash;
    protected final float protocolVersion;
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
    public BlockHeader(byte[] previousBlockHash, float protocolVersion, long timestamp, byte[] nonce) {
        this.previousBlockHash = previousBlockHash;
        this.protocolVersion = protocolVersion;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.merkleRoot = new byte[0];
    }

    // FOR USE WITH CLONE
    private BlockHeader (byte[] previousBlockHash, float protocolVersion, long timestamp, byte[] nonce, byte[] merkleRoot) {
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
        return Long.SIZE + Float.SIZE + this.previousBlockHash.length + this.nonce.length + this.merkleRoot.length;
    }

    /**
     * Calculates all the attributes in byte array format.
     *
     * @return the data (byte[])
     */
    public byte[] getData() {
        byte[] protocolVersionBytes;
        byte[] timestampBytes;

        try {
            protocolVersionBytes = Util.floatToByteArray(this.protocolVersion);
            timestampBytes = Util.longToByteArray(this.timestamp);
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
