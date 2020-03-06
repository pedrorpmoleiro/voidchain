package pt.ipleiria.estg.dei.pi.voidchain.blockchain;

import java.sql.Timestamp;

public class Transaction {
    private String message;
    private long timestamp;

    public Transaction(String message) {
        this.message = message;
        this.timestamp = new Timestamp(System.currentTimeMillis()).getTime();
    }
}
