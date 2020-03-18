package voidchain.demo.blockchain;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import voidchain.blockchain.Block;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Replica extends DefaultSingleRecoverable {
    private int id;
    private Set<Block> blockchain;
    private Block mostRecentBlock;
    private Logger logger;

    public Replica(int id) {
        this.id = id;
        this.blockchain = new LinkedHashSet<>();
        this.logger = Logger.getLogger(Replica.class.getName());

        String firstPreviousHash = "GENESIS";
        Block genesisBlock = new Block(firstPreviousHash.getBytes(StandardCharsets.UTF_8));
        this.blockchain.add(genesisBlock);

        Block secondBlock = new Block(genesisBlock.getHash());
        this.blockchain.add(secondBlock);
        this.mostRecentBlock = secondBlock;

        new ServiceReplica(id, this, this);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: demo.map.MapServer <server id>");
            System.exit(-1);
        }
        new Replica(Integer.parseInt(args[0]));
    }

    @Override
    public void installSnapshot(byte[] bytes) {

    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext) {
        return new byte[0];
    }

    @Override
    public byte[] appExecuteUnordered(byte[] bytes, MessageContext messageContext) {
        return new byte[0];
    }
}
