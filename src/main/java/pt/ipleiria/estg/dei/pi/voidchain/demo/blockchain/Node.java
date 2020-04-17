package pt.ipleiria.estg.dei.pi.voidchain.demo.blockchain;

import bftsmart.statemanagement.ApplicationState;
import bftsmart.statemanagement.StateManager;
import bftsmart.statemanagement.strategy.StandardStateManager;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ReplicaContext;
import bftsmart.tom.server.Recoverable;
import bftsmart.tom.server.SingleExecutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Node implements Recoverable, SingleExecutable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private StateManager stateManager;

    @Override
    public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
        return new byte[0];
    }

    @Override
    public void setReplicaContext(ReplicaContext replicaContext) {

    }

    @Override
    public ApplicationState getState(int cid, boolean sendState) {
        return null;
    }

    @Override
    public int setState(ApplicationState state) {
        return 0;
    }

    @Override
    public StateManager getStateManager() {
        if(stateManager == null)
            stateManager = new StandardStateManager();

        return stateManager;
    }

    @Override
    public void Op(int CID, byte[] requests, MessageContext msgCtx) {

    }

    @Override
    public void noOp(int CID, byte[][] operations, MessageContext[] msgCtx) {

    }

    @Override
    public byte[] executeOrdered(byte[] command, MessageContext msgCtx) {
        return new byte[0];
    }

    public static void main(String[] args) {

    }
}
