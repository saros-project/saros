package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.ag_se.browser.html.ISelector.Selector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot.widget.IRemoteHTMLTree;

public final class RemoteHTMLTree extends HTMLSTFRemoteObject implements
    IRemoteHTMLTree {

    private static final RemoteHTMLTree INSTANCE = new RemoteHTMLTree();

    public static RemoteHTMLTree getInstance() {
        return INSTANCE;
    }

    @Override
    public void check(String title) throws RemoteException {
        // if (isChecked(title) == false) {
        Selector nodeSelector = new Selector("span[title=\"" + title + "\"]");
        browser.run(nodeSelector.getStatement() + ".click();");
        // }
    }

    @Override
    public void uncheck(String title) throws RemoteException {
        if (isChecked(title)) {
            Selector nodeSelector = new Selector("span[title=\"" + title
                + "\"]");
            browser.run(nodeSelector.getStatement() + "[0].click();");
        }
    }

    @Override
    public boolean isChecked(String title) throws RemoteException {
        Selector nodeSelector = new Selector("span[title=\"" + title + "\"]");
        Object checked = browser.syncRun("return "
            + nodeSelector.getStatement()
            + ".prev().hasClass('rc-tree-checkbox-checked')");
        return checked != null ? (Boolean) checked : null;
    }
}
