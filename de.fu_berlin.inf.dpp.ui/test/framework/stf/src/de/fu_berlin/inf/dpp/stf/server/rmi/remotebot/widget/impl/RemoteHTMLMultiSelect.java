package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.NameSelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLMultiSelect;

public final class RemoteHTMLMultiSelect extends HTMLSTFRemoteObject implements
    IRemoteHTMLMultiSelect {

    private static final RemoteHTMLMultiSelect INSTANCE = new RemoteHTMLMultiSelect();
    private static final Logger log = Logger
        .getLogger(RemoteHTMLMultiSelect.class);

    private IJQueryBrowser browser;
    private ISelector selector;

    public static RemoteHTMLMultiSelect getInstance() {
        return INSTANCE;
    }

    @Override
    public List<String> getSelection() throws RemoteException {
        String name = getSelectorName();
        Object selection = browser.syncRun(String.format(
            "return view.getFieldValue('%s')", name));
        if (selection != null) {
            String raw = selection.toString();
            String[] items = raw.replaceAll("\\[", "").replaceAll("\\]", "")
                .replaceAll("\\s", "").split(",");
            return Arrays.asList(items);
        }

        return null;
    }

    @Override
    public void select(List<String> value) throws RemoteException {
        String name = getSelectorName();

        browser.syncRun(String.format("view.setFieldValue('%s', '%s')", name,
            value.toString()));

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
