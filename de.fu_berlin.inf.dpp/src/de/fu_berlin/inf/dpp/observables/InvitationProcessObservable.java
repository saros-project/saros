package de.fu_berlin.inf.dpp.observables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;

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

    private Map<JID, List<InvitationProcess>> processes = new HashMap<JID, List<InvitationProcess>>();

    /**
     * Returns an invitation process from the currently running invitation
     * processes.
     * 
     * @param jid
     *            the JID of the remote contact that is part of the invitation
     *            process
     * @param id
     *            the ID of the invitation process
     * @return an {@link InvitationProcess} object or <code>null</code> if no
     *         such process exists
     */
    public synchronized InvitationProcess getInvitationProcess(JID jid,
        String id) {
        List<InvitationProcess> currentProcesses = processes.get(jid);

        if (currentProcesses == null || currentProcesses.isEmpty())
            return null;

        for (InvitationProcess process : currentProcesses)
            if (process.getID().equals(id))
                return process;

        return null;
    }

    /**
     * Adds an invitation process to the current set.
     * 
     * @param process
     *            the process to add
     */
    public synchronized void addInvitationProcess(InvitationProcess process) {
        List<InvitationProcess> currentProcesses = processes.get(process
            .getPeer());

        if (currentProcesses == null) {
            currentProcesses = new ArrayList<InvitationProcess>();
            processes.put(process.getPeer(), currentProcesses);
        }

        if (currentProcesses.size() >= 1)
            log.warn("there is already a running invitation for contact: "
                + process.getPeer());

        for (InvitationProcess currentProcess : currentProcesses) {
            if (currentProcess.getID().equals(process.getID())) {
                log.warn("an invitation with ID " + process.getID()
                    + " is already registered");
                return;
            }
        }

        currentProcesses.add(process);
    }

    /**
     * Removes an invitation process from the current set.
     * 
     * @param process
     *            the process to remove
     */

    public synchronized void removeInvitationProcess(InvitationProcess process) {
        List<InvitationProcess> currentProcesses = processes.get(process
            .getPeer());

        if (currentProcesses == null)
            currentProcesses = Collections.emptyList();

        for (Iterator<InvitationProcess> it = currentProcesses.iterator(); it
            .hasNext();) {

            InvitationProcess currentProcess = it.next();
            if (currentProcess.getID().equals(process.getID())) {
                it.remove();
                return;
            }
        }

        log.warn("an invitation with ID " + process.getID()
            + " is not registered");

    }

    /**
     * Returns a snap shot of all currently running invitation processes.
     * 
     * @return a list of the currently running invitation processes which may be
     *         empty
     */
    public synchronized List<InvitationProcess> getProcesses() {
        List<InvitationProcess> runningProcesses = new ArrayList<InvitationProcess>();

        for (List<InvitationProcess> processes : this.processes.values())
            runningProcesses.addAll(processes);

        return runningProcesses;
    }
}
