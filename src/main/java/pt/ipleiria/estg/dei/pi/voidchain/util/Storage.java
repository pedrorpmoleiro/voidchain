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
}
