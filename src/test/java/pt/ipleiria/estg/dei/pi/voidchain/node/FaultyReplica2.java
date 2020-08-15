package pt.ipleiria.estg.dei.pi.voidchain.node;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import pt.ipleiria.estg.dei.pi.voidchain.util.KeyGenerator;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import java.io.IOException;
import java.security.Security;

/**
 * Faulty Replica class that takes too long to reply to a command.
 */
public class FaultyReplica2 extends DefaultSingleRecoverable {
    public FaultyReplica2(int id) {
        new ServiceReplica(id, this, this);
    }

    @Override
    public void installSnapshot(byte[] bytes) {}

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext) {
        try {
            Thread.sleep(900000000000000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-2);
        }
        return new byte[0];
    }

    @Override
    public byte[] appExecuteUnordered(byte[] bytes, MessageContext messageContext) {
        try {
            Thread.sleep(900000000000000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-2);
        }
        return new byte[0];
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: FaultyReplica2 <id>");
            System.exit(-1);
        }

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        Storage.createDefaultConfigFiles();

        int id = Integer.parseInt(args[0]);
        KeyGenerator.generatePubAndPrivKeys(id);
        KeyGenerator.generateSSLKey(id);

        KeyGenerator.generatePubAndPrivKeys(-42); // Genesis Block Priv & Pub Key

        new FaultyReplica2(id);
    }
}
