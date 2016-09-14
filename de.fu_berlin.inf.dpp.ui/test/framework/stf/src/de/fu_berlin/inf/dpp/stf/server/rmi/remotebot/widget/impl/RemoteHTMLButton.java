package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLButton;
import de.fu_berlin.inf.dpp.ui.pages.MainPage;

public final class RemoteHTMLButton extends HTMLSTFRemoteObject implements
    IRemoteHTMLButton {

    private static final RemoteHTMLButton INSTANCE = new RemoteHTMLButton();

    private ISelector selector;

    public static RemoteHTMLButton getInstance() {
        return INSTANCE;
    }

    public IRemoteHTMLButton setSelector(ISelector selector) {
        this.selector = selector;
        return this;
    }

    @Override
    public void click() throws RemoteException {
        getBrowserManager().getBrowser(MainPage.class).run(
            selector.getStatement() + ".click();");
    }
}
