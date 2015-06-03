package de.fu_berlin.inf.dpp.observables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * Observable which keeps track of all {@link ProjectNegotiation project
 * negotiations} currently running.
 *
 * This class is used to that everybody can have an easy access to the project
 * negotiations.
 */
@Component(module = "observables")
public class ProjectNegotiationObservable {

    private static Logger log = Logger
        .getLogger(ProjectNegotiationObservable.class);

    private int addings = 0;

    // FIXME you can have multiple negotiations !!!!!!!!!!!!!!!!!!!!
    /**
     * JID => ProjectNegotiation
     *
     * You can have only one ProjectNegotiation with each session member.
     */
    protected Map<JID, ProjectNegotiation> negotiations = new HashMap<JID, ProjectNegotiation>();

    /**
     * Get the project negotiation with the session participant
     * <code><b>jid</b></code>.
     *
     * @return the ongoing project negotiation between the local user and the
     *         user with the JID <code><b>jid</b></code> or
     *         <code><b>null</b></code>
     */
    public synchronized ProjectNegotiation get(JID jid) {
        return this.negotiations.get(jid);
    }

    public synchronized void add(ProjectNegotiation negotiation) {
        addings++;
        log.info("This is ProjectNegotiation number " + addings
            + " in this session. There are " + negotiations.size()
            + " negotiation(s) in this session");

        ProjectNegotiation oldNegotiation = this.negotiations.put(
            negotiation.getPeer(), negotiation);
        if (oldNegotiation != null) {
            log.error(
                "An internal error occurred:"
                    + " An existing ProjectNegotiation with "
                    + oldNegotiation.getPeer() + " was replaced by a new one",
                new StackTrace());
        }
    }

    public synchronized void remove(ProjectNegotiation negotiation) {
        if (this.negotiations.remove(negotiation.getPeer()) == null) {
            log.error("An internal error occurred:"
                + " No ProjectNegotiation with " + negotiation.getPeer()
                + " could be found", new StackTrace());
        }
    }

    public synchronized Collection<ProjectNegotiation> list() {
        Collection<ProjectNegotiation> currentNegotiations = new ArrayList<ProjectNegotiation>();
        currentNegotiations.addAll(negotiations.values());
        return currentNegotiations;
    }
}
