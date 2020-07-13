package pt.ipleiria.estg.dei.pi.voidchain.util;

import bftsmart.tom.util.ECDSAKeyPairGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class SignatureKeyGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SignatureKeyGenerator.class);

    private static final String BFT_SMART_CONFIG_FILE = "config" + File.separator + "system.config";

    /**
     * Generates public and private ECDSA keys according to 'system.config' configuration file.
     *
     * @param id the id of the key
     */
    // TODO: SSL_TLS KEYS
    public static void generatePubAndPrivKeys(int id) {
        boolean defaultKeys = false;
        String signatureAlgorithmProvider = "BC";
        String keyLoader = "ECDSA";

        Configuration config = Configuration.getInstance();

        try {
            FileReader fr = new FileReader(BFT_SMART_CONFIG_FILE);
            BufferedReader rd = new BufferedReader(fr);

            String line;
            while ((line = rd.readLine()) != null) {
                if (line.startsWith("#"))
                    continue;

                StringTokenizer str = new StringTokenizer(line, "=");
                if (str.countTokens() > 1) {
                    switch (str.nextToken().trim()) {
                        case "system.communication.signatureAlgorithmProvider":
                            signatureAlgorithmProvider = str.nextToken().trim();
                            continue;
                        case "system.communication.defaultKeyLoader":
                            keyLoader = str.nextToken().trim();
                            continue;
                        case "system.communication.defaultkeys":
                            defaultKeys = str.nextToken().trim().equalsIgnoreCase("true");
                            continue;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read BFT-SMaRt configurations, proceeding with default signature keys", e);
            return;
        }

        String keyDir;
        if (keyLoader.equalsIgnoreCase("ECDSA"))
            keyDir = "config" + File.separator + "keysECDSA";
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
            logger.error("Error occurred while creating directory for key file storage ('config" +
                    File.separator + "keysECDSA" + File.separator, ioException);
            return;
        }


        try {
            new FileReader(keyDir + File.separator + "privatekey" + id);
        } catch (FileNotFoundException e) {
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
