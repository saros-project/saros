package de.fu_berlin.inf.dpp.net.business;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The ExecutorService under which all incoming activities should be executed.
 * 
 */
@Component(module = "Core")
public class DispatchThreadContext {

    private static final Logger log = Logger
        .getLogger(DispatchThreadContext.class);

    protected ExecutorService dispatch = Executors
        .newSingleThreadExecutor(new NamedThreadFactory(
            "XMPPChatTransmitter-Dispatch-"));

    /**
     * Execute the given runnable as if it was received via the network
     * component.
     * 
     * This is used by the ConcurrentDocumentManager to skip sending a
     * JupiterActivity via the network which originated on the host to the
     * JupiterServer.
     */
    public void executeAsDispatch(Runnable runnable) {
        dispatch.submit(Util.wrapSafe(log, runnable));
    }

    public ExecutorService getDispatchExecutor() {
        return dispatch;
    }

}
