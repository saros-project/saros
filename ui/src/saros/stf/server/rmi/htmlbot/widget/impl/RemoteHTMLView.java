package saros.stf.server.rmi.htmlbot.widget.impl;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.IdSelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.NameSelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.Selector;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;
import saros.stf.server.HTMLSTFRemoteObject;
import saros.stf.server.bot.BotPreferences;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLButton;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLCheckbox;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLInputField;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLMultiSelect;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLProgressBar;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLRadioGroup;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLSelect;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLTextElement;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLTree;
import saros.stf.server.rmi.htmlbot.widget.IRemoteHTMLView;
import saros.ui.View;

public class RemoteHTMLView extends HTMLSTFRemoteObject implements IRemoteHTMLView {

  private static final RemoteHTMLView INSTANCE = new RemoteHTMLView();
  private static final Logger log = Logger.getLogger(RemoteHTMLView.class);

  public static RemoteHTMLView getInstance() {
    return INSTANCE;
  }

  private View view;
  private RemoteHTMLButton button;
  private RemoteHTMLInputField inputField;
  private RemoteHTMLCheckbox checkbox;
  private RemoteHTMLRadioGroup radioGroup;
  private RemoteHTMLSelect select;
  private RemoteHTMLMultiSelect multiSelect;
  private RemoteHTMLProgressBar progressBar;
  private RemoteHTMLTextElement textElement;
  private RemoteHTMLTree tree;

  public RemoteHTMLView() {
    button = RemoteHTMLButton.getInstance();
    inputField = RemoteHTMLInputField.getInstance();
    checkbox = RemoteHTMLCheckbox.getInstance();
    radioGroup = RemoteHTMLRadioGroup.getInstance();
    select = RemoteHTMLSelect.getInstance();
    multiSelect = RemoteHTMLMultiSelect.getInstance();
    progressBar = RemoteHTMLProgressBar.getInstance();
    textElement = RemoteHTMLTextElement.getInstance();
    tree = RemoteHTMLTree.getInstance();
  }

  @Override
  public boolean hasElementWithId(String id) throws RemoteException {
    return exists(new IdSelector(id));
  }

  @Override
  public boolean hasElementWithName(String name) throws RemoteException {
    return exists(new NameSelector(name));
  }

  @Override
  public IRemoteHTMLButton button(String id) throws RemoteException {
    IdSelector selector = new IdSelector(id);
    button.setSelector(selector);
    ensureExistence(selector);
    return button;
  }

  @Override
  public IRemoteHTMLButton contactListItem(String jid) throws RemoteException {
    Selector selector = new Selector("li[data-jid=\"" + jid + "\"]");
    button.setSelector(selector);
    ensureExistence(selector);
    return button;
  }

  @Override
  public IRemoteHTMLInputField inputField(String name) throws RemoteException {
    NameSelector selector = new NameSelector(name);
    inputField.setSelector(selector);
    ensureExistence(selector);
    return inputField;
  }

  @Override
  public IRemoteHTMLCheckbox checkbox(String name) throws RemoteException {
    NameSelector selector = new NameSelector(name);
    checkbox.setSelector(selector);
    ensureExistence(selector);
    return checkbox;
  }

  @Override
  public IRemoteHTMLRadioGroup radioGroup(String name) throws RemoteException {
    NameSelector selector = new NameSelector(name);
    radioGroup.setSelector(selector);
    ensureExistence(selector);
    return radioGroup;
  }

  @Override
  public IRemoteHTMLSelect select(String name) throws RemoteException {
    NameSelector selector = new NameSelector(name);
    select.setSelector(selector);
    ensureExistence(selector);
    return select;
  }

  @Override
  public IRemoteHTMLMultiSelect multiSelect(String name) throws RemoteException {
    NameSelector selector = new NameSelector(name);
    multiSelect.setSelector(selector);
    ensureExistence(selector);
    return multiSelect;
  }

  @Override
  public IRemoteHTMLProgressBar progressBar(String name) throws RemoteException {
    NameSelector selector = new NameSelector(name);
    progressBar.setSelector(selector);
    ensureExistence(selector);
    return progressBar;
  }

  @Override
  public IRemoteHTMLTextElement textElement(String id) throws RemoteException {
    IdSelector selector = new IdSelector(id);
    textElement.setSelector(selector);
    ensureExistence(selector);
    return textElement;
  }

  @Override
  public IRemoteHTMLTree tree(String className) throws RemoteException {
    Selector selector = new Selector("ul." + className);
    tree.setSelector(selector);
    ensureExistence(selector);
    return tree;
  }

  @Override
  public boolean isOpen() {
    return exists(new IdSelector(view.getRootId()));
  }

  @Override
  public void open() {
    getBrowser().run(String.format("SarosApi.viewStore.doChangeView('%s')", view.getViewName()));
  }

  public void selectView(View view) {
    this.view = view;
    this.button.setBrowser(getBrowser());
    this.inputField.setBrowser(getBrowser());
    this.checkbox.setBrowser(getBrowser());
    this.radioGroup.setBrowser(getBrowser());
    this.select.setBrowser(getBrowser());
    this.multiSelect.setBrowser(getBrowser());
    this.progressBar.setBrowser(getBrowser());
    this.textElement.setBrowser(getBrowser());
    this.tree.setBrowser(getBrowser());
  }

  private IJQueryBrowser getBrowser() {
    return getBrowserManager().getBrowser(view.getPageClass());
  }

  private void ensureExistence(ISelector selector) throws RemoteException {
    if (!exists(selector))
      throw new RemoteException("did not find HTML element " + selector.getStatement());
  }

  private boolean exists(ISelector selector) {
    boolean foundIt = false;
    try {
      foundIt =
          getBrowser()
              .containsElement(selector)
              .get(BotPreferences.SHORT_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      log.error("could not determine whether element exists", e);
    } catch (TimeoutException e) {
      log.error("could not determine whether element exists", e);
    }
    return foundIt;
  }
}
