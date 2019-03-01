package saros.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import saros.negotiation.SessionNegotiation;
import saros.net.xmpp.JID;

/** Observable which keeps track of all session negotiations currently running. */
// TODO remove the ID part here, there should only be one invitation per JID
final class SessionNegotiationObservable {

  private static final Logger LOG = Logger.getLogger(SessionNegotiationObservable.class);

  private final Map<JID, List<SessionNegotiation>> negotiations =
      new HashMap<JID, List<SessionNegotiation>>();

  /**
   * Returns the session negotiation for the given JID with the given id.
   *
   * @param jid the JID to lookup
   * @param id the ID to lookup
   * @return the current {@link SessionNegotiation session negotiation} or <code>null</code> if no
   *     such negotiation exists
   */
  public synchronized SessionNegotiation get(final JID jid, final String id) {
    final List<SessionNegotiation> currentNegotiations = negotiations.get(jid);

    if (currentNegotiations == null || currentNegotiations.isEmpty()) return null;

    for (final SessionNegotiation negotiation : currentNegotiations)
      if (negotiation.getID().equals(id)) return negotiation;

    return null;
  }

  /**
   * Checks if a session negotiation currently exists for the given JID.
   *
   * @param jid the JID to lookup
   * @return <code>true</code> if a session negotiation exists for the current JID, <code>false
   *     </code> otherwise
   */
  public synchronized boolean exists(final JID jid) {
    final List<SessionNegotiation> currentNegotiations = negotiations.get(jid);
    return currentNegotiations != null && !currentNegotiations.isEmpty();
  }

  /**
   * Adds a session negotiation.
   *
   * @param negotiation the session negotiation to add
   */
  public synchronized void add(final SessionNegotiation negotiation) {
    List<SessionNegotiation> currentNegotiations = negotiations.get(negotiation.getPeer());

    if (currentNegotiations == null) {
      currentNegotiations = new ArrayList<SessionNegotiation>();
      negotiations.put(negotiation.getPeer(), currentNegotiations);
    }

    if (!currentNegotiations.isEmpty())
      LOG.warn(
          "there is already a running session negotiation for contact: " + negotiation.getPeer());

    for (final SessionNegotiation currentNegotiation : currentNegotiations) {
      if (currentNegotiation.getID().equals(negotiation.getID())) {
        LOG.warn("a session negotiation with ID " + negotiation.getID() + " is already registered");
        return;
      }
    }

    currentNegotiations.add(negotiation);
  }

  /**
   * Removes a session negotiation.
   *
   * @param negotiation the session negotiation to remove
   */
  public synchronized void remove(SessionNegotiation negotiation) {
    List<SessionNegotiation> currentNegotiations = negotiations.get(negotiation.getPeer());

    if (currentNegotiations == null) currentNegotiations = Collections.emptyList();

    for (final Iterator<SessionNegotiation> it = currentNegotiations.iterator(); it.hasNext(); ) {

      final SessionNegotiation currentNegotiation = it.next();
      if (currentNegotiation.getID().equals(negotiation.getID())) {
        it.remove();
        return;
      }
    }

    LOG.warn("a session negotiation with ID " + negotiation.getID() + " is not registered");
  }

  /**
   * Returns a snap shot of all currently running session negotiation negotiations.
   *
   * @return a list of all currently running session negotiation negotiations
   */
  public synchronized List<SessionNegotiation> list() {
    final List<SessionNegotiation> currentNegotiations = new ArrayList<SessionNegotiation>();

    for (final List<SessionNegotiation> negotiationList : negotiations.values())
      currentNegotiations.addAll(negotiationList);

    return currentNegotiations;
  }
}
