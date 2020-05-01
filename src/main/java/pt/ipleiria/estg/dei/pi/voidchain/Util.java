package pt.ipleiria.estg.dei.pi.voidchain;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

/**
 * Class for useful methods
 */
public class Util {
    /**
     * Converts Long to byte array.
     *
     * @param l the long
     * @return the byte[]
     * @throws IOException the io exception
     */
// https://javadeveloperzone.com/java-basic/java-convert-long-to-byte-array/#2_long_to_byte_array
    public static byte[] longToByteArray(final long l) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeLong(l);
        dos.flush();
        return bos.toByteArray();
    }

    /**
     * Converts byte array to long long.
     *
     * @param longBytes the long bytes
     * @return the long
     */
    public static long convertByteArrayToLong(byte[] longBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(longBytes);
        byteBuffer.flip();
        return byteBuffer.getLong();
    }

    /**
     * Converts Int to byte array.
     *
     * @param i the integer
     * @return the byte[]
     * @throws IOException the io exception
     */
// https://javadeveloperzone.com/java-basic/java-convert-int-to-byte-array/
    public static byte[] intToByteArray(final int i) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(i);
        dos.flush();
        return bos.toByteArray();
    }

    /**
     * Convert byte array to int.
     *
     * @param intBytes the int bytes
     * @return the int
     */
    public static int convertByteArrayToInt(byte[] intBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(intBytes);
        return byteBuffer.getInt();
    }

    /**
     * Converts Float to byte array.
     *
     * @param f the float
     * @return the byte[]
     * @throws IOException the io exception
     */
    public static byte[] floatToByteArray(final float f) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeFloat(f);
        dos.flush();
        return bos.toByteArray();
    }

    /**
     * Convert byte array to float.
     *
     * @param floatBytes the float bytes
     * @return the float
     */
    public static float convertByteArrayToFloat(byte[] floatBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(floatBytes);
        byteBuffer.flip();
        return byteBuffer.getLong();
    }

    public static byte[] calculateHash(byte[] data) {
        SHA3.Digest512 sha3_512 = new SHA3.Digest512();
        RIPEMD160.Digest ripemd160 = new RIPEMD160.Digest();

        return ripemd160.digest(sha3_512.digest(data));
    }

    // https://medium.com/@vinayprabhu19/merkel-tree-in-java-b45093c8c6bd
    public static byte[] getMerkleRoot(Set<byte[]> transactionHashList) {
        return merkleTree(new ArrayList<>(transactionHashList)).get(0);
    }

    public static ArrayList<byte[]> merkleTree(ArrayList<byte[]> hashList) {
        if (hashList.size() == 1) {
            return hashList;
        }

        ArrayList<byte[]> parentList = new ArrayList<>();

        // Hash the leaf transaction pair to get parent transaction
        // for (int i = 0; i < hashList.size(); i+=2) {
        for (int i = 0; i < hashList.size() - 1; i+=2) {
            byte[] t1Hash = hashList.get(i);
            byte[] t2hash = hashList.get(i + 1);

            int sizeAux = t1Hash.length + t2hash.length;
            byte[] aux = new byte[sizeAux];
            int j = 0;

            for (byte b : t1Hash) {
                aux[j] = b;
                j++;
            }
            for (byte b : t2hash) {
                aux[j] = b;
                j++;
            }

            if (j != sizeAux) {
                // TODO: ERROR
                System.out.println("THIS SHOULDN'T RUN");
                return null;
            }

            parentList.add(calculateHash(aux));
        }

        // If odd number of transactions, add the last transaction again
        if (hashList.size() % 2 == 1)
            parentList.add(hashList.get(hashList.size() - 1));

        return merkleTree(parentList);
    }
}
