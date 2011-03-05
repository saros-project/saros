package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.views;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotView;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.superFinder.remoteComponents.Component;

public class ConsoleViewImp extends Component implements ConsoleView {

    private static transient ConsoleViewImp consoleViewObject;
    private STFBotView view;

    /**
     * {@link ConsoleViewImp} is a singleton, but inheritance is possible.
     */
    public static ConsoleViewImp getInstance() {
        if (consoleViewObject != null)
            return consoleViewObject;
        consoleViewObject = new ConsoleViewImp();
        return consoleViewObject;
    }

    public ConsoleView setView(STFBotView view) {
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
        bot().waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return existTextInConsole();
            }

            public String getFailureMessage() {
                return "in the console view contains no text.";
            }
        });
    }
}
