package pt.ipleiria.estg.dei.pi.voidchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The Configuration Singleton reads and stores the values found in the configuration file.
 */
public class Configuration {
    private static Configuration INSTANCE = null;

    private boolean firstRun = true;

    /**
     * The constant CONFIG_FILES stores all the configuration files stored in the jar.
     */
    public static final List<String> CONFIG_FILES = new ArrayList<>() {{
        add("hosts.config");
        add("system.config");
        add("voidchain.config");
    }};

    /**
     * The constant CONFIG_DIR stores the location of the configuration directory.
     */
    public static final String CONFIG_DIR = "config" + File.separator;
    /**
     * The constant CONFIG_FILE stores the location of the configuration file.
     */
    public static final String CONFIG_FILE = CONFIG_DIR + "voidchain.config";
    /**
     * The constant BFT_SMART_CONFIG stores the location bft-smart configuration file.
     */
    public static final String BFT_SMART_CONFIG_FILE = CONFIG_DIR + "system.config";
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
     * The constant DEFAULT_DATA_FILE_EXTENSION stores the default value of data file extension.
     */
    public static final String DEFAULT_DATA_FILE_EXTENSION = "dat";
    /**
     * The constant DEFAULT_BLOCK_FILE_BASE_NAME stores the default value of the block file base name.
     */
    public static final String DEFAULT_BLOCK_FILE_BASE_NAME = "block";
    /**
     * The constant DEFAULT_WALLET_FILE_BASE_NAME stores the default value of the block file base name.
     */
    public static final String DEFAULT_WALLET_FILE_BASE_NAME = "wallet";
    /**
     * The constant DEFAULT_DATA_DIRECTORY stores the default value of the data directory.
     */
    public static final String DEFAULT_DATA_DIRECTORY = "data" + File.separator;
    /**
     * The constant DEFAULT_BLOCK_FILE_DIRECTORY stores the default value of block file directory.
     */
    public static final String DEFAULT_BLOCK_FILE_DIRECTORY = DEFAULT_DATA_DIRECTORY + "blocks" + File.separator;
    /**
     * The constant DEFAULT_WALLET_FILE_DIRECTORY stores the default value of the wallet directory.
     */
    public static final String DEFAULT_WALLET_FILE_DIRECTORY = DEFAULT_DATA_DIRECTORY + "wallets" + File.separator;
    /**
     * The constant FILE_EXTENSION_SEPARATOR stores the block file extension separator.
     */
    public static final String FILE_EXTENSION_SEPARATOR = ".";
    /**
     * The constant BLOCK_FILE_EXTENSION_SEPARATOR_SPLIT stores the block file extension separator
     * to be used in split method.
     */
    public static final String FILE_EXTENSION_SEPARATOR_SPLIT = "\\.";
    /**
     * The constant FILE_NAME_SEPARATOR stores the file name separator.
     */
    public static final String FILE_NAME_SEPARATOR = "_";
    /**
     * The constant DEFAULT_BLOCK_SYNC_PORT stores the default value of the Block Synchronization service port.
     */
    public static final int DEFAULT_BLOCK_SYNC_PORT = 18189;
    /**
     * The constant DEFAULT_EC_PARAM stores the default value of the Elliptic Curve Domain Param.
     */
    public static final String DEFAULT_EC_PARAM = "secp256k1";
    /**
     * The constant DEFAULT_BLOCK_PROPOSAL_TIMER stores the default value of the sleep timer of block proposal thread.
     */
    public static final int DEFAULT_BLOCK_PROPOSAL_TIMER = 5000;
    /**
     * The constant DEFAULT_BLOCKCHAIN_VALIDATION_TIMER stores the default value of the sleep timer of blockchain validity
     * verifier thread
     */
    public static final int DEFAULT_BLOCKCHAIN_VALIDATION_TIMER = 60000;

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private String protocolVersion = DEFAULT_PROTOCOL_VERSION;
    private int transactionMaxSize = DEFAULT_TRANSACTION_MAX_SIZE;
    private int numTransactionsInBlock = DEFAULT_NUM_TRANSACTIONS_BLOCK;
    private int memoryUsedForBlocks = DEFAULT_MEMORY_USED_FOR_BLOCKS;
    private String dataFileExtension = DEFAULT_DATA_FILE_EXTENSION;
    private String blockFileBaseName = DEFAULT_BLOCK_FILE_BASE_NAME;
    private String dataDirectory = DEFAULT_DATA_DIRECTORY;
    private String blockFileDirectory = DEFAULT_BLOCK_FILE_DIRECTORY;
    private String walletFileDirectory = DEFAULT_WALLET_FILE_DIRECTORY;
    private String walletFileBaseName = DEFAULT_WALLET_FILE_BASE_NAME;
    private int blockSyncPort = DEFAULT_BLOCK_SYNC_PORT;
    private String ecParam = DEFAULT_EC_PARAM;
    private int blockProposalTimer = DEFAULT_BLOCK_PROPOSAL_TIMER;
    private int blockchainValidTimer = DEFAULT_BLOCKCHAIN_VALIDATION_TIMER;

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
                if (line.startsWith("#")) continue;
                if (line.equalsIgnoreCase("")) continue;

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
                        case "system.voidchain.storage.data_file_extension":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.dataFileExtension = aux;
                            }
                            continue;
                        case "system.voidchain.storage.block_file_base_name":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.blockFileBaseName = aux;
                            }
                            continue;
                        case "system.voidchain.storage.wallet_file_base_name":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.walletFileBaseName = aux;
                            }
                            continue;
                        case "system.voidchain.storage.data_directory":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null) {
                                    aux = aux.replace('/', File.separatorChar);
                                    if (!aux.endsWith(File.separator))
                                        //aux = aux.substring(0, aux.length() - 1);
                                        aux = aux.concat(File.separator);
                                    this.dataDirectory = aux;
                                }
                            }
                            continue;
                        case "system.voidchain.storage.block_file_directory":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null) {
                                    aux = aux.replace('/', File.separatorChar);
                                    if (!aux.endsWith(File.separator))
                                        //aux = aux.substring(0, aux.length() - 1);
                                        aux = aux.concat(File.separator);
                                    this.blockFileDirectory = aux;
                                }
                            }
                            continue;
                        case "system.voidchain.storage.wallet_file_directory":
                            if (firstRun) {
                                aux = str.nextToken().trim();
                                if (aux != null) {
                                    aux = aux.replace('/', File.separatorChar);
                                    if (!aux.endsWith(File.separator))
                                        //aux = aux.substring(0, aux.length() - 1);
                                        aux = aux.concat(File.separator);
                                    this.walletFileDirectory = aux;
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
                        case "system.voidchain.crypto.ec_param":
                            aux = str.nextToken().trim();
                            if (aux != null)
                                this.ecParam = aux;
                            continue;
                        case "system.voidchain.core.block_proposal_timer":
                            aux = str.nextToken().trim();
                            if (aux != null)
                                this.blockProposalTimer = Integer.parseInt(aux) * 1000;
                            continue;
                        case "system.voidchain.blockchain.chain_valid_timer":
                            aux = str.nextToken().trim();
                            if (aux != null)
                                this.blockchainValidTimer = Integer.parseInt(aux) * 1000;
                            continue;
                    }
                }
            }

            fr.close();
            rd.close();

            if (this.firstRun)
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
     * Gets the data file extension.
     *
     * @return the data file extension
     */
    public String getDataFileExtension() {
        return dataFileExtension;
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
     * Gets the block file directory with full path from project root.
     *
     * @return the block file directory
     */
    public String getBlockFileDirectoryFull() {
        return dataDirectory + blockFileDirectory;
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

    /**
     * Gets data directory.
     *
     * @return the data directory
     */
    public String getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Gets wallet file directory.
     *
     * @return the wallet file directory
     */
    public String getWalletFileDirectory() {
        return walletFileDirectory;
    }

    /**
     * Gets wallet file directory with full path from project root.
     *
     * @return the wallet file directory
     */
    public String getWalletFileDirectoryFull() {
        return dataDirectory + walletFileDirectory;
    }

    /**
     * Gets wallet file base name.
     *
     * @return the wallet file base name
     */
    public String getWalletFileBaseName() {
        return walletFileBaseName;
    }

    /**
     * Gets timer (in millis) for block proposal thread.
     *
     * @return the block proposal timer
     */
    public int getBlockProposalTimer() {
        return blockProposalTimer;
    }

    /**
     * Gets blockchain valid timer.
     *
     * @return the blockchain valid timer
     */
    public int getBlockchainValidTimer() {
        return blockchainValidTimer;
    }

    /**
     * Gets bft smart key loader.
     *
     * @return the bft smart key loader
     * @throws IOException the io exception
     */
    public String getBftSmartKeyLoader() throws IOException {
        String keyLoader = "ECDSA";

        FileReader fr = new FileReader(Configuration.BFT_SMART_CONFIG_FILE);
        BufferedReader rd = new BufferedReader(fr);

        String line;
        while ((line = rd.readLine()) != null) {
            if (line.startsWith("#"))
                continue;

            StringTokenizer str = new StringTokenizer(line, "=");
            if (str.countTokens() > 1) {
                switch (str.nextToken().trim()) {
                    case "system.communication.defaultKeyLoader":
                        keyLoader = str.nextToken().trim();
                        continue;
                }
            }
        }

        return keyLoader;
    }

    @Override
    public String toString() {
        return "Configuration: " + System.lineSeparator() +
                "\tprotocolVersion: " + protocolVersion + System.lineSeparator() +
                "\ttransactionMaxSize: " + transactionMaxSize + System.lineSeparator() +
                "\tnumTransactionsInBlock: " + numTransactionsInBlock + System.lineSeparator() +
                "\tmemoryUsedForBlocks: " + memoryUsedForBlocks + System.lineSeparator() +
                "\tdataFileExtension: " + dataFileExtension + System.lineSeparator() +
                "\tblockFileBaseName: " + blockFileBaseName + System.lineSeparator() +
                "\tdataDirectory: " + dataDirectory + System.lineSeparator() +
                "\tblockFileDirectory: " + blockFileDirectory + System.lineSeparator() +
                "\twalletFileDirectory: " + walletFileDirectory + System.lineSeparator() +
                "\twalletFileBaseName: " + walletFileBaseName + System.lineSeparator() +
                "\tblockSyncPort: " + blockSyncPort + System.lineSeparator() +
                "\tecParam: " + ecParam + System.lineSeparator() +
                "\tblockProposalTimer: " + blockProposalTimer;
    }
}
