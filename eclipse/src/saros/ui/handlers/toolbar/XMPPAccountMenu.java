package saros.ui.handlers.toolbar;

import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;

public class XMPPAccountMenu {

  @Inject private XMPPAccountStore accountService;

  @Inject private ConnectionHandler connectionHandler;

  public XMPPAccountMenu() {
    SarosPluginContext.initComponent(this);
  }

  @AboutToShow
  public void createMenu(List<MMenuElement> items, EModelService service) {
    final List<XMPPAccount> accounts = accountService.getAllAccounts();

    final String connectionId = connectionHandler.getConnectionID();

    if (connectionHandler.isConnected() && connectionId != null) {

      final JID jid = new JID(connectionId);

      /*
       *  TODO this may filter out too much but this situation is somewhat rare (multiple accounts
       *  with same name and domain but different server
       */

      accounts.removeIf(
          a ->
              a.getUsername().equalsIgnoreCase(jid.getName())
                  && a.getDomain().equalsIgnoreCase(jid.getDomain()));
    }

    accounts.forEach(
        account -> {
          // The additional @ is needed because @ has special meaning in
          // Action#setText(), see JavaDoc of Action().

          String accountText = account.getUsername() + "@" + account.getDomain();

          MDirectMenuItem accountElement = service.createModelElement(MDirectMenuItem.class);
          accountElement.setContributionURI(
              "bundleclass://saros.eclipse/saros.ui.e4.toolbar.ChangeXMPPAccountHandler$AddAccountToMenuHandler");
          accountElement.setLabel(accountText);

          Map<String, Object> objectData = accountElement.getTransientData();
          objectData.put("account", account);

          items.add(accountElement);
        });
  }
}
