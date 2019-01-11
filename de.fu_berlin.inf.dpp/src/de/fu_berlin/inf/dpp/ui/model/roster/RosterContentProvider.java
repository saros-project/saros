package de.fu_berlin.inf.dpp.ui.model.roster;

import de.fu_berlin.inf.dpp.SarosConstants;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.discovery.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.xmpp.discovery.DiscoveryManagerListener;
import de.fu_berlin.inf.dpp.ui.model.TreeContentProvider;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

/**
 * {@link IContentProvider} for use in conjunction with a {@link Roster} input.
 *
 * <p>Automatically keeps track of changes of contacts.
 *
 * @author bkahlert
 */
public final class RosterContentProvider extends TreeContentProvider {

  private Viewer viewer;
  private volatile Roster roster;

  @Inject private volatile DiscoveryManager discoveryManager;

  private final DiscoveryManagerListener discoveryManagerListener =
      new DiscoveryManagerListener() {
        @Override
        public void featureSupportUpdated(final JID jid, String feature, boolean isSupported) {

          // TODO maybe use display.timerExec to avoid massive refresh calls
          ViewerUtils.refresh(viewer, true);
        }
      };

  private final RosterListener rosterListener =
      new RosterListener() {
        @Override
        public void presenceChanged(Presence presence) {
          ViewerUtils.refresh(viewer, true);

          final String user = presence.getFrom();

          if (user != null) querySarosSupport(Collections.singletonList(user));
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
          ViewerUtils.refresh(viewer, true);
          querySarosSupport(addresses);
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
          ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void entriesAdded(Collection<String> addresses) {
          ViewerUtils.refresh(viewer, true);
          querySarosSupport(addresses);
        }
      };

  public RosterContentProvider() {
    SarosPluginContext.initComponent(this);

    discoveryManager.addDiscoveryManagerListener(discoveryManagerListener);
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    this.viewer = viewer;

    if (oldInput instanceof Roster) ((Roster) oldInput).removeRosterListener(rosterListener);

    roster = null;

    if (newInput instanceof Roster) {
      roster = (Roster) newInput;
      roster.addRosterListener(rosterListener);

      for (final RosterEntry entry : roster.getEntries())
        querySarosSupport(Collections.singletonList(entry.getUser()));
    }
  }

  @Override
  public void dispose() {
    if (roster != null) roster.removeRosterListener(rosterListener);

    discoveryManager.removeDiscoveryManagerListener(discoveryManagerListener);

    roster = null;
    discoveryManager = null;
  }

  /**
   * Returns {@link RosterGroup}s followed by {@link RosterEntry}s which don't belong to any {@link
   * RosterGroup}.
   */
  @Override
  public Object[] getElements(Object inputElement) {

    if (!(inputElement instanceof Roster)) return new Object[0];

    Roster inputRoster = (Roster) inputElement;
    final List<Object> elements = new ArrayList<Object>();

    /*
     * always show contacts that support Saros regardless of any other
     * source, i.e the user is online with:
     *
     * alice@foo/Saros (<- has Saros support)
     *
     * and
     *
     * alice@foo/Pidgen
     *
     * so always display alice@foo/Saros
     */

    for (RosterGroup group : inputRoster.getGroups()) {
      elements.add(
          new RosterGroupElement(
              group,
              filterRosterEntryElements(createRosterEntryElements(group.getEntries()))
                  .toArray(new RosterEntryElement[0])));
    }

    elements.addAll(
        filterRosterEntryElements(createRosterEntryElements(inputRoster.getUnfiledEntries())));

    return elements.toArray();
  }

  private List<RosterEntryElement> createRosterEntryElements(
      final Collection<RosterEntry> entries) {

    final List<RosterEntryElement> elements = new ArrayList<RosterEntryElement>();

    for (final RosterEntry entry : entries)
      elements.add(createRosterEntryElement(new JID(entry.getUser())));

    return elements;
  }

  private RosterEntryElement createRosterEntryElement(final JID jid) {
    final Boolean isSarosSupport =
        discoveryManager.isFeatureSupported(jid, SarosConstants.XMPP_FEATURE_NAMESPACE);

    return new RosterEntryElement(roster, jid, isSarosSupport == null ? false : isSarosSupport);
  }

  private void querySarosSupport(Collection<String> users) {

    final Roster currentRoster = roster;
    final DiscoveryManager currentDiscoveryManager = discoveryManager;

    if (currentRoster == null || currentDiscoveryManager == null) return;

    for (final String user : users) {
      final JID jid = new JID(user);

      if (!currentRoster.getPresence(jid.getBase()).isAvailable()) continue;

      Boolean sarosSupported =
          discoveryManager.isFeatureSupported(jid, SarosConstants.XMPP_FEATURE_NAMESPACE);

      if (sarosSupported == null)
        discoveryManager.queryFeatureSupport(jid, SarosConstants.XMPP_FEATURE_NAMESPACE, true);
    }
  }

  /**
   * Filters the given roster entry elements by removing entries which bare JID are equal.
   * Furthermore if two entries are equal the one with possible Saros support will always be kept
   * and the other one will be discarded.
   */
  private final List<RosterEntryElement> filterRosterEntryElements(
      final Collection<RosterEntryElement> elements) {

    final Map<JID, RosterEntryElement> filteredElements =
        new HashMap<JID, RosterEntryElement>(elements.size());

    for (final RosterEntryElement element : elements) {

      final JID bareJID = element.getJID().getBareJID();

      final RosterEntryElement filteredElement = filteredElements.get(bareJID);

      if (filteredElement != null && filteredElement.isSarosSupported()) {
        continue;
      } else if (filteredElement != null && !filteredElement.isSarosSupported()) {
        filteredElements.remove(bareJID);
      }

      filteredElements.put(bareJID, element);
    }

    return new ArrayList<RosterEntryElement>(filteredElements.values());
  }
}
