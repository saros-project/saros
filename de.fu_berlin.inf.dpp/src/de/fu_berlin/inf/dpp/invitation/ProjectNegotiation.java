package de.fu_berlin.inf.dpp.invitation;

import java.util.Map;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosContext;
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
    protected String processID;

    /**
     * While sending all the projects with a big archive containing the project
     * archives, we create a temp-File. This file is named "projectID" +
     * projectIDDelimiter + "a random number chosen by 'Java'" + ".zip" This
     * delimiter is the string that separates projectID and this random number.
     * Now we can assign the zip archive to the matching project.
     * 
     * WARNING: If changed compatibility is broken
     */
    protected final String projectIDDelimiter = "&&&&";

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
        ProjectNegotiationObservable projectExchangeProcesses,
        SarosContext sarosContext) {
        this.transmitter = transmitter;
        this.peer = peer;
        this.projectExchangeProcesses = projectExchangeProcesses;
        this.projectExchangeProcesses.addProjectExchangeProcess(this);

        sarosContext.initComponent(this);
    }

    /**
     * 
     * @return the names of the projects that are shared by the peer. projectID
     *         => projectName
     */
    public abstract Map<String, String> getProjectNames();

    public abstract String getProcessID();

    public JID getPeer() {
        return this.peer;
    }

    /**
     * 
     * @param errorMsg
     */
    public abstract void remoteCancel(String errorMsg);
}
