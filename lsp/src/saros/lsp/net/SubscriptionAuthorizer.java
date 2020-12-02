package saros.lsp.net;

import java.util.Optional;
import saros.lsp.ui.UIInteractionManager;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.net.xmpp.subscription.SubscriptionHandler;
import saros.net.xmpp.subscription.SubscriptionListener;

/**
 * Authorizer for incoming subscription requests which will ask the user for consent in that case.
 */
public class SubscriptionAuthorizer implements SubscriptionListener {

  private SubscriptionHandler subscriptionHandler;
  private XMPPContactsService contactsService;
  private UIInteractionManager interactionManager;

  public SubscriptionAuthorizer(
      SubscriptionHandler subscriptionHandler,
      XMPPContactsService contactsService,
      UIInteractionManager interactionManager) {
    this.subscriptionHandler = subscriptionHandler;
    this.contactsService = contactsService;
    this.interactionManager = interactionManager;
    subscriptionHandler.addSubscriptionListener(this);
  }

  @Override
  public void subscriptionRequestReceived(JID jid) {
    String title = String.format("User '%s' requested subscription", jid.getName());
    String message = String.format("Allow subscription from '%s'?", jid.getBase());

    if (this.interactionManager.getUserInputYesNo(title, message)) {
      subscriptionHandler.addSubscription(jid, true);
    }
  }

  @Override
  public void subscriptionCanceled(JID jid) {
    subscriptionHandler.removeSubscription(jid);
    Optional<XMPPContact> contactResult = this.contactsService.getContact(jid.getBase());
    if (contactResult.isPresent()) {
      XMPPContact contact = contactResult.get();
      String title =
          String.format("User '%s' cancelled subscription", contact.getDisplayableName());
      String message =
          String.format("Remove user '%s' from contact list?", contact.getDisplayableName());

      if (this.interactionManager.getUserInputYesNo(title, message)) {
        this.contactsService.removeContact(contact);
      }
    }
  }
}
