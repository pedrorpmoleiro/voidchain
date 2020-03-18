package voidchain.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class Block {
    private byte[] previousHash;
    private Set<Transaction> transactions;
    private byte[] hash;
    private long timestamp;

    public Block(byte[] previousHash) {
        this.previousHash = previousHash;
        this.transactions = new HashSet<>();

        this.timestamp = new Timestamp(System.currentTimeMillis()).getTime();

        this.updateHash();
    }

    public Block(byte[] previousHash, Set<Transaction> transactions) {
        this.previousHash = previousHash;
        this.transactions = new HashSet<>();
        this.transactions.addAll(transactions);

        this.timestamp = new Timestamp(System.currentTimeMillis()).getTime();

        this.updateHash();
    }

    private void updateHash() {
        String data = this.transactions.toString() + this.timestamp + Util.convertByteArrayToHexString(this.previousHash);

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            byte[] currentBytes = data.getBytes(StandardCharsets.UTF_8);

            this.hash = sha.digest(currentBytes);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        this.updateHash();
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getPreviousHash() {
        return previousHash;
    }
}
