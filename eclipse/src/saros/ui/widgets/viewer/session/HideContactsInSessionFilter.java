package saros.ui.widgets.viewer.session;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jivesoftware.smack.Roster;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;
import saros.ui.model.roster.RosterEntryElement;

/**
 * This filter is responsible for hiding those contacts in the {@linkplain Roster contact list} that
 * are currently part of the running Saros session.
 *
 * @author srossbach
 * @author waldmann
 */
public final class HideContactsInSessionFilter extends ViewerFilter {

  private final ISarosSession session;

  public HideContactsInSessionFilter(ISarosSession session) {
    this.session = session;
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {

    if (!(element instanceof RosterEntryElement)) return true;

    RosterEntryElement entry = (RosterEntryElement) element;

    // Don't filter out the groups
    if (entry.getChildren().length != 0) return true;

    JID rqJID = session.getResourceQualifiedJID(entry.getJID());

    return rqJID == null || session.getUser(rqJID) == null;
  }
}
