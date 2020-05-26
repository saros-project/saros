package saros.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import saros.negotiation.ResourceNegotiation;
import saros.net.xmpp.JID;

/**
 * Observable which keeps track of all {@link ResourceNegotiation project negotiations} currently
 * running.
 */
final class ResourceNegotiationObservable {

  private static final Logger log = Logger.getLogger(ResourceNegotiationObservable.class);

  private final Map<JID, List<ResourceNegotiation>> negotiations =
      new HashMap<JID, List<ResourceNegotiation>>();

  /**
   * Returns the project negotiation for the given JID with the given id.
   *
   * @param jid the JID to lookup
   * @param id the ID to lookup
   * @return the current {@link ResourceNegotiation project negotiation} or <code>null</code> if no
   *     such negotiation exists
   */
  public synchronized ResourceNegotiation get(final JID jid, final String id) {
    final List<ResourceNegotiation> currentNegotiations = negotiations.get(jid);

    if (currentNegotiations == null || currentNegotiations.isEmpty()) return null;

    for (final ResourceNegotiation negotiation : currentNegotiations)
      if (negotiation.getID().equals(id)) return negotiation;

    return null;
  }

  /**
   * Adds a project negotiation.
   *
   * @param negotiation the project negotiation to add
   */
  public synchronized void add(final ResourceNegotiation negotiation) {
    List<ResourceNegotiation> currentNegotiations = negotiations.get(negotiation.getPeer());

    if (currentNegotiations == null) {
      currentNegotiations = new ArrayList<ResourceNegotiation>();
      negotiations.put(negotiation.getPeer(), currentNegotiations);
    }

    for (final ResourceNegotiation currentNegotiation : currentNegotiations) {
      if (currentNegotiation.getID().equals(negotiation.getID())) {
        log.warn(
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
  public synchronized void remove(ResourceNegotiation negotiation) {
    List<ResourceNegotiation> currentNegotiations = negotiations.get(negotiation.getPeer());

    if (currentNegotiations == null) currentNegotiations = Collections.emptyList();

    for (final Iterator<ResourceNegotiation> it = currentNegotiations.iterator(); it.hasNext(); ) {

      final ResourceNegotiation currentNegotiation = it.next();
      if (currentNegotiation.getID().equals(negotiation.getID())) {
        it.remove();
        return;
      }
    }

    log.warn(
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
  public synchronized List<ResourceNegotiation> list() {
    final List<ResourceNegotiation> currentNegotiations = new ArrayList<ResourceNegotiation>();

    for (final List<ResourceNegotiation> negotiationList : negotiations.values())
      currentNegotiations.addAll(negotiationList);

    return currentNegotiations;
  }

  /**
   * Returns <tt>true</tt> if no running negotiations exist.
   *
   * @return <tt>true</tt> if no running negotiations exist
   */
  public synchronized boolean isEmpty() {
    for (List<ResourceNegotiation> negotiationList : negotiations.values()) {
      if (!negotiationList.isEmpty()) {
        return false;
      }
    }
    return true;
  }
}
