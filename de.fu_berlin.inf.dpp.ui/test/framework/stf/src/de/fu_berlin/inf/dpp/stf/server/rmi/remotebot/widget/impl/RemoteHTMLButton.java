package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLButton;

public final class RemoteHTMLButton extends HTMLSTFRemoteObject implements
    IRemoteHTMLButton {

    private static final RemoteHTMLButton INSTANCE = new RemoteHTMLButton();

    private IJQueryBrowser browser;
    private ISelector selector;

    public static RemoteHTMLButton getInstance() {
        return INSTANCE;
    }

    @Override
    public void click() throws RemoteException {
        browser.run(selector.getStatement() + ".click();");
    }

    void setBrowser(IJQueryBrowser browser) {
        this.browser = browser;
    }

    void setSelector(ISelector selector) {
        this.selector = selector;
    }
}
