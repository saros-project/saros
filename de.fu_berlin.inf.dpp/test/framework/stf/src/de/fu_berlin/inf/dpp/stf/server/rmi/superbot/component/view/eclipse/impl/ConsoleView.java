package de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.impl;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.server.rmi.superbot.component.view.eclipse.IConsoleView;

public final class ConsoleView extends StfRemoteObject implements IConsoleView {

    private static final Logger log = Logger.getLogger(ConsoleView.class);
    private static final ConsoleView INSTANCE = new ConsoleView();

    private SWTBotView view;

    public static ConsoleView getInstance() {
        return INSTANCE;
    }

    public IConsoleView setView(SWTBotView view) {
        this.view = view;
        return this;
    }

    public String getFirstTextInConsole() throws RemoteException {
        return view.bot().styledText().getText();
    }

    public boolean existTextInConsole() throws RemoteException {
        try {
            if (view.bot().styledText().getText().equals(""))
                return false;
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

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
