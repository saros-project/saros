package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.NameSelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLProgressBar;

public final class RemoteHTMLProgressBar extends HTMLSTFRemoteObject implements
    IRemoteHTMLProgressBar {

    private static final RemoteHTMLProgressBar INSTANCE = new RemoteHTMLProgressBar();
    private static final Logger log = Logger
        .getLogger(RemoteHTMLProgressBar.class);

    private IJQueryBrowser browser;
    private ISelector selector;

    public static RemoteHTMLProgressBar getInstance() {
        return INSTANCE;
    }

    @Override
    public int getValue() throws RemoteException {
        String name = getSelectorName();
        Object selection = browser.syncRun(String.format(
            "return view.getFieldValue('%s')", name));
        return selection != null ? ((Double) selection).intValue() : null;
    }

    @Override
    public void setValue(int value) throws RemoteException {
        String name = getSelectorName();
        browser.syncRun(String.format("view.setFieldValue('%s',%d)", name,
            value));

    }

    void setBrowser(IJQueryBrowser browser) {
        this.browser = browser;
    }

    void setSelector(ISelector selector) {
        this.selector = selector;
    }

    private String getSelectorName() {
        return ((NameSelector) selector).getName();
    }

}
