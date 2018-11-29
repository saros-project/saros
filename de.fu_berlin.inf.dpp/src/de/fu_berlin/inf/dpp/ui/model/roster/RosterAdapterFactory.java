package de.fu_berlin.inf.dpp.ui.model.roster;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.eclipse.core.runtime.IAdapterFactory;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

/**
 * Provides adapters for {@link Roster} entities which are provided by {@link
 * RosterContentProvider}.
 *
 * <p>E.g. let's you adapt {@link RosterGroupElement} to {@link RosterGroup} with
 *
 * <pre>
 * RosterGroup rosterGroup = (RosterGroup) rosterGroupElement
 *     .getAdapter(RosterGroup.class);
 * if (rosterGroup != null)
 *     return true;
 * else
 *     return false;
 * </pre>
 *
 * <p><b>IMPORTANT:</b> If you update this class, please also update the extension <code>
 * org.eclipse.core.runtime.adapters</code> in <code>plugin.xml</code>!<br>
 * Eclipse needs to know which object can be adapted to which type.
 *
 * @author bkahlert
 */
public class RosterAdapterFactory implements IAdapterFactory {

  @Override
  @SuppressWarnings("rawtypes")
  public Class[] getAdapterList() {
    return new Class[] {RosterGroup.class, RosterEntry.class, JID.class};
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (adaptableObject instanceof RosterGroupElement) {
      if (adapterType == RosterGroup.class) {
        return ((RosterGroupElement) adaptableObject).getRosterGroup();
      }
    }

    if (adaptableObject instanceof RosterEntryElement) {
      if (adapterType == RosterEntry.class)
        return ((RosterEntryElement) adaptableObject).getRosterEntry();
      if (adapterType == JID.class) return ((RosterEntryElement) adaptableObject).getJID();
    }

    return null;
  }
}
