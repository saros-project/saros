package de.fu_berlin.inf.dpp.observables;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * Observable which keeps track of all InvitationProcesses currently running.
 * 
 * This class is used to that everybody can have an easy access to the
 * InvitationProcesses.
 */
public class InvitationProcessObservable {

    private static Logger log = Logger
        .getLogger(InvitationProcessObservable.class);

    protected Map<JID, IInvitationProcess> processes = new HashMap<JID, IInvitationProcess>();

    public synchronized IInvitationProcess getInvitationProcess(JID jid) {
        return this.processes.get(jid);
    }

    public synchronized void addInvitationProcess(IInvitationProcess process) {
        IInvitationProcess oldProcess = this.processes.put(process.getPeer(),
            process);
        if (oldProcess != null) {
            log.error("An internal error occurred:"
                + " An existing invititation process with "
                + oldProcess.getPeer() + " was replace by a new one",
                new StackTrace());
        }
    }

    public synchronized void removeInvitationProcess(IInvitationProcess process) {
        if (this.processes.remove(process.getPeer()) == null) {
            log.error("An internal error occurred:"
                + " No invititation process with " + process.getPeer()
                + " could be found", new StackTrace());
        }
    }

}
