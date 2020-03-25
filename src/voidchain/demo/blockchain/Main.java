package voidchain.demo.blockchain;

import org.bouncycastle.util.encoders.Base64;
import voidchain.blockchain.Block;
import voidchain.blockchain.Blockchain;
import voidchain.blockchain.Transaction;

public class Main {
    public static void main(String[] args) {
        Blockchain voidchain = new Blockchain();

        System.out.println("GENESIS HASH: " + Base64.toBase64String(voidchain.getCurrentBlock().getPreviousHash()));
        System.out.println("BEFORE ADD TRANSACTION, BLOCK: " + Base64.toBase64String(voidchain.getCurrentBlock().getHash()));

        voidchain.getCurrentBlock().addTransaction(new Transaction("FIRST TRANSACTION"));

        System.out.println("AFTER ADD TRANSACTION AND BEFORE CREATE BLOCK: " + Base64.toBase64String(voidchain.getCurrentBlock().getHash()));

        Block midBlock = voidchain.getCurrentBlock();
        voidchain.createBlock();

        System.out.println("AFTER CREATE NEW BLOCK: " + Base64.toBase64String(voidchain.getCurrentBlock().getHash()));

        System.out.println("NUMBER OF BLOCKS: " + voidchain.getBlockHeight());

        voidchain.getCurrentBlock().addTransaction(new Transaction("NEW TRANSACTION"));

        System.out.println("AFTER CREATE TRANSACTION ON NEW BLOCK: " + Base64.toBase64String(voidchain.getCurrentBlock().getHash()));

        System.out.println("IS BLOCKCHAIN VALID: " + voidchain.isChainValid());

        System.out.println("ALTERING SECOND BLOCK (ADDING TRANSACTION)");
        midBlock.addTransaction(new Transaction("BREAKING TRANSACTION"));

        System.out.println("IS BLOCKCHAIN VALID: " + voidchain.isChainValid());
    }
}
