package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views.eclipseViews;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets.IRemoteBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class ConsoleView extends Component implements IConsoleView {

    private static transient ConsoleView consoleViewObject;
    private IRemoteBotView view;

    /**
     * {@link ConsoleView} is a singleton, but inheritance is possible.
     */
    public static ConsoleView getInstance() {
        if (consoleViewObject != null)
            return consoleViewObject;
        consoleViewObject = new ConsoleView();
        return consoleViewObject;
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
        remoteBot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existTextInConsole();
            }

            public String getFailureMessage() {
                return "in the console view contains no text.";
            }
        });
    }
}
