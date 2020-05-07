package pt.ipleiria.estg.dei.pi.voidchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.StringTokenizer;

public class Configuration {
    private static Configuration INSTANCE = null;

    public static final String CONFIG_FILE = "config" + File.separator + "voidchain.config";

    public static final String DEFAULT_PROTOCOL_VERSION = "0.2";
    public static final int DEFAULT_TRANSACTION_MAX_SIZE = 1024;
    public static final int DEFAULT_NUM_TRANSACTIONS_BLOCK = 5;

    public static final int DEFAULT_BLOCKS_MEMORY = 2;
    public static final String DEFAULT_BLOCK_FILE_EXTENSION = ".dat";
    public static final String DEFAULT_BLOCK_FILE_BASE_NAME = "block";
    public static final String DEFAULT_BLOCK_FILE_DIRECTORY = "data" + File.separator + "blocks";


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String protocolVersion = DEFAULT_PROTOCOL_VERSION;
    private int transactionMaxSize = DEFAULT_TRANSACTION_MAX_SIZE;
    private int numTransactionsInBlock = DEFAULT_NUM_TRANSACTIONS_BLOCK;

    private int numBlockInMemory = DEFAULT_BLOCKS_MEMORY;
    private String blockFileExtension = DEFAULT_BLOCK_FILE_EXTENSION;
    private String blockFileBaseName = DEFAULT_BLOCK_FILE_BASE_NAME;
    private String blockFileDirectory = DEFAULT_BLOCK_FILE_DIRECTORY;

    private Configuration() {
        try {
            FileReader fr = new FileReader(CONFIG_FILE);
            BufferedReader rd = new BufferedReader(fr);

            String line;
            while ((line = rd.readLine()) != null) {
                if (!line.startsWith("#")) {
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
                            case "system.voidchain.memory.num_blocks":
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.numBlockInMemory = Integer.parseInt(aux);
                                continue;
                            case "system.voidchain.storage.block_file_extension":
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.blockFileExtension = aux;
                                continue;
                            case "system.voidchain.storage.block_file_base_name":
                                aux = str.nextToken().trim();
                                if (aux != null)
                                    this.blockFileBaseName = aux;
                                continue;
                            case "system.voidchain.storage.block_file_directory":
                                aux = str.nextToken().trim();
                                if (aux != null) {
                                    aux = aux.replace('/', File.separatorChar);

                                    this.blockFileDirectory = aux;
                                }
                        }
                    }
                }
            }

            fr.close();
            rd.close();
        } catch (IOException e) {
            this.logger.error("Could not load configuration", e);
        }
    }

    public static Configuration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Configuration();
        }

        return INSTANCE;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public int getTransactionMaxSize() {
        return transactionMaxSize;
    }

    public int getNumTransactionsInBlock() {
        return numTransactionsInBlock;
    }

    public int getNumBlockInMemory() {
        return numBlockInMemory;
    }

    public String getBlockFileExtension() {
        return blockFileExtension;
    }

    public String getBlockFileBaseName() {
        return blockFileBaseName;
    }

    public String getBlockFileDirectory() {
        return blockFileDirectory;
    }

    @Override
    public String toString() {
        return "Configuration: {" + System.lineSeparator() +
                "protocol version: '" + protocolVersion + '\'' + System.lineSeparator() +
                "transaction max size: " + transactionMaxSize + System.lineSeparator() +
                "number of transactions per block: " + numTransactionsInBlock + System.lineSeparator() +
                "number of blocks in memory: " + numBlockInMemory + System.lineSeparator() +
                "block file extension: '" + blockFileExtension + '\'' + System.lineSeparator() +
                "block file base name: '" + blockFileBaseName + '\'' + System.lineSeparator() +
                "block file directory: '" + blockFileDirectory + '\'' + System.lineSeparator() +
                '}';
    }
}
