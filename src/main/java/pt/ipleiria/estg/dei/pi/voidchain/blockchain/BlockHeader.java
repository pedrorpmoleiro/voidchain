package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.util.encoders.Base64;
import pt.ipleiria.estg.dei.pi.voidchain.Util;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * The block header is a section of a block.
 * The hash of the block header is what IDs a block in the blockchain
 */
public class BlockHeader implements Serializable {
    /* Attributes */
    private long timestamp;
    private byte[] previousBlockHash;
    private float protocolVersion;
    private int nonce;

    /**
     * Instantiates a new Block header.
     *
     * @param previousBlockHash the previous block hash
     * @param protocolVersion   the protocol version
     */
    public BlockHeader(byte[] previousBlockHash, float protocolVersion) {
        this.previousBlockHash = previousBlockHash;
        this.protocolVersion = protocolVersion;
        this.timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        // TODO: IMPROVE SECURITY
        this.nonce = new Random().nextInt();
    }


    public BlockHeader(byte[] previousBlockHash, float protocolVersion, long timestamp, int nonce) {
        this.previousBlockHash = previousBlockHash;
        this.protocolVersion = protocolVersion;
        this.timestamp = timestamp;
        this.nonce = nonce;
    }

    /* Methods */
    /* Getters */
    /**
     * Gets the Epoch time the block was created
     *
     * @return the timestamp (long)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the hash of the previous block in the chain.
     *
     * @return the previous block hash (byte[])
     */
    public byte[] getPreviousBlockHash() {
        return previousBlockHash;
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version (float)
     */
    public float getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Gets nonce. A random int
     *
     * @return the nonce (int)
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * Calculates the size of the block header in bytes.
     *
     * @return the size (int)
     */
    public int getSize() {
        return Long.SIZE + previousBlockHash.length + Float.SIZE + Integer.SIZE;
    }

    /**
     * Calculates all the attributes in byte array format.
     *
     * @return the data (byte[])
     */
    public byte[] getData() {
        byte[] protocolVersionBytes;
        byte[] timestampBytes;
        byte[] nonceBytes;

        try {
            protocolVersionBytes = Util.floatToByteArray(this.protocolVersion);
            timestampBytes = Util.longToByteArray(this.timestamp);
            nonceBytes = Util.intToByteArray(this.nonce);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }

        int size = protocolVersionBytes.length + timestampBytes.length + this.previousBlockHash.length + nonceBytes.length;
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
        for (byte b : nonceBytes) {
            dataBytes[i] = b;
            i++;
        }

        if (i != size) {
            System.out.println("THIS SHOULDN'T RUN");

            return null;
        }

        return dataBytes;
    }

    @Override
    public String toString() {
        return "BlockHeader{" +
                "timestamp=" + timestamp +
                ", previousBlockHash=" + Base64.toBase64String(previousBlockHash) +
                ", protocolVersion=" + protocolVersion +
                ", nonce=" + nonce +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BlockHeader that = (BlockHeader) o;

        // return Arrays.equals(Util.calculateHash(getData()), Util.calculateHash(that.getData()));
        return timestamp == that.timestamp &&
                Float.compare(that.protocolVersion, protocolVersion) == 0 &&
                nonce == that.nonce &&
                Arrays.equals(previousBlockHash, that.previousBlockHash);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(timestamp, protocolVersion, nonce);
        result = 31 * result + Arrays.hashCode(previousBlockHash);

        // return Util.convertByteArrayToInt(Util.calculateHash(getData()));
        return result;
    }
}
