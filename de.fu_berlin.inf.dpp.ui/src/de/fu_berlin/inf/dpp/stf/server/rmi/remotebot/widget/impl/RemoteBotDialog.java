package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotDialog;
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage;

public class RemoteBotDialog extends HTMLSTFRemoteObject implements
    IRemoteBotDialog {

    private final static RemoteBotDialog INSTANCE = new RemoteBotDialog();

    private Class<? extends IBrowserPage> browserPageClass;

    public RemoteBotDialog() {

    }

    public static RemoteBotDialog getInstance() {
        return INSTANCE;
    }

    @Override
    public void fillInputField(String id, String value) throws RemoteException {
        getBrowser().syncRun("$('#" + id + "').val('" + value + "');");
    }

    @Override
    public void submit() throws RemoteException {
        getBrowser().syncRun("$('#finishButton').click();");
    }

    @Override
    public void cancel() throws RemoteException {
        getBrowser().syncRun("$('#cancelButton').click();");
    }

    private IJQueryBrowser getBrowser() {
        return getBrowserManager().getBrowser(browserPageClass);
    }

    public void setBrowserPage(Class<? extends IBrowserPage> browserPageClass) {
        this.browserPageClass = browserPageClass;
    }
}
