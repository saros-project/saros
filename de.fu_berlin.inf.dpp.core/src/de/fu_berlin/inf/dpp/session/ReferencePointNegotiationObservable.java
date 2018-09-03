package de.fu_berlin.inf.dpp.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.negotiation.ReferencePointNegotiation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;

/**
 * Observable which keeps track of all {@link ReferencePointNegotiation
 * referencePoint negotiations} currently running.
 */
final class ReferencePointNegotiationObservable {

    private static final Logger LOG = Logger
        .getLogger(ReferencePointNegotiationObservable.class);

    private final Map<JID, List<ReferencePointNegotiation>> negotiations = new HashMap<JID, List<ReferencePointNegotiation>>();

    /**
     * Returns the referencePoint negotiation for the given JID with the given
     * id.
     * 
     * @param jid
     *            the JID to lookup
     * @param id
     *            the ID to lookup
     * @return the current {@link ReferencePointNegotiation referencePoint
     *         negotiation} or <code>null</code> if no such negotiation exists
     */
    public synchronized ReferencePointNegotiation get(final JID jid,
        final String id) {
        final List<ReferencePointNegotiation> currentNegotiations = negotiations
            .get(jid);

        if (currentNegotiations == null || currentNegotiations.isEmpty())
            return null;

        for (final ReferencePointNegotiation negotiation : currentNegotiations)
            if (negotiation.getID().equals(id))
                return negotiation;

        return null;
    }

    /**
     * Adds a referencePoint negotiation.
     * 
     * @param negotiation
     *            the referencePoint negotiation to add
     */
    public synchronized void add(final ReferencePointNegotiation negotiation) {
        List<ReferencePointNegotiation> currentNegotiations = negotiations
            .get(negotiation.getPeer());

        if (currentNegotiations == null) {
            currentNegotiations = new ArrayList<ReferencePointNegotiation>();
            negotiations.put(negotiation.getPeer(), currentNegotiations);
        }

        for (final ReferencePointNegotiation currentNegotiation : currentNegotiations) {
            if (currentNegotiation.getID().equals(negotiation.getID())) {
                LOG.warn(negotiation.getPeer()
                    + ": a referencePoint negotiation with ID "
                    + negotiation.getID() + " is already registered");
                return;
            }
        }

        currentNegotiations.add(negotiation);
    }

    /**
     * Removes a referencePoint negotiation.
     * 
     * @param negotiation
     *            the referencePoint negotiation to remove
     */
    public synchronized void remove(ReferencePointNegotiation negotiation) {
        List<ReferencePointNegotiation> currentNegotiations = negotiations
            .get(negotiation.getPeer());

        if (currentNegotiations == null)
            currentNegotiations = Collections.emptyList();

        for (final Iterator<ReferencePointNegotiation> it = currentNegotiations
            .iterator(); it.hasNext();) {

            final ReferencePointNegotiation currentNegotiation = it.next();
            if (currentNegotiation.getID().equals(negotiation.getID())) {
                it.remove();
                return;
            }
        }

        LOG.warn(negotiation.getPeer()
            + ": a referencePoint negotiation with ID " + negotiation.getID()
            + " is not registered");

    }

    /**
     * Returns a snap shot of all currently running referencePoint negotiations.
     * 
     * @return a list of all currently running referencePoint negotiations
     */
    public synchronized List<ReferencePointNegotiation> list() {
        final List<ReferencePointNegotiation> currentNegotiations = new ArrayList<ReferencePointNegotiation>();

        for (final List<ReferencePointNegotiation> negotiationList : negotiations
            .values())
            currentNegotiations.addAll(negotiationList);

        return currentNegotiations;
    }
}
