package pt.ipleiria.estg.dei.pi.voidchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;

import java.util.*;

// https://medium.com/@vinayprabhu19/merkel-tree-in-java-b45093c8c6bd
public class MerkleTree {
    private static final Logger logger = LoggerFactory.getLogger(MerkleTree.class);

    /**
     * Return the merkle root of the given set of hashes.
     * <br>
     * Returns byte[0] if error occurred while calculating the merkle tree.
     *
     * @param transactionMap the transaction map
     * @return the merkle root
     */
    public static byte[] getMerkleRoot(Map<byte[], Transaction> transactionMap) {
        try {
            ArrayList<byte[]> hashList = new ArrayList<>();
            List<Map.Entry<byte[], Transaction>> entryList = new ArrayList<>(transactionMap.entrySet());
            entryList.sort(Comparator.comparingLong(o -> o.getValue().getTimestamp()));
            entryList.forEach(t -> hashList.add(t.getKey()));

            return merkleTree(hashList).get(0);
        } catch (RuntimeException e) {
            logger.error("Error occurred while calculating merkle tree", e);
            return new byte[0];
        }
    }

    /**
     * Creates merkle tree of given list of hashes.
     *
     * @param hashList the hash list
     * @return the merkle tree array list
     * @throws RuntimeException         runtime exception if error occurred while calculating the merkle root
     * @throws IllegalArgumentException illegal argument exception if given list is empty
     */
    public static ArrayList<byte[]> merkleTree(ArrayList<byte[]> hashList) throws RuntimeException {
        if (hashList.size() == 0)
            throw new IllegalArgumentException("Received Hash List is empty");

        if (hashList.size() == 1)
            return hashList;

        ArrayList<byte[]> parentList = new ArrayList<>();

        // Hash the leaf transaction pair to get parent transaction
        // for (int i = 0; i < hashList.size(); i+=2) {
        for (int i = 0; i < hashList.size() - 1; i += 2) {
            byte[] t1Hash = hashList.get(i);
            byte[] t2hash = hashList.get(i + 1);

            int sizeAux = t1Hash.length + t2hash.length;
            byte[] aux = new byte[sizeAux];
            int j = 0;

            for (byte b : t1Hash) {
                aux[j] = b;
                j++;
            }
            for (byte b : t2hash) {
                aux[j] = b;
                j++;
            }

            if (j != sizeAux)
                // THIS SHOULDN'T RUN
                throw new RuntimeException("Could not write all bytes to array");

            parentList.add(Hash.calculateSHA3512RIPEMD160(aux));
        }

        // If odd number of transactions, add the last transaction again
        if (hashList.size() % 2 == 1)
            parentList.add(hashList.get(hashList.size() - 1));

        return merkleTree(parentList);
    }
}
