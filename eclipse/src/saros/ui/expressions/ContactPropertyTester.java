package saros.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;

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
