package pt.ipleiria.estg.dei.pi.voidchain.util;

import bftsmart.reconfiguration.util.HostsConfig;
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

            FileOutputStream fos = new FileOutputStream(fileDirectory + fileName, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(object);

            oos.close();
            fos.close();

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
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);

        Object o = ois.readObject();

        ois.close();
        fis.close();

        return o;
    }

    public static void cleanDirectory(String directory) {
        File dir = new File(directory);

        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                cleanDirectory(f.getPath());
            else
                f.delete();
        }
    }

    // TODO: Javadoc
    public static boolean fileExists(String fileName) {
        try {
            new FileReader(fileName);
        } catch (FileNotFoundException e) {
            logger.warn("The requested file '" + fileName + "' was not found");
            return false;
        }

        return true;
    }

    public static void createDefaultConfigFiles() throws IOException {
        Path configDir = Paths.get(Configuration.CONFIG_DIR);

        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            logger.error("Unable to create config directory");
            throw new IOException("Unable to create config dir");
        }

        for (String f : Configuration.CONFIG_FILES) {
            String filePath = configDir + File.separator + f;
            if (Files.notExists(Paths.get(filePath))) {
                logger.info("Creating file '" + f + "' in 'config'");

                InputStream in = Storage.class.getClassLoader().getResourceAsStream(filePath);
                File outFile = new File(filePath);
                FileOutputStream out = new FileOutputStream(outFile);

                outFile.createNewFile();

                out.write(in.readAllBytes());
                out.flush();
                out.close();
            }
        }
    }

    /*public static void copyFilesRecursive(File[] files, Path dir) throws IOException {
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
    }*/
}
