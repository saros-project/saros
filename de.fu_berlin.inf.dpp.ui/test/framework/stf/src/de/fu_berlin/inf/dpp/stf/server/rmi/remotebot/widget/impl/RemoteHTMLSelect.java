package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.NameSelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLSelect;

public final class RemoteHTMLSelect extends HTMLSTFRemoteObject implements
    IRemoteHTMLSelect {

    private static final RemoteHTMLSelect INSTANCE = new RemoteHTMLSelect();
    private static final Logger log = Logger.getLogger(RemoteHTMLSelect.class);

    private IJQueryBrowser browser;
    private ISelector selector;

    public static RemoteHTMLSelect getInstance() {
        return INSTANCE;
    }

    @Override
    public String getSelection() throws RemoteException {
        String name = getSelectorName();
        Object selection = browser.syncRun(String.format(
            "return view.getFieldValue('%s')", name));
        return selection != null ? selection.toString() : null;
    }

    @Override
    public void select(String value) throws RemoteException {
        String name = getSelectorName();
        browser.syncRun(String.format("view.setFieldValue('%s', '%s')", name,
            value));

    }

    @Override
    public int size() throws RemoteException {
        return options().size();
    }

    @Override
    public List<String> options() throws RemoteException {
        Object[] objects = (Object[]) browser.syncRun("var options = []; "
            + "$('" + selector + " option').each(function (i) { "
            + "options[i] = $(this).val(); }); " + "return options; ");

        List<String> strings = new ArrayList<String>();
        for (Object o : objects) {
            strings.add(o.toString());
        }

        return strings;
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
