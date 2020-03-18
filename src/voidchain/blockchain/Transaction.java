package voidchain.blockchain;

import java.sql.Timestamp;

public class Transaction {
    private String message;
    private long timestamp;

    public Transaction(String message) {
        this.message = message;
        this.timestamp = new Timestamp(System.currentTimeMillis()).getTime();
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
