package voidchain.blockchain;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

/**
 * The type Transaction.
 */
public class Transaction {
    private long timestamp;
    private byte[] data;
    private int size;
    private float protocolVersion;
    private byte[] hash;

    /**
     * Instantiates a new Transaction.
     *
     * @param data            the data
     * @param protocolVersion the protocol version
     */
    public Transaction(byte[] data, float protocolVersion) {
        this.timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        this.data = data;
        this.protocolVersion = protocolVersion;
        this.size = Long.SIZE + this.data.length + Integer.SIZE + Float.SIZE;

        // TODO: Alter for performance
        var aux = this.protocolVersion + this.timestamp + this.size + Base64.toBase64String(this.data);
        var auxBytes = aux.getBytes(StandardCharsets.UTF_8);

        // TODO: SHA3 256
        SHA256.Digest sha256 = new SHA256.Digest();
        RIPEMD160.Digest ripemd160 = new RIPEMD160.Digest();

        this.hash = sha256.digest(auxBytes);
        this.hash = ripemd160.digest(this.hash);
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
     * Get data byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * Get hash byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Gets protocol version.
     *
     * @return the protocol version
     */
    public float getProtocolVersion() {
        return protocolVersion;
    }
}
