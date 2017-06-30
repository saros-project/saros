package de.fu_berlin.inf.dpp.stf.server.rmi.htmlbot;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotDialog;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteBotDialog;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl.RemoteHTMLView;
import de.fu_berlin.inf.dpp.ui.pages.IBrowserPage;
import de.fu_berlin.inf.dpp.ui.pages.MainPage;

public class HTMLBotImpl extends HTMLSTFRemoteObject implements IHTMLBot {

    private static final IHTMLBot INSTANCE = new HTMLBotImpl();

    private RemoteHTMLView view;

    private RemoteBotDialog dialog;

    public HTMLBotImpl() {
        dialog = RemoteBotDialog.getInstance();
        view = RemoteHTMLView.getInstance();
    }

    @Override
    public IRemoteHTMLView view(View view) {
        this.view.selectView(view);
        return this.view;
    }

    @Override
    public IRemoteBotDialog getDialogWindow(
        Class<? extends IBrowserPage> pageClass) throws RemoteException {
        dialog.setBrowserPage(pageClass);
        return dialog;
    }

    @Override
    public List<String> getAccountList() throws RemoteException {
        Object[] objects = (Object[]) getBrowser().syncRun(
            "var names = []; " + "$('.accountEntry').each(function (i) { "
                + "names[i] = $(this).text().trim(); }); " + "return names; ");

        List<String> strings = new ArrayList<String>();
        for (Object o : objects) {
            strings.add(o.toString());
        }

        return strings;
    }

    private IJQueryBrowser getBrowser() {
        return getBrowserManager().getBrowser(MainPage.class);
    }

    public static IHTMLBot getInstance() {
        return INSTANCE;
    }
}
