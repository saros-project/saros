package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLTextElement;

public final class RemoteHTMLTextElement extends HTMLSTFRemoteObject implements
    IRemoteHTMLTextElement {

    private static final RemoteHTMLTextElement INSTANCE = new RemoteHTMLTextElement();

    private IJQueryBrowser browser;
    private ISelector selector;

    public static RemoteHTMLTextElement getInstance() {
        return INSTANCE;
    }

    @Override
    public String getText() throws RemoteException {

        Object text = browser.syncRun(String.format("return %s.text()",
            selector.getStatement()));
        return text != null ? text.toString() : null;
    }

    @Override
    public void setText(String text) throws RemoteException {
        browser.run(String.format("%s.text('%s')", selector.getStatement(),
            text));
    }

    void setBrowser(IJQueryBrowser browser) {
        this.browser = browser;
    }

    void setSelector(ISelector selector) {
        this.selector = selector;
    }

}
