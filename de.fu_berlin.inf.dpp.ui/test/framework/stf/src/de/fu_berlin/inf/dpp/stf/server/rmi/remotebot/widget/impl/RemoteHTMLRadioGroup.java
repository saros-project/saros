package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.NameSelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.BotUtils;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLRadioGroup;

public final class RemoteHTMLRadioGroup extends HTMLSTFRemoteObject implements
    IRemoteHTMLRadioGroup {

    private static final RemoteHTMLRadioGroup INSTANCE = new RemoteHTMLRadioGroup();
    private static final Logger log = Logger
        .getLogger(RemoteHTMLRadioGroup.class);

    private IJQueryBrowser browser;
    private ISelector selector;

    public static RemoteHTMLRadioGroup getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSelected() throws RemoteException {
        String name = getSelectorName();
        Object value = browser.syncRun(String.format(
            "return view.getFieldValue('%s')", name));
        return value != null ? value.toString() : null;
    }

    @Override
    public void select(String value) throws RemoteException {
        String name = getSelectorName();
        browser.syncRun(String.format("view.setFieldValue('%s', '%s')", name,
            value));
    }

    @Override
    public int size() throws RemoteException {
        return values().size();
    }

    @Override
    public List<String> values() throws RemoteException {
        return BotUtils.getListItemsValue(browser, selector);
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
