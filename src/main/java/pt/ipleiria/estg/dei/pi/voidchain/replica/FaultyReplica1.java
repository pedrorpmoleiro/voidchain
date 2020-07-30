package pt.ipleiria.estg.dei.pi.voidchain.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import pt.ipleiria.estg.dei.pi.voidchain.util.SignatureKeyGenerator;
import pt.ipleiria.estg.dei.pi.voidchain.util.Storage;

import java.io.IOException;
import java.security.Security;
import java.util.Random;

/**
 * Faulty Replica class that always provides the same reply regardless of command.
 */
public class FaultyReplica1 extends DefaultSingleRecoverable {
    private byte[] reply;

    public FaultyReplica1(int id) {
        this.reply = new byte[10];
        new Random().nextBytes(reply);

        new ServiceReplica(id, this, this);
    }

    @Override
    public void installSnapshot(byte[] bytes) {}

    @Override
    public byte[] getSnapshot() {
        return reply;
    }

    @Override
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext) {
        return reply;
    }

    @Override
    public byte[] appExecuteUnordered(byte[] bytes, MessageContext messageContext) {
        return reply;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: FaultyReplica1 <id>");
            System.exit(-1);
        }

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        Storage.createDefaultConfigFiles();

        int id = Integer.parseInt(args[0]);
        SignatureKeyGenerator.generatePubAndPrivKeys(id);
        SignatureKeyGenerator.generateSSLKey(id);

        SignatureKeyGenerator.generatePubAndPrivKeys(-42); // Genesis Block Priv & Pub Key

        new FaultyReplica1(id);
    }
}
