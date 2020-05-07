package pt.ipleiria.estg.dei.pi.voidchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Storage {
    private static Logger logger = LoggerFactory.getLogger(Storage.class.getName());

    // TODO: IMPROVE
    public static boolean writeToDiskCompressed(Object object, String fileDirectory, String fileName) {
        Path pD = Paths.get(fileDirectory);

        try {
            if (Files.notExists(pD))
                Files.createDirectories(pD);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(new FileOutputStream(
                fileDirectory + fileName, false)))) {
            oos.writeObject(object);
        } catch (IOException e) {
            logger.error("Error while writing '" + fileName + "' to disk", e);
            return false;
        }

        return true;
    }

    public static Object readFromDiskCompressed(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new InflaterInputStream(new FileInputStream(fileName)));

        return ois.readObject();
    }
}
