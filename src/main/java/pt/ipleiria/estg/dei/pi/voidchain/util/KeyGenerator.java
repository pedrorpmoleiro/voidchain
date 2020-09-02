package pt.ipleiria.estg.dei.pi.voidchain.util;

import bftsmart.reconfiguration.util.TOMConfiguration;
import bftsmart.tom.util.ECDSAKeyPairGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class KeyGenerator {
    private static final Logger logger = LoggerFactory.getLogger(KeyGenerator.class);

    private static final String SECRET = "Q2o3^TjE9OcrcZqG";

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
                    " -keypass " + KeyGenerator.SECRET + " -storepass " + KeyGenerator.SECRET +
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
            else
                logger.warn("Could not generate SSL/TLS Key pair, try to run the following command on the root of " +
                        "the application: " + System.lineSeparator() + "\t" + command);
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
}
