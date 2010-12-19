package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class BasicComponentImp extends EclipseComponent implements
    BasicComponent {

    private static transient BasicComponentImp eclipseBasicObjectImp;

    /**
     * {@link BasicComponentImp} is a singleton, but inheritance is possible.
     */
    public static BasicComponentImp getInstance() {
        if (eclipseBasicObjectImp != null)
            return eclipseBasicObjectImp;
        eclipseBasicObjectImp = new BasicComponentImp();
        return eclipseBasicObjectImp;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * basic widget: {@link SWTBotButton}.
     * 
     **********************************************/
    public void clickButton(String mnemonicText) throws RemoteException {
        bot.button(mnemonicText).click();
    }

    public boolean isButtonEnabled(String mnemonicText) throws RemoteException {
        return bot.button(mnemonicText).isEnabled();
    }

    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(bot.button(mnemonicText)));
    }

    public void waitUnitButtonWithTooltipIsEnabled(String tooltipText)
        throws RemoteException {
        waitUntil(Conditions
            .widgetIsEnabled(bot.buttonWithTooltip(tooltipText)));
    }

    /**********************************************
     * 
     * basic widget: {@link SWTBotText}.
     * 
     **********************************************/
    public void setTextInTextWithLabel(String text, String label)
        throws RemoteException {
        bot.textWithLabel(label).setText(text);
    }

    public String getTextInTextWithLabel(String label) throws RemoteException {
        return bot.textWithLabel(label).getText();
    }

    /**********************************************
     * 
     * basic widget: {@link SWTBotLabel}.
     * 
     **********************************************/
    public String geFirsttLabelText() throws RemoteException {
        return bot.label().getText();
    }

    public boolean existsLabel(String label) throws RemoteException {
        try {
            bot.label(label);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }
}
