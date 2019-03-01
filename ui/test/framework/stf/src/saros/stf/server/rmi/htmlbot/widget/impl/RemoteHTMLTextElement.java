package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.IRemoteHTMLTextElement;
import java.rmi.RemoteException;

public final class RemoteHTMLTextElement extends HTMLSTFRemoteObject
    implements IRemoteHTMLTextElement {

  private static final RemoteHTMLTextElement INSTANCE = new RemoteHTMLTextElement();

  public static RemoteHTMLTextElement getInstance() {
    return INSTANCE;
  }

  @Override
  public String getText() throws RemoteException {

    Object text = browser.syncRun(String.format("return %s.text()", selector.getStatement()));
    return text != null ? text.toString() : null;
  }

  @Override
  public void setText(String text) throws RemoteException {
    browser.run(String.format("%s.text('%s')", selector.getStatement(), text));
  }
}
