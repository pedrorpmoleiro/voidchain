package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Block;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Blockchain;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Transaction;
import pt.ipleiria.estg.dei.pi.voidchain.blockchain.Util;

public class Main {
    public static void main(String[] args) {
        Blockchain voidchain = new Blockchain();

        System.out.println("GENESIS HASH: " + Util.convertByteArrayToHexString(voidchain.getCurrentBlock().getPreviousHash()));
        System.out.println("BEFORE ADD TRANSACTION, BLOCK: " + Util.convertByteArrayToHexString(voidchain.getCurrentBlock().getHash()));

        voidchain.getCurrentBlock().addTransaction(new Transaction("FIRST TRANSACTION"));

        System.out.println("AFTER ADD TRANSACTION AND BEFORE CREATE BLOCK: " + Util.convertByteArrayToHexString(voidchain.getCurrentBlock().getHash()));

        Block midBlock = voidchain.getCurrentBlock();
        voidchain.createBlock();

        System.out.println("AFTER CREATE NEW BLOCK: " + Util.convertByteArrayToHexString(voidchain.getCurrentBlock().getHash()));

        System.out.println("NUMBER OF BLOCKS: " + voidchain.getBlockHeight());

        voidchain.getCurrentBlock().addTransaction(new Transaction("NEW TRANSACTION"));

        System.out.println("AFTER CREATE TRANSACTION ON NEW BLOCK: " + Util.convertByteArrayToHexString(voidchain.getCurrentBlock().getHash()));

        System.out.println("IS BLOCKCHAIN VALID: " + voidchain.isChainValid());

        System.out.println("ALTERING SECOND BLOCK (ADDING TRANSACTION)");
        midBlock.addTransaction(new Transaction("BREAKING TRANSACTION"));

        System.out.println("IS BLOCKCHAIN VALID: " + voidchain.isChainValid());
    }
}
