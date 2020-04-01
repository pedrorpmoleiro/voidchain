package voidchain.blockchain;

import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Random;

/**
 * The type Block header.
 */
public class BlockHeader {
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

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get previous block hash byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getPreviousBlockHash() {
        return previousBlockHash;
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version
     */
    public float getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Gets nonce.
     *
     * @return the nonce
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public int getSize() {
        return Long.SIZE + previousBlockHash.length + Float.SIZE + Integer.SIZE;
    }

    /**
     * Get data byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getData() {
        var aux = this.protocolVersion + this.timestamp + Base64.toBase64String(this.previousBlockHash) + this.nonce;

        return aux.getBytes(StandardCharsets.UTF_8);
    }
}
