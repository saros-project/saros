package de.fu_berlin.inf.dpp.observables;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Observable which keeps track of all InvitationProcesses currently running.
 * 
 * This class is used to that everybody can have an easy access to the
 * InvitationProcesses.
 */
@Component(module = "observables")
public class InvitationProcessObservable {

    private static Logger log = Logger
        .getLogger(InvitationProcessObservable.class);

    protected Map<JID, InvitationProcess> processes = new HashMap<JID, InvitationProcess>();

    public synchronized InvitationProcess getInvitationProcess(JID jid) {
        return this.processes.get(jid);
    }

    public synchronized void addInvitationProcess(InvitationProcess process) {
        InvitationProcess oldProcess = this.processes.put(process.getPeer(),
            process);
        if (oldProcess != null) {
            log.error("An internal error occurred:"
                + " An existing invititation process with "
                + Utils.prefix(oldProcess.getPeer())
                + " was replace by a new one", new StackTrace());
        }
    }

    public synchronized void removeInvitationProcess(InvitationProcess process) {
        if (this.processes.remove(process.getPeer()) == null) {
            log.error("An internal error occurred:"
                + " No invititation process with "
                + Utils.prefix(process.getPeer()) + " could be found",
                new StackTrace());
        }
    }

}
