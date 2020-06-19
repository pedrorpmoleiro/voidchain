package pt.ipleiria.estg.dei.pi.voidchain.util;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.jcajce.provider.digest.SHA3;

public class Hash {
    /**
     * Calculates ripemd-160 hash of sha3-512 hash of given byte array.
     *
     * @param data the data (byte[])
     * @return the byte[]
     */
    public static byte[] calculateSHA3512RIPEMD160(byte[] data) {
        SHA3.Digest512 sha3_512 = new SHA3.Digest512();
        RIPEMD160.Digest ripemd160 = new RIPEMD160.Digest();

        return ripemd160.digest(sha3_512.digest(data));
    }
}
