package pt.ipleiria.estg.dei.pi.voidchain.client;

import bftsmart.reconfiguration.util.TOMConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.util.Configuration;
import pt.ipleiria.estg.dei.pi.voidchain.util.Hash;
import pt.ipleiria.estg.dei.pi.voidchain.util.Pair;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The wallet is a storage device for the user's asymmetric keys, used by users to sign transactions and a list of the user's transactions.
 * To secure its data, a symmetric-key algorithm, with user-created password, is applied.
 *
 */

public class Wallet implements Serializable {
    /* Attributes */
    private int id;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private byte[] password;
    private List<Pair<Integer, byte[]>> transactions;

    private static final transient Logger logger = LoggerFactory.getLogger(Wallet.class);

    private static Wallet INSTANCE = null;

    /* Constructors */

    private Wallet(TOMConfiguration smartConf, byte[] password) {
        if (password.length < 8)
            throw new IllegalArgumentException("Password is too short");

        this.id = smartConf.getProcessId();
        this.privateKey = smartConf.getPrivateKey();
        this.publicKey = smartConf.getPublicKey();
        this.password = Hash.calculateSHA3256(password);
        this.transactions = new ArrayList<>();
    }


    /**
     * Gets the instance of Wallet class
     *
     * @param smartConf
     * @param password
     * @return the Wallet class instance
     */
    public static Wallet getInstance(TOMConfiguration smartConf, byte[] password) {
        if (INSTANCE == null)
            try {
                INSTANCE = Wallet.fromDisk(smartConf.getProcessId(), password);
            } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException |
                    NoSuchProviderException | IllegalBlockSizeException | BadPaddingException |
                    NoSuchPaddingException e) {
                logger.info("Encryption encryption", e);
            } catch (FileNotFoundException e) {
                logger.info("No wallet file detected, creating new wallet");
                INSTANCE = new Wallet(smartConf, password);
                INSTANCE.toDisk();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Error while performing IO Operations", e);
            }

        return INSTANCE;
    }

    /**
     * Gets the wallet ID
     *
     * @return the wallet ID
     */
    public int getId() {
        return id;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getPassword() {
        return password;
    }

    public List<Pair<Integer, byte[]>> getTransactions() {
        return transactions;
    }

    public int getSize() {
        int auxSize = Integer.BYTES + password.length;

        for (Pair<Integer, byte[]> p : transactions) {
            auxSize += Integer.BYTES + p.getO2().length;
        }

        if (privateKey != null)
            auxSize += privateKey.getEncoded().length;

        if (publicKey != null)
            auxSize += publicKey.getEncoded().length;

        return auxSize;
    }

    /**
     *  Adds a transaction to the wallet
     *
     * @param transaction
     */
    public void addTransaction(Transaction transaction) {
        this.transactions.add(new Pair<>(0, transaction.getHash()));
        this.toDisk();
    }

    /**
     * Adds a transaction to the wallet plus the last block
     *
     * @param transaction
     * @param lastBlockInChainHeight
     */
    public void addTransaction(Transaction transaction, int lastBlockInChainHeight) {
        this.transactions.add(new Pair<>(lastBlockInChainHeight, transaction.getHash()));
        this.toDisk();
    }

    /**
     * Adds a batch of transactions to the wallet
     *
     * @param transactions
     */
    public void addTransactions(List<Transaction> transactions) {
        transactions.forEach(transaction -> {
            this.transactions.add(new Pair<>(0, transaction.getHash()));
        });
        this.toDisk();
    }

    public void addTransactions(List<Transaction> transactions, int lastBlockInChainHeight) {
        transactions.forEach(transaction -> {
            this.transactions.add(new Pair<>(lastBlockInChainHeight, transaction.getHash()));
        });
        this.toDisk();
    }

    /**
     * Saves the wallet in local disk
     *
     * @return true if writing/saving the wallet in the local disk was successful or false otherwise
     */
    public boolean toDisk() {
        Configuration config = Configuration.getInstance();

        byte[] walletData;
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut)) {

            objOut.writeObject(this);
            objOut.flush();
            byteOut.flush();

            walletData = byteOut.toByteArray();

        } catch (IOException e) {
            logger.error("Unable to convert Wallet instance to byte array", e);
            return false;
        }

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            logger.error("Unable to create AES cipher for encryption", e);
            return false;
        }

        SecretKeySpec secret = new SecretKeySpec(password, "AES");
        IvParameterSpec iv = new IvParameterSpec(password, 0, 16);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            logger.error("Unable to initialize cipher for encryption", e);
            return false;
        }

        byte[] walletEnc;
        try {
            walletEnc = cipher.doFinal(walletData);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            logger.error("Error occurred while encrypting wallet data", e);
            return false;
        }

        return Storage.writeObjectToDisk(walletEnc, config.getWalletFileDirectoryFull(),
                config.getWalletFileBaseName() + Configuration.FILE_NAME_SEPARATOR + this.id +
                        Configuration.FILE_EXTENSION_SEPARATOR + config.getDataFileExtension());
    }

    /**
     * Loads wallet from local disk
     *
     * @param id
     * @param password
     * @return the wallet saved in local disk
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */
    public static Wallet fromDisk(int id, byte[] password) throws IOException, ClassNotFoundException,
            NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        Configuration config = Configuration.getInstance();

        byte[] passwordHash = Hash.calculateSHA3256(password);
        SecretKeySpec secret = new SecretKeySpec(passwordHash, "AES");
        IvParameterSpec iv = new IvParameterSpec(passwordHash, 0, 16);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, secret, iv);

        byte[] walletEnc = (byte[]) Storage.readObjectFromDisk(config.getWalletFileDirectoryFull() +
                config.getWalletFileBaseName() + Configuration.FILE_NAME_SEPARATOR + id +
                Configuration.FILE_EXTENSION_SEPARATOR + config.getDataFileExtension());

        byte[] walletData = cipher.doFinal(walletEnc);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(walletData);
        ObjectInput objIn = new ObjectInputStream(byteIn);

        Wallet wallet = (Wallet) objIn.readObject();

        objIn.close();
        byteIn.close();

        return wallet;
    }
}
