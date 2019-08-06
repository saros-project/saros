package saros.stf.server.rmi.htmlbot.impl;

import java.rmi.RemoteException;
import java.util.List;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.jquery.ISelector;
import saros.stf.server.bot.jquery.ISelector.CssClassSelector;
import saros.stf.server.bot.jquery.JQueryHelper;
import saros.stf.server.rmi.htmlbot.IHTMLBot;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLView;
import saros.stf.server.rmi.htmlbot.widget.impl.RemoteHTMLView;
import saros.ui.View;
import saros.ui.browser.IBrowser;
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
    return new JQueryHelper(getBrowser(MainPage.class)).getListItemsText(SELECTOR_ACCOUNT_ENTRY);
  }

  @Override
  public List<String> getContactList(View view) throws RemoteException {
    return new JQueryHelper(getBrowser(view.getPageClass()))
        .getListItemsText(SELECTOR_CONTACT_ITEM_DISPLAY_NAME);
  }

  private IBrowser getBrowser(Class<? extends IBrowserPage> browserPageClass) {
    return getBrowserManager().getBrowser(browserPageClass);
  }

  public static IHTMLBot getInstance() {
    return INSTANCE;
  }
}
