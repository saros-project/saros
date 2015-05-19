package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotDialog;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotDialog;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteHTMLButton;
import de.fu_berlin.inf.dpp.ui.webpages.BrowserPage;
import de.fu_berlin.inf.dpp.ui.webpages.SarosMainPage;

public class HTMLBotImpl extends HTMLSTFRemoteObject implements IHTMLBot {

    private static final IHTMLBot INSTANCE = new HTMLBotImpl();

    private RemoteBotDialog dialog;

    private RemoteHTMLButton button;

    public HTMLBotImpl() {
        dialog = RemoteBotDialog.getInstance();
    }

    @Override
    public IRemoteBotDialog getDialogWindow(
        Class<? extends BrowserPage> pageClass) throws RemoteException {
        dialog.setBrowserPage(pageClass);
        return dialog;
    }

    @Override
    public IRemoteHTMLButton buttonWithId(String id) throws RemoteException {
        button.setSelector(new ISelector.IdSelector(id));
        return button;
    }

    @Override
    public List<String> getAccountList() throws RemoteException {
        Object[] objects = (Object[]) getBrowser().syncRun(
            "var names = []; " + "$('.accountEntry').each(function (i) { "
                + "names[i] = $(this).text().trim(); }); " + "return names; ");

        ArrayList<String> strings = new ArrayList<String>();
        for (Object o : objects) {
            strings.add(o.toString());
        }

        return strings;
    }

    private IJQueryBrowser getBrowser() {
        return getBrowserManager().getBrowser(SarosMainPage.class);
    }

    public static IHTMLBot getInstance() {
        return INSTANCE;
    }
}
