package pt.ipleiria.estg.dei.pi.voidchain.util;

import bftsmart.reconfiguration.util.TOMConfiguration;
import bftsmart.tom.util.ECDSAKeyPairGenerator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.TimeUnit;

public class Keys {
    protected static final Logger logger = LoggerFactory.getLogger(Keys.class);

    protected static final String SECRET = "Q2o3^TjE9OcrcZqG";

    /**
     * Generates public and private SSL/TLS keys according to 'system.config' configuration file.
     *
     * @param id the id of the client/replica
     */
    public static void generateSSLKey(int id) {
        try {
            Path keyDir = Paths.get(Configuration.CONFIG_DIR + "keysSSL_TLS");
            if (Files.notExists(keyDir))
                Files.createDirectories(keyDir);
        } catch (IOException e) {
            logger.error("Error occurred while creating directory for key file storage ('" + Configuration.CONFIG_DIR +
                    "keysSSL_TLS" + File.separator + "')", e);
            return;
        }

        TOMConfiguration tomConf = new TOMConfiguration(id, Configuration.CONFIG_DIR, null);

        String[] aux = tomConf.getSSLTLSKeyStore().split("_");

        if (!aux[0].equals("EC"))
            logger.warn("Recommended use of EC for SSL/TLS keys");

        String alg = aux[0];
        int keySize = Integer.parseInt(aux[2].split("\\.")[0]);

        String keyFile = "config" + File.separator + "keysSSL_TLS" + File.separator + alg + "_KeyPair_" +
                keySize + ".pkcs12";

        if (!Storage.fileExists(keyFile)) {
            logger.warn("SSL/TLS Key not found, attempting to generate a new one");

            String command = "keytool -genkey -keyalg " + alg + " -keysize " + keySize + " -alias bftsmart" + alg +
                    " -keypass " + Keys.SECRET + " -storepass " + Keys.SECRET +
                    " -keystore " + keyFile + " -dname \"CN=BFT-SMaRT\"";

            logger.debug("Executing command: '" + command + "'");
            try {
                Process tr = Runtime.getRuntime().exec(command);
                tr.waitFor(10, TimeUnit.SECONDS);
            } catch (IOException e) {
                logger.error("Error while calling 'keytool'", e);
            } catch (InterruptedException e) {
                logger.error("Error while waiting for command to finish", e);
            }

            if (Storage.fileExists(keyFile))
                logger.info("SSL/TLS Key pair generated successfully");
            else {
                logger.warn("Could not generate SSL/TLS Key pair, try to run the following command on the root of " +
                        "the application: " + System.lineSeparator() + "\t" + command);
                throw new RuntimeException("SSL/TLS Keys generation error");
            }
        } else
            logger.debug("SSL/TLS Key pair already present in system");
    }

    /**
     * Generates public and private ECDSA keys according to 'system.config' configuration file.
     *
     * @param id the id of the client/replica
     */
    public static void generatePubAndPrivKeys(int id) {
        Configuration config = Configuration.getInstance();
        TOMConfiguration tomConf = new TOMConfiguration(id, Configuration.CONFIG_DIR, null);

        String signatureAlgorithmProvider = tomConf.getSignatureAlgorithmProvider();
        boolean defaultKeys = tomConf.useDefaultKeys();
        String keyLoader;

        try {
            keyLoader = config.getBftSmartKeyLoader();
        } catch (IOException e) {
            logger.error("Unable to read BFT-SMaRt configurations, proceeding with default signature keys", e);
            return;
        }

        String keyDir;
        if (keyLoader.equalsIgnoreCase("ECDSA"))
            keyDir = Configuration.CONFIG_DIR + "keysECDSA";
        else {
            logger.warn("########### WARNING ###########" + System.lineSeparator() +
                    "Voidchain recommends utilizing ECDSA keys for increased security. Furthermore Voichain" +
                    " only automatically generates keys for this type of algorithm");
            return;
        }

        if (defaultKeys) {
            logger.warn("Using default keys");
            return;
        }

        try {
            Path pD = Paths.get(keyDir);
            if (Files.notExists(pD))
                Files.createDirectories(pD);
        } catch (IOException ioException) {
            logger.error("Error occurred while creating directory for key file storage ('" + Configuration.CONFIG_DIR +
                    "keysECDSA" + File.separator + "')", ioException);
            return;
        }

        if (!Storage.fileExists(keyDir + File.separator + "privatekey" + id)) {
            logger.warn("Private Key file not found, generating new keys");

            try {
                new ECDSAKeyPairGenerator().run(id, config.getEcParam(), signatureAlgorithmProvider);
                logger.info("New Elliptic Curve Key Pair has been generated using '" + config.getEcParam() + "'");
            } catch (Exception exception) {
                logger.error("Error occurred while creating and/or storing the new key pair", exception);
            }
        }
    }

    public static byte[] getPubGenesisKeyBytes() throws IOException {
        return Storage.class.getClassLoader().getResourceAsStream("keys" + File.separator +
                "publickey-genesis").readAllBytes();
    }

    public static byte[] getPrivGenesisKeyBytes() throws IOException {
        return Storage.class.getClassLoader().getResourceAsStream("keys" + File.separator +
                "privatekey-genesis").readAllBytes();
    }

    public static PublicKey getPubKey(byte[] pubKey) throws IOException, NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        TOMConfiguration tomConf = new TOMConfiguration(-100, Configuration.CONFIG_DIR, null);
        String signatureAlgorithmProvider = tomConf.getSignatureAlgorithmProvider();

        KeyFactory keyFactory = KeyFactory.getInstance(Configuration.getInstance().getBftSmartKeyLoader(),
                signatureAlgorithmProvider);
        EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKey);

        return keyFactory.generatePublic(pubKeySpec);
    }

    public static PrivateKey getPrivKey(byte[] privKey) throws IOException, NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        TOMConfiguration tomConf = new TOMConfiguration(-100, Configuration.CONFIG_DIR, null);
        String signatureAlgorithmProvider = tomConf.getSignatureAlgorithmProvider();

        KeyFactory keyFactory = KeyFactory.getInstance(Configuration.getInstance().getBftSmartKeyLoader(),
                signatureAlgorithmProvider);
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privKey);

        return keyFactory.generatePrivate(privateKeySpec);
    }

    public static PublicKey getPubKey(byte[] pubKey, TOMConfiguration tomConf) throws IOException, NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        String signatureAlgorithmProvider = tomConf.getSignatureAlgorithmProvider();

        KeyFactory keyFactory = KeyFactory.getInstance(Configuration.getInstance().getBftSmartKeyLoader(),
                signatureAlgorithmProvider);
        EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKey);

        return keyFactory.generatePublic(pubKeySpec);
    }

    public static PrivateKey getPrivKey(byte[] privKey, TOMConfiguration tomConf) throws IOException, NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        String signatureAlgorithmProvider = tomConf.getSignatureAlgorithmProvider();

        KeyFactory keyFactory = KeyFactory.getInstance(Configuration.getInstance().getBftSmartKeyLoader(),
                signatureAlgorithmProvider);
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privKey);

        return keyFactory.generatePrivate(privateKeySpec);
    }

    // Generate Genesis Keys
    public static void main(String[] args) throws IOException {
        Storage.createDefaultConfigFiles();

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        int id = -100;

        Keys.generatePubAndPrivKeys(id);

        TOMConfiguration tomConf = new TOMConfiguration(id, Configuration.CONFIG_DIR, null);

        File outFilePriv = new File(Configuration.CONFIG_DIR + "privatekey-genesis");
        FileOutputStream outPriv = new FileOutputStream(outFilePriv);
        outFilePriv.createNewFile();
        outPriv.write(tomConf.getPrivateKey().getEncoded());
        outPriv.flush();
        outPriv.close();

        File outFilePub = new File(Configuration.CONFIG_DIR + "publickey-genesis");
        FileOutputStream outPub = new FileOutputStream(outFilePub);
        outFilePub.createNewFile();
        outPub.write(tomConf.getPublicKey().getEncoded());
        outPub.flush();
        outPub.close();
    }
}
