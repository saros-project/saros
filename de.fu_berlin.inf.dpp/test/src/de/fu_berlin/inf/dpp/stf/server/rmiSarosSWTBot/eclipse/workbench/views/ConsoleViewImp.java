package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.views;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class ConsoleViewImp extends EclipsePart implements ConsoleView {

    private static transient ConsoleViewImp consoleViewObject;

    /**
     * {@link ConsoleViewImp} is a singleton, but inheritance is possible.
     */
    public static ConsoleViewImp getInstance() {
        if (consoleViewObject != null)
            return consoleViewObject;
        consoleViewObject = new ConsoleViewImp();
        return consoleViewObject;
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
    public String getTextInConsole() throws RemoteException {
        return viewW.getView("Console").bot().styledText().getText();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitsUntilTextInConsoleExisted() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                try {
                    SWTBotStyledText styledText = viewW.getView("Console")
                        .bot().styledText();
                    if (styledText != null && styledText.getText() != null
                        && !styledText.getText().equals(""))
                        return true;
                    else
                        return false;
                } catch (WidgetNotFoundException e) {
                    return false;
                }
            }

            public String getFailureMessage() {
                return "in the console view contains no text.";
            }
        });
    }
}
