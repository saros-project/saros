package saros.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;
import saros.SarosPluginContext;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

/** Adds tests to a contact represented by a {@link JID}. */
public class ContactPropertyTester extends PropertyTester {

  @Inject private ISarosSessionManager sessionManager;
  @Inject private XMPPContactsService contactsService;

  public ContactPropertyTester() {
    SarosPluginContext.initComponent(this);
  }

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

    if (!(receiver instanceof JID)) return false;

    final JID jid = (JID) receiver;

    if ("isInSarosSession".equals(property)) {

      final ISarosSession session = sessionManager.getSession();

      return session != null && session.getUsers().stream().anyMatch(u -> u.getJID().equals(jid));
    } else if ("isOnline".equals(property)) {
      return contactsService
          .getContact(jid.getBase())
          .map(contact -> contact.getStatus().isOnline())
          .orElse(false);
    }

    return false;
  }
}
