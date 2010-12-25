package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class ConsoleViewComponentImp extends EclipseComponent implements
    ConsoleViewComponent {

    private static transient ConsoleViewComponentImp consoleViewObject;

    /**
     * {@link BasicComponentImp} is a singleton, but inheritance is possible.
     */
    public static ConsoleViewComponentImp getInstance() {
        if (consoleViewObject != null)
            return consoleViewObject;
        consoleViewObject = new ConsoleViewComponentImp();
        return consoleViewObject;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions with basic widget: {@link SWTBotButton}.
     * 
     **********************************************/
    public void waitsUntilTextInConsoleExisted() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                try {
                    SWTBotStyledText styledText = basicC.getView("Console")
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

    public String getTextInConsole() throws RemoteException {
        return basicC.getView("Console").bot().styledText().getText();
    }
}
