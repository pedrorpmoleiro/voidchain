package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Random;

/**
 * The block header is a section of a block.
 * The hash of the block header is what IDs a block in the blockchain
 */
public class BlockHeader {
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
        this.nonce = new Random().nextInt();
    }

    /* Methods */

    /* Getters */

    /**
     * Gets the time the block was created
     *
     * @return the timestamp (long)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the hash of the last block in the chain.
     * Or in other words, it gets the blocks parent.
     *
     * @return the previous block hash/ parent hash (byte[])
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
     * Gets the size of the block
     *
     * @return the size (int)
     */
    public int getSize() {
        return Long.SIZE + previousBlockHash.length + Float.SIZE + Integer.SIZE;
    }

    /**
     * Gets the data/transactions that are stored in the block
     *
     * @return the data (byte[])
     */
    public byte[] getData() {
        var aux = this.protocolVersion + this.timestamp + Base64.toBase64String(this.previousBlockHash) + this.nonce;

        return aux.getBytes(StandardCharsets.UTF_8);
    }
}
