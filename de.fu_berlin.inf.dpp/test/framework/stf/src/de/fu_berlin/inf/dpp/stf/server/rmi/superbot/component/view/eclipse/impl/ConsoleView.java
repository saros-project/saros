package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IConsoleView;

public final class ConsoleView extends StfRemoteObject implements IConsoleView {

    private static final ConsoleView INSTANCE = new ConsoleView();

    private IRemoteBotView view;

    public static ConsoleView getInstance() {
        return INSTANCE;
    }

    public IConsoleView setView(IRemoteBotView view) {
        this.view = view;
        return this;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public String getFirstTextInConsole() throws RemoteException {
        return view.bot().styledText().getText();
    }

    public boolean existTextInConsole() throws RemoteException {
        if (!view.bot().existsStyledText())
            return false;
        if (view.bot().styledText().getText().equals(""))
            return false;
        return true;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilExistsTextInConsole() throws RemoteException {
        RemoteWorkbenchBot.getInstance().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existTextInConsole();
            }

            public String getFailureMessage() {
                return "the console view contains no text";
            }
        });
    }
}
