package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

/**
 * The transaction contains data of the operations performed by the replicas.
 * In the 'transactions' section of a block, transactions get recorded.
 */
public class Transaction {
    /* Attributes */
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

        SHA3.Digest512 sha3_512 = new SHA3.Digest512();
        RIPEMD160.Digest ripemd160 = new RIPEMD160.Digest();

        this.hash = sha3_512.digest(auxBytes);
        this.hash = ripemd160.digest(this.hash);
    }

    /* Methods */

    /* Getters */

    /**
     * Gets timestamp of when a transaction was created.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the data of a transaction.
     *
     * @return the byte [ ]
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the size of a transaction (in MB).
     *
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the hash of a transaction ( SHA256(TX) ).
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
