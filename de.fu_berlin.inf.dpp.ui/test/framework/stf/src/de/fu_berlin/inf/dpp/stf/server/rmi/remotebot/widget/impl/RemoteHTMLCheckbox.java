package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.NameSelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLCheckbox;

public final class RemoteHTMLCheckbox extends HTMLSTFRemoteObject implements
    IRemoteHTMLCheckbox {

    private static final RemoteHTMLCheckbox INSTANCE = new RemoteHTMLCheckbox();
    private static final Logger log = Logger
        .getLogger(RemoteHTMLCheckbox.class);

    private IJQueryBrowser browser;
    private ISelector selector;

    public static RemoteHTMLCheckbox getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isChecked() throws RemoteException {
        String name = getSelectorName();
        Object checked = browser.syncRun(String.format(
            "return view.getFieldValue('%s')", name));
        return checked != null ? (Boolean) checked : null;
    }

    @Override
    public void check() throws RemoteException {
        String name = getSelectorName();
        browser.run(String.format("view.setFieldValue('%s', true)", name));
    }

    @Override
    public void uncheck() throws RemoteException {
        String name = getSelectorName();
        browser.run(String.format("view.setFieldValue('%s', false)", name));

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
