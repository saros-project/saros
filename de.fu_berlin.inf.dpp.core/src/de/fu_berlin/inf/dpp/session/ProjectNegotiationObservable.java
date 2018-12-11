package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Observable which keeps track of all {@link ProjectNegotiation project negotiations} currently
 * running.
 */
final class ProjectNegotiationObservable {

  private static final Logger LOG = Logger.getLogger(ProjectNegotiationObservable.class);

  private final Map<JID, List<ProjectNegotiation>> negotiations =
      new HashMap<JID, List<ProjectNegotiation>>();

  /**
   * Returns the project negotiation for the given JID with the given id.
   *
   * @param jid the JID to lookup
   * @param id the ID to lookup
   * @return the current {@link ProjectNegotiation project negotiation} or <code>null</code> if no
   *     such negotiation exists
   */
  public synchronized ProjectNegotiation get(final JID jid, final String id) {
    final List<ProjectNegotiation> currentNegotiations = negotiations.get(jid);

    if (currentNegotiations == null || currentNegotiations.isEmpty()) return null;

    for (final ProjectNegotiation negotiation : currentNegotiations)
      if (negotiation.getID().equals(id)) return negotiation;

    return null;
  }

  /**
   * Adds a project negotiation.
   *
   * @param negotiation the project negotiation to add
   */
  public synchronized void add(final ProjectNegotiation negotiation) {
    List<ProjectNegotiation> currentNegotiations = negotiations.get(negotiation.getPeer());

    if (currentNegotiations == null) {
      currentNegotiations = new ArrayList<ProjectNegotiation>();
      negotiations.put(negotiation.getPeer(), currentNegotiations);
    }

    for (final ProjectNegotiation currentNegotiation : currentNegotiations) {
      if (currentNegotiation.getID().equals(negotiation.getID())) {
        LOG.warn(
            negotiation.getPeer()
                + ": a project negotiation with ID "
                + negotiation.getID()
                + " is already registered");
        return;
      }
    }

    currentNegotiations.add(negotiation);
  }

  /**
   * Removes a project negotiation.
   *
   * @param negotiation the project negotiation to remove
   */
  public synchronized void remove(ProjectNegotiation negotiation) {
    List<ProjectNegotiation> currentNegotiations = negotiations.get(negotiation.getPeer());

    if (currentNegotiations == null) currentNegotiations = Collections.emptyList();

    for (final Iterator<ProjectNegotiation> it = currentNegotiations.iterator(); it.hasNext(); ) {

      final ProjectNegotiation currentNegotiation = it.next();
      if (currentNegotiation.getID().equals(negotiation.getID())) {
        it.remove();
        return;
      }
    }

    LOG.warn(
        negotiation.getPeer()
            + ": a project negotiation with ID "
            + negotiation.getID()
            + " is not registered");
  }

  /**
   * Returns a snap shot of all currently running project negotiations.
   *
   * @return a list of all currently running project negotiations
   */
  public synchronized List<ProjectNegotiation> list() {
    final List<ProjectNegotiation> currentNegotiations = new ArrayList<ProjectNegotiation>();

    for (final List<ProjectNegotiation> negotiationList : negotiations.values())
      currentNegotiations.addAll(negotiationList);

    return currentNegotiations;
  }

  /**
   * Returns <tt>true</tt> if no running negotiations exist.
   *
   * @return <tt>true</tt> if no running negotiations exist
   */
  public synchronized boolean isEmpty() {
    for (List<ProjectNegotiation> negotiationList : negotiations.values()) {
      if (!negotiationList.isEmpty()) {
        return false;
      }
    }
    return true;
  }
}
