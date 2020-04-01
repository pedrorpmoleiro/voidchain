package voidchain;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Deprecated
public class Util {
    // https://javadeveloperzone.com/java-basic/java-convert-long-to-byte-array/#2_long_to_byte_array
    public static byte[] longToByteArray(final long i) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeLong(i);
        dos.flush();
        return bos.toByteArray();
    }

    public static long convertByteArrayToLong(byte[] longBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(longBytes);
        byteBuffer.flip();
        return byteBuffer.getLong();
    }

    // https://javadeveloperzone.com/java-basic/java-convert-int-to-byte-array/
    public static byte[] intToByteArray(final int i) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(i);
        dos.flush();
        return bos.toByteArray();
    }

    public static int convertByteArrayToInt(byte[] intBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(intBytes);
        return byteBuffer.getInt();
    }
}
