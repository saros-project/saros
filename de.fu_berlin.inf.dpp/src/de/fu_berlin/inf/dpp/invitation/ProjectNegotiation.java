package de.fu_berlin.inf.dpp.invitation;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.exceptions.StreamException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.StreamServiceManager;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.net.internal.StreamSession.StreamSessionListener;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * 
 * 
 * This abstract class is the superclass for {@link OutgoingProjectNegotiation}
 * and {@link IncomingProjectNegotiation}.
 */
public abstract class ProjectNegotiation {

    private static Logger log = Logger.getLogger(ProjectNegotiation.class);

    protected ProjectNegotiationObservable projectExchangeProcesses;
    protected JID peer;
    protected ITransmitter transmitter;
    protected boolean error = false;
    protected StreamSession streamSession;

    @Inject
    protected StreamServiceManager streamServiceManager;
    @Inject
    protected ArchiveStreamService archiveStreamService;
    @Inject
    protected SarosSessionManager sessionManager;

    protected StreamSessionListener sessionListener = new StreamSessionListener() {

        public void sessionStopped() {
            if (streamSession != null) {
                streamSession.shutdownFinished();
                streamSession = null;
            }
        }

        public void errorOccured(StreamException e) {
            log.error("Got error while streaming project archive: ", e);
            error = true;
        }
    };

    public ProjectNegotiation(ITransmitter transmitter, JID peer,
        ProjectNegotiationObservable projectExchangeProcesses) {
        this.transmitter = transmitter;
        this.peer = peer;
        this.projectExchangeProcesses = projectExchangeProcesses;
        this.projectExchangeProcesses.addProjectExchangeProcess(this);

        Saros.injectDependenciesOnly(this);
    }

    /**
     * 
     * @return the name of the project that is shared by the peer.
     */
    public abstract String getProjectName();

    public abstract String getProjectID();

    public JID getPeer() {
        return this.peer;
    }

    /**
     * 
     * @param errorMsg
     */
    public abstract void remoteCancel(String errorMsg);
}
