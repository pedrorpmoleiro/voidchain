package pt.ipleiria.estg.dei.pi.voidchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Storage {
    private static final Logger logger = LoggerFactory.getLogger(Storage.class);

    /**
     * Write object to disk.
     *
     * @param object        the object to be saved
     * @param fileDirectory the directory to be saved in
     * @param fileName      the file name to be saved under
     * @return true if the object was saved successfully or false otherwise
     */
    public static boolean writeObjectToDisk(Object object, String fileDirectory, String fileName) {
        try {
            Path pD = Paths.get(fileDirectory);

            if (Files.notExists(pD))
                Files.createDirectories(pD);

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileDirectory + fileName,
                    false));
            oos.writeObject(object);
            oos.close();

            return true;
        } catch (IOException e) {
            logger.error("Error while writing '" + fileName + "' to disk", e);
            return false;
        }
    }

    /**
     * Reads object from disk.
     *
     * @param fileName the path and file name
     * @return the object
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static Object readObjectFromDisk(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));

        return ois.readObject();
    }

    // TODO: Javadoc
    public static boolean fileExists(String fileName) {
        try {
            new FileReader(fileName);
        } catch (FileNotFoundException e) {
            logger.warn("Requested file '" + fileName + "' has a problem", e);
            return false;
        }

        return true;
    }

    public static void createDefaultConfigFiles() throws IOException {
        Path configDir = Paths.get(Configuration.CONFIG_DIR);

        if (!Files.notExists(configDir))
            return;

        logger.info("Config files not found, creating with default values");

        File confDir = new File(Storage.class.getClassLoader().getResource("config").getPath());
        //File[] files2 = new File(ClassLoader.getSystemResource("config").getPath()).listFiles();
        //File[] files3 = new File(URLClassLoader.getSystemResource("config").getPath()).listFiles();
        File files4 = new File(Storage.class.getClassLoader().getResource("logback.xml").getPath());
        File[] confFiles = confDir.listFiles();
        //File files6 = new File(Storage.class.getClassLoader().getResource("config" + File.separator + "hosts.config").getPath());
        boolean filesDir = confDir.isDirectory();

        try {
            logger.debug("Creating config directory");
            Files.createDirectories(configDir);
        } catch (IOException e) {
            logger.error("Unable to create config directory");
            throw new IOException("Unable to create config dir");
        }

        try {
            assert confFiles != null;
            copyFilesRecursive(confFiles, configDir);
        } catch (IOException e) {
            logger.error("Error while creating default config files");
            throw new IOException("Error while creating default config files", e);
        }
    }

    private static void copyFilesRecursive(File[] files, Path dir) throws IOException {
        for (File f : files)
            if (f.isDirectory()) {
                logger.debug("creating directory'" + f.getName() + "' in '" + dir + "'");
                Path path = Paths.get(dir + File.separator + f.getName());
                Files.createDirectories(path);
                File[] filesAux = f.listFiles();
                assert filesAux != null;
                copyFilesRecursive(filesAux, path);
            } else {
                logger.debug("Creating file '" + f.getName() + "' in '" + dir + "'");
                Path path = Paths.get(dir + File.separator + f.getName());
                Files.copy(f.toPath(), path);
            }
    }

    public static void main(String[] args) throws IOException {
        createDefaultConfigFiles();
    }
}
