package pt.ipleiria.estg.dei.pi.voidchain.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import pt.ipleiria.estg.dei.pi.voidchain.util.SignatureKeyGenerator;

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

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: FaultyReplica2 <id>");
            System.exit(-1);
        }

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());

        int id = Integer.parseInt(args[0]);
        SignatureKeyGenerator.generatePubAndPrivKeys(id);

        new FaultyReplica2(id);
    }
}
