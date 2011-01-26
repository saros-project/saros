package de.fu_berlin.inf.dpp.observables;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Observable which keeps track of all ProjectNegotiationProcesses currently
 * running.
 * 
 * This class is used to that everybody can have an easy access to the
 * ProjectNegotiationProcesses.
 */
@Component(module = "observables")
public class ProjectNegotiationObservable {

    private static Logger log = Logger
        .getLogger(ProjectNegotiationObservable.class);

    private int addings = 0;

    protected Map<JID, ProjectNegotiation> processes = new HashMap<JID, ProjectNegotiation>();

    /**
     * Get the projectExchangeProcess with the session participant
     * <code><b>jid</b></code>
     * 
     * @return the ongoing ProjectNegotiation between the local user and the
     *         user with the JID <code><b>jid</b></code> or
     *         <code><b>null</b></code>
     */
    public synchronized ProjectNegotiation getProjectExchangeProcess(JID jid) {
        return this.processes.get(jid);
    }

    public synchronized ProjectNegotiation getProjectExchangeProcess(
        String projectID) {
        for (ProjectNegotiation projectNegotiation : this.processes.values()) {
            if (projectNegotiation.getProjectID() == projectID) {
                return projectNegotiation;
            }
        }
        return null;
    }

    public synchronized void addProjectExchangeProcess(
        ProjectNegotiation process) {
        addings++;
        log.info("This is ProjectNegotiation number " + addings
            + " in this session. There are " + processes.size()
            + " process(es) in this session");

        ProjectNegotiation oldProcess = this.processes.put(process.getPeer(),
            process);
        if (oldProcess != null) {
            log.error(
                "An internal error occurred:"
                    + " An existing ProjectNegotiation process with "
                    + Util.prefix(oldProcess.getPeer())
                    + " was replaced by a new one", new StackTrace());
        }
    }

    public synchronized void removeProjectExchangeProcess(
        ProjectNegotiation process) {
        if (this.processes.remove(process.getPeer()) == null) {
            log.error(
                "An internal error occurred:" + " No ProjectNegotiation with "
                    + Util.prefix(process.getPeer()) + " could be found",
                new StackTrace());
        }
    }
}
