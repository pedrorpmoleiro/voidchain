package pt.ipleiria.estg.dei.pi.voidchain.replica;

import bftsmart.communication.SystemMessage;

import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

public class ReplicaMessage extends SystemMessage implements Externalizable, Comparable, Cloneable {

    private ReplicaMessageType type;
    private byte[] content = null;

    // ? Internal
    private transient int id; // ID for this message. It should be unique
    public transient long timestamp; // timestamp to be used by the application

    public transient long receptionTime; // the reception time of this message (nanoseconds)
    public transient long receptionTimestamp; // the reception timestamp of this message (miliseconds)

    // ?
    //the bytes received from the client and its MAC and signature
    public transient byte[] serializedMessage = null;
    public transient byte[] serializedMessageSignature = null;
    public transient byte[] serializedMessageMAC = null;

    public transient ReplicaMessage reply = null;
    public transient boolean alreadyProposed = false;

    public ReplicaMessage() {
    }

    public ReplicaMessage(int sender, ReplicaMessageType type, byte[] content) {
        super(sender);
        buildId();
        this.type = type;
        this.content = content;
    }

    // FROM TOMMessage
    private void buildId() {
        int hash = 5;
        hash = 59 * hash + this.getSender();
        this.id = hash;
    }

    public void wExternal(DataOutput out) throws IOException {
        out.writeInt(sender);
        out.writeInt(type.toInt());

        if (content == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(content.length);
            out.write(content);
        }
    }

    public void rExternal(DataInput in) throws IOException, ClassNotFoundException {
        sender = in.readInt();
        type = ReplicaMessageType.fromInt(in.readInt());

        int toRead = in.readInt();
        if (toRead != -1) {
            content = new byte[toRead];
            in.readFully(content);
        }

        buildId();
    }

    public static int getSenderFromId(int id) {
        return id >>> 20;
    }

    public static byte[] messageToBytes(ReplicaMessage rm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try{
            rm.wExternal(dos);
            dos.flush();
        }catch(Exception e) {
        }
        return baos.toByteArray();
    }

    public static ReplicaMessage bytesToMessage(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);

        ReplicaMessage rm = new ReplicaMessage();

        try{
            rm.rExternal(dis);
        }catch(Exception e) {
            LoggerFactory.getLogger(ReplicaMessage.class).error("Failed to deserialize ReplicaMessage",e);
            return null;
        }

        return rm;
    }

    // GETS
    public ReplicaMessageType getType() {
        return this.type;
    }

    public byte[] getContent() {
        return this.content;
    }

    public int getId() {
        return this.id;
    }

    // COMPARABLE
    @Override
    public int compareTo(Object o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        ReplicaMessage rm = (ReplicaMessage) o;

        if (this.equals(rm)) return EQUAL;

        if (this.getSender() < rm.getSender()) return BEFORE;

        if (this.getSender() > rm.getSender()) return AFTER;

        return EQUAL;
    }

    // CLONEABLE
    @Override
    protected Object clone() throws CloneNotSupportedException {
        ReplicaMessage clone = new ReplicaMessage(this.sender, this.type, this.content);

        clone.alreadyProposed = this.alreadyProposed;
        clone.authenticated = this.authenticated;
        clone.receptionTime = this.receptionTime;
        clone.receptionTimestamp = this.receptionTimestamp;
        clone.reply = this.reply;
        clone.serializedMessage = this.serializedMessage;
        clone.serializedMessageMAC = this.serializedMessageMAC;
        clone.serializedMessageSignature = this.serializedMessageSignature;
        clone.timestamp = this.timestamp;

        return clone;
    }

    @Override
    public boolean equals(Object o) {
        /*if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplicaMessage that = (ReplicaMessage) o;
        return id == that.id &&
                timestamp == that.timestamp &&
                receptionTime == that.receptionTime &&
                receptionTimestamp == that.receptionTimestamp &&
                alreadyProposed == that.alreadyProposed &&
                type == that.type &&
                Arrays.equals(content, that.content) &&
                Arrays.equals(serializedMessage, that.serializedMessage) &&
                Arrays.equals(serializedMessageSignature, that.serializedMessageSignature) &&
                Arrays.equals(serializedMessageMAC, that.serializedMessageMAC) &&
                Objects.equals(reply, that.reply);*/

        if (o == null) return false;

        if (!(o instanceof ReplicaMessage)) return false;

        return ((ReplicaMessage) o).getSender() == sender;
    }

    @Override
    public int hashCode() {
        /*int result = Objects.hash(type, id, timestamp, receptionTime, receptionTimestamp, reply, alreadyProposed);
        result = 31 * result + Arrays.hashCode(content);
        result = 31 * result + Arrays.hashCode(serializedMessage);
        result = 31 * result + Arrays.hashCode(serializedMessageSignature);
        result = 31 * result + Arrays.hashCode(serializedMessageMAC);
        return result;*/

        return this.id;
    }

    @Override
    public String toString() {
        return "ReplicaMessage: {" + System.lineSeparator() +
                "type=" + this.type + System.lineSeparator() +
                "content=" + Arrays.toString(this.content) + System.lineSeparator() +
                "id=" + this.id + System.lineSeparator() +
                "timestamp=" + this.timestamp + System.lineSeparator() +
                "receptionTime=" + this.receptionTime + System.lineSeparator() +
                "receptionTimestamp=" + this.receptionTimestamp + System.lineSeparator() +
                //"serializedMessage=" + Arrays.toString(this.serializedMessage) + System.lineSeparator() +
                //"serializedMessageSignature=" + Arrays.toString(this.serializedMessageSignature) + System.lineSeparator() +
                //"serializedMessageMAC=" + Arrays.toString(this.serializedMessageMAC) + System.lineSeparator() +
                "reply=" + this.reply + System.lineSeparator() +
                "alreadyProposed=" + this.alreadyProposed + System.lineSeparator() +
                '}';
    }
}
