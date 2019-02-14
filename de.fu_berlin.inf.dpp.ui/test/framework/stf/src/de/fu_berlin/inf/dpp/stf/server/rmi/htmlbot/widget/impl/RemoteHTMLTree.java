package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl;

import de.fu_berlin.inf.ag_se.browser.html.ISelector.Selector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.IRemoteHTMLTree;
import java.rmi.RemoteException;

public final class RemoteHTMLTree extends HTMLSTFRemoteObject implements IRemoteHTMLTree {

  private static final RemoteHTMLTree INSTANCE = new RemoteHTMLTree();

  public static RemoteHTMLTree getInstance() {
    return INSTANCE;
  }

  @Override
  public void check(String title) throws RemoteException {
    if (isChecked(title) == false) {
      Selector nodeSelector = new Selector("span[title=\"" + title + "\"]");
      browser.run(String.format("%s[0].click();", nodeSelector.getStatement()));
    }
  }

  @Override
  public void uncheck(String title) throws RemoteException {
    if (isChecked(title)) {
      Selector nodeSelector = new Selector("span[title=\"" + title + "\"]");
      browser.run(String.format("%s[0].click();", nodeSelector.getStatement()));
    }
  }

  @Override
  public boolean isChecked(String title) throws RemoteException {
    Selector nodeSelector = new Selector("span[title=\"" + title + "\"]");
    Object checked =
        browser.syncRun(
            String.format(
                "return %s.prev().hasClass('rc-tree-checkbox-checked')",
                nodeSelector.getStatement()));
    return checked != null ? (Boolean) checked : null;
  }
}
