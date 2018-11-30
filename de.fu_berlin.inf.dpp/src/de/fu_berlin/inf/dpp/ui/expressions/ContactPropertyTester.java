package de.fu_berlin.inf.dpp.ui.expressions;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import org.eclipse.core.expressions.PropertyTester;
import org.picocontainer.annotations.Inject;

/** Adds tests to a contact represented by a {@link JID}. */
public class ContactPropertyTester extends PropertyTester {

  @Inject private ISarosSessionManager sessionManager;

  public ContactPropertyTester() {
    SarosPluginContext.initComponent(this);
  }

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

    if (!(receiver instanceof JID)) return false;

    final ISarosSession session = sessionManager.getSession();

    if (session == null) return false;

    final JID jid = (JID) receiver;

    if ("isInSarosSession".equals(property)) {
      for (final User user : session.getUsers()) {
        if (user.getJID().equals(jid)) return true;
      }
    }

    return false;
  }
}
