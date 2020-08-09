package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import org.bouncycastle.util.encoders.Base64;
import pt.ipleiria.estg.dei.pi.voidchain.util.Hash;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class represents the same as the block class but without any transactions.
 * It is used in the case that a client requests block data but doesn't require the transactions contained in the block.
 */
public class BlockNoTransactions implements Serializable {
    /* Attributes */
    private final BlockHeader blockHeader;
    private final int transactionCounter;
    private final int blockHeight;
    private final int size;

    /* Constructors */

    /**
     * Instantiates a new Block no transactions.
     *
     * @param blockHeader        the block header
     * @param transactionCounter the transaction counter
     * @param blockHeight        the block height
     * @param size               the size
     */
    protected BlockNoTransactions(BlockHeader blockHeader, int transactionCounter, int blockHeight, int size) {
        this.blockHeader = blockHeader;
        this.transactionCounter = transactionCounter;
        this.blockHeight = blockHeight;
        this.size = size;
    }

    /* Methods */

    /**
     * Gets the number of transactions inside a block.
     *
     * @return the transaction counter
     */
    public int getTransactionCounter() {
        return this.transactionCounter;
    }

    /**
     * Gets block height.
     *
     * @return the block height
     */
    public int getBlockHeight() {
        return this.blockHeight;
    }

    /**
     * Gets the transactions merkle tree root, from the block header.
     *
     * @return the merkle tree root
     */
    public byte[] getMerkleRoot() {
        return this.blockHeader.merkleRoot;
    }

    /**
     * Gets the Epoch time the block was created, from the block header
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return this.blockHeader.timestamp;
    }

    /**
     * Gets the hash of the previous block in the chain, from the block header.
     *
     * @return the previous block hash
     */
    public byte[] getPreviousBlockHash() {
        return this.blockHeader.previousBlockHash;
    }

    /**
     * Gets protocol version, from the block header.
     *
     * @return the protocol version
     */
    public String getProtocolVersion() {
        return this.blockHeader.protocolVersion;
    }

    /**
     * Gets the size of the block, including transactions.
     *
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * Calculates the hash of the block.
     * To calculate the hash of a block, we double hash it's header (block header).
     * <br>
     * SHA3_512(RIPEMD160(blockHeader))
     * <br>
     * Will return byte[0] if error occurred while calculating hash.
     *
     * @return the block hash
     */
    public byte[] getHash() {
        byte[] blockHeaderData = this.blockHeader.getData();
        byte[] aux = new byte[0];

        if (Arrays.equals(blockHeaderData, aux)) {
            return aux;
        }

        return Hash.calculateSHA3512RIPEMD160(blockHeaderData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockNoTransactions that = (BlockNoTransactions) o;
        return transactionCounter == that.transactionCounter &&
                blockHeight == that.blockHeight &&
                size == that.size &&
                blockHeader.equals(that.blockHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockHeader, transactionCounter, blockHeight, size);
    }

    @Override
    public String toString() {
        return "Block: {" + System.lineSeparator() +
                this.blockHeader + System.lineSeparator() +
                "size: " + this.getSize() + System.lineSeparator() +
                "transaction counter: " + this.transactionCounter + System.lineSeparator() +
                "block height: " + this.blockHeight + System.lineSeparator() +
                "hash: " + Base64.toBase64String(this.getHash()) + System.lineSeparator() +
                "}";
    }
}
