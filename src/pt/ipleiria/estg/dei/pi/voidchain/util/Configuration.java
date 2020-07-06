package pt.ipleiria.estg.dei.pi.voidchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.StringTokenizer;

/**
 * The Configuration Singleton reads and stores the values found in the configuration file.
 */
public class Configuration {
    private static Configuration INSTANCE = null;

    private boolean firstRun = true;

    /**
     * The constant CONFIG_FILE stores the location of the configuration file.
     */
    public static final String CONFIG_FILE = "config" + File.separator + "voidchain.config";
    /**
     * The constant DEFAULT_PROTOCOL_VERSION stores the default value of the protocol version.
     */
    public static final String DEFAULT_PROTOCOL_VERSION = "1.0";
    /**
     * The constant DEFAULT_TRANSACTION_MAX_SIZE stores the default value of the max size of a transaction.
     */
    public static final int DEFAULT_TRANSACTION_MAX_SIZE = 512000;
    /**
     * The constant DEFAULT_NUM_TRANSACTIONS_BLOCK stores the default value of number of transactions to be stored in a
     * block.
     */
    public static final int DEFAULT_NUM_TRANSACTIONS_BLOCK = 100;
    /**
     * The constant DEFAULT_MEMORY_USED_FOR_BLOCKS stores the default value of how much memory should be used
     * to store blocks in memory.
     */
    public static final int DEFAULT_MEMORY_USED_FOR_BLOCKS = 128; // in MB
    /**
     * The constant DEFAULT_BLOCK_FILE_EXTENSION stores the default value of block file extension.
     */
    public static final String DEFAULT_BLOCK_FILE_EXTENSION = "dat";
    /**
     * The constant DEFAULT_BLOCK_FILE_BASE_NAME stores the default value of the block file base name.
     */
    public static final String DEFAULT_BLOCK_FILE_BASE_NAME = "block";
    /**
     * The constant DEFAULT_BLOCK_FILE_DIRECTORY stores the default value of block file directory.
     */
    public static final String DEFAULT_BLOCK_FILE_DIRECTORY = "data" + File.separator + "blocks";
    /**
     * The constant BLOCK_FILE_EXTENSION_SEPARATOR stores the block file extension separator.
     */
    public static final String BLOCK_FILE_EXTENSION_SEPARATOR = ".";
    /**
     * The constant BLOCK_FILE_EXTENSION_SEPARATOR_SPLIT stores the block file extension separator
     * to be used in split method.
     */
    public static final String BLOCK_FILE_EXTENSION_SEPARATOR_SPLIT = "\\.";
    /**
     * The constant DEFAULT_BLOCK_SYNC_PORT stores the default value of the Block Synchronization service port.
     */
    public static final int DEFAULT_BLOCK_SYNC_PORT = 18189;
    /**
     * The constant DEFAULT_EC_PARAM stores the default value of the Elliptic Curve Domain Param.
     */
    public static final String DEFAULT_EC_PARAM = "secp256k1";

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private String protocolVersion = DEFAULT_PROTOCOL_VERSION;
    private int transactionMaxSize = DEFAULT_TRANSACTION_MAX_SIZE;
    private int numTransactionsInBlock = DEFAULT_NUM_TRANSACTIONS_BLOCK;
    private int memoryUsedForBlocks = DEFAULT_MEMORY_USED_FOR_BLOCKS;
    private String blockFileExtension = DEFAULT_BLOCK_FILE_EXTENSION;
    private String blockFileBaseName = DEFAULT_BLOCK_FILE_BASE_NAME;
    private final String blockFileBaseNameSeparator = "_";
    private String blockFileDirectory = DEFAULT_BLOCK_FILE_DIRECTORY;
    private int blockSyncPort = DEFAULT_BLOCK_SYNC_PORT;
    private String ecParam = DEFAULT_EC_PARAM;

    private Configuration() {
        this.loadConfigurationFromDisk();
    }

    /**
     * Gets instance of the Configuration Singleton class.
     *
     * @return the Configuration class instance
     */
    public static Configuration getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Configuration();
        else
            INSTANCE.loadConfigurationFromDisk();

        return INSTANCE;
    }

    /**
     * Loads and stores the configuration from the configuration file.
     */
    public void loadConfigurationFromDisk() {
        try {
            FileReader fr = new FileReader(CONFIG_FILE);
            BufferedReader rd = new BufferedReader(fr);

            String line;
            while ((line = rd.readLine()) != null) {
                if (line.startsWith("#"))
                    return;

                StringTokenizer str = new StringTokenizer(line, "=");
                if (str.countTokens() > 1) {
                    String aux;
                    switch (str.nextToken().trim()) {
                        case "system.voidchain.protocol_version":
                            aux = str.nextToken().trim();
                            if (aux != null)
                                this.protocolVersion = aux;
                            continue;
                        case "system.voidchain.transaction.max_size":
                            aux = str.nextToken().trim();
                            if (aux != null)
                                this.transactionMaxSize = Integer.parseInt(aux);
                            continue;
                        case "system.voidchain.block.num_transaction":
                            aux = str.nextToken().trim();
                            if (aux != null)
                                this.numTransactionsInBlock = Integer.parseInt(aux);
                            continue;
                        case "system.voidchain.storage.block_file_extension":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.blockFileExtension = aux;
                            }
                            continue;
                        case "system.voidchain.storage.block_file_base_name":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.blockFileBaseName = aux;
                            }
                            continue;
                        case "system.voidchain.storage.block_file_directory":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null) {
                                    aux = aux.replace('/', File.separatorChar);

                                    this.blockFileDirectory = aux;
                                }
                            }
                            continue;
                        case "system.voidchain.memory.block_megabytes":
                            aux = str.nextToken().trim();
                            if (aux != null)
                                this.memoryUsedForBlocks = Integer.parseInt(aux);
                            continue;
                        case "system.voidchain.sync.block_sync_port":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.blockSyncPort = Integer.parseInt(aux);
                            }
                            continue;
                        case "DEFAULT_EC_PARAM":
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.ecParam = aux;
                            continue;
                    }
                }
            }

            fr.close();
            rd.close();

            this.firstRun = false;
        } catch (IOException e) {
            logger.error("Could not load configuration", e);
        }
    }

    /**
     * Gets the protocol version.
     *
     * @return the protocol version
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Gets the transaction max size.
     *
     * @return the transaction max size
     */
    public int getTransactionMaxSize() {
        return transactionMaxSize;
    }

    /**
     * Gets the number of transactions to be added in a block.
     *
     * @return the number of transactions transactions in a block
     */
    public int getNumTransactionsInBlock() {
        return numTransactionsInBlock;
    }

    /**
     * Gets the block file extension.
     *
     * @return the block file extension
     */
    public String getBlockFileExtension() {
        return blockFileExtension;
    }

    /**
     * Gets the block file base name.
     *
     * @return the block file base name
     */
    public String getBlockFileBaseName() {
        return blockFileBaseName;
    }

    /**
     * Gets the block file directory.
     *
     * @return the block file directory
     */
    public String getBlockFileDirectory() {
        return blockFileDirectory;
    }

    /**
     * Gets the block file base name separator.
     *
     * @return the block file base name separator
     */
    public String getBlockFileBaseNameSeparator() {
        return blockFileBaseNameSeparator;
    }

    /**
     * Gets the amount of memory to be used to store blocks in memory.
     *
     * @return the amount of memory to be used for block storage
     */
    public int getMemoryUsedForBlocks() {
        return memoryUsedForBlocks;
    }

    /**
     * Gets the port to be used in the Block Synchronization service.
     *
     * @return the port
     */
    public int getBlockSyncPort() {
        return blockSyncPort;
    }

    /**
     * Gets the Elliptic Curve param to be used in the creation of new Key pairs.
     *
     * @return the Domain Param
     */
    public String getEcParam() {
        return ecParam;
    }

    @Override
    public String toString() {
        return "Configuration: {" + System.lineSeparator() +
                "protocol version: '" + protocolVersion + '\'' + System.lineSeparator() +
                "transaction max size: " + transactionMaxSize + System.lineSeparator() +
                "number of transactions per block: " + numTransactionsInBlock + System.lineSeparator() +
                "number of MB used to store blocks in memory: " + memoryUsedForBlocks + System.lineSeparator() +
                "block file extension: '" + blockFileExtension + '\'' + System.lineSeparator() +
                "block file base name: '" + blockFileBaseName + '\'' + System.lineSeparator() +
                "block file directory: '" + blockFileDirectory + '\'' + System.lineSeparator() +
                "block file base name separator: '" + blockFileBaseNameSeparator + '\'' + System.lineSeparator() +
                '}';
    }
}
