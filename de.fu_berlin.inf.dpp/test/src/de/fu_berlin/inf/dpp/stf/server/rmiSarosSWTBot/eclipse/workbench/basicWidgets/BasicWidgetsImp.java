package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class BasicWidgetsImp extends EclipsePart implements BasicWidgets {

    private static transient BasicWidgetsImp eclipseBasicObjectImp;

    /**
     * {@link BasicWidgetsImp} is a singleton, but inheritance is possible.
     */
    public static BasicWidgetsImp getInstance() {
        if (eclipseBasicObjectImp != null)
            return eclipseBasicObjectImp;
        eclipseBasicObjectImp = new BasicWidgetsImp();
        return eclipseBasicObjectImp;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotText}.
     * 
     **********************************************/

    // actions
    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException {
        bot.textWithLabel(label).setText(text);
    }

    // states
    public String getTextInTextWithLabel(String label) throws RemoteException {
        return bot.textWithLabel(label).getText();
    }

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotLabel}.
     * 
     **********************************************/
    // states
    public String getTextOfLabel() throws RemoteException {
        return bot.label().getText();
    }

    public boolean existsLabel(String mnemonicText) throws RemoteException {
        try {
            bot.label(mnemonicText);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean existsLabelInView(String viewTitle) throws RemoteException {
        try {
            viewW.getView(viewTitle).bot().label();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotView}.
     * 
     **********************************************/

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotMenu}.
     * 
     **********************************************/

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

}
