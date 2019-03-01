package saros.stf.server.rmi.htmlbot.impl;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.CssClassSelector;
import java.rmi.RemoteException;
import java.util.List;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.BotUtils;
import saros.stf.server.rmi.htmlbot.IHTMLBot;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLView;
import saros.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLView;
import saros.ui.View;
import saros.ui.pages.IBrowserPage;
import saros.ui.pages.MainPage;

public class HTMLBotImpl extends HTMLSTFRemoteObject implements IHTMLBot {

  private static final IHTMLBot INSTANCE = new HTMLBotImpl();

  private static final ISelector SELECTOR_ACCOUNT_ENTRY = new CssClassSelector("accountEntry");
  private static final ISelector SELECTOR_CONTACT_ITEM_DISPLAY_NAME =
      new CssClassSelector("contact-item-display-name");

  private RemoteHTMLView view;

  public HTMLBotImpl() {
    view = RemoteHTMLView.getInstance();
  }

  @Override
  public IRemoteHTMLView view(View view) {
    this.view.selectView(view);
    return this.view;
  }

  @Override
  public List<String> getAccountList() throws RemoteException {
    return BotUtils.getListItemsText(getBrowser(MainPage.class), SELECTOR_ACCOUNT_ENTRY);
  }

  @Override
  public List<String> getContactList(View view) throws RemoteException {
    return BotUtils.getListItemsText(
        getBrowser(view.getPageClass()), SELECTOR_CONTACT_ITEM_DISPLAY_NAME);
  }

  private IJQueryBrowser getBrowser(Class<? extends IBrowserPage> browserPageClass) {
    return getBrowserManager().getBrowser(browserPageClass);
  }

  public static IHTMLBot getInstance() {
    return INSTANCE;
  }
}
