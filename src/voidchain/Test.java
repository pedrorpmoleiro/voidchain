package voidchain;

import org.bouncycastle.jcajce.provider.digest.MD5;
import org.bouncycastle.util.encoders.Base64;

public class Test {
    public static void main(String[] args) {
        MD5.Digest md5Digest = new MD5.Digest();

        System.out.println(Base64.toBase64String(md5Digest.digest("Hello World".getBytes())));
    }
}
