package saros.ui.model.roster;

import org.eclipse.core.runtime.IAdapterFactory;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;

/**
 * Provides adapters for contact entities which are provided by {@link RosterContentProvider}.
 *
 * <p>E.g. let's you adapt {@link RosterEntryElement} to {@link JID} with
 *
 * <pre>
 * JID jid = rosterEntryElement.getAdapter(JID.class)
 *
 * if (jid != null)
 *     return true;
 * else
 *     return false;
 * </pre>
 *
 * <p><b>IMPORTANT:</b> If you update this class, please also update the extension <code>
 * org.eclipse.core.runtime.adapters</code> in <code>plugin.xml</code>!<br>
 * Eclipse needs to know which object can be adapted to which type.
 */
public class RosterAdapterFactory implements IAdapterFactory {

  @Override
  public Class<?>[] getAdapterList() {
    return new Class[] {XMPPContact.class, JID.class};
  }

  @Override
  public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
    if (adaptableObject instanceof RosterEntryElement) {
      if (adapterType == XMPPContact.class)
        return adapterType.cast(((RosterEntryElement) adaptableObject).getContact());
      if (adapterType == JID.class)
        return adapterType.cast(((RosterEntryElement) adaptableObject).getJID());
    }

    return null;
  }
}
