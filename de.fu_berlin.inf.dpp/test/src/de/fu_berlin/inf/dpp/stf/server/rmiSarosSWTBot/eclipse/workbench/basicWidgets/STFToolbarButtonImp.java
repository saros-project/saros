package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class STFToolbarButtonImp extends EclipseComponentImp implements
    STFToolbarButton {

    private static transient STFToolbarButtonImp ToolbarButtonImp;

    /**
     * {@link STFToolbarButtonImp} is a singleton, but inheritance is possible.
     */
    public static STFToolbarButtonImp getInstance() {
        if (ToolbarButtonImp != null)
            return ToolbarButtonImp;
        ToolbarButtonImp = new STFToolbarButtonImp();
        return ToolbarButtonImp;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void clickToolbarButtonInView(String viewTitle)
        throws RemoteException {
        bot.viewByTitle(viewTitle).bot().toolbarButton().click();
    }

    public void clickToolbarButtonWithIndexInView(String viewTitle, int index)
        throws RemoteException {
        bot.viewByTitle(viewTitle).bot().toolbarButton(index).click();
    }

    public void clickToolbarButtonWithTooltipOnView(String viewTitle,
        String tooltipText) throws RemoteException {
        bot.viewByTitle(viewTitle).toolbarButton(tooltipText).click();
    }

    public void clickToolbarButtonWithRegexTooltipOnView(String viewTitle,
        String tooltipText) throws RemoteException {
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(viewTitle)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(
                ".*" + tooltipText + ".*")) {
                toolbarButton.click();
                return;
            }
        }
        throw new WidgetNotFoundException(
            "The toolbarbutton with the tooltipText "
                + tooltipText
                + " doesn't exist. Are you sure that the passed tooltip text is correct?");
    }

    public void clickToolbarPushButtonWithTooltipOnView(String viewTitle,
        String tooltip) throws RemoteException {
        bot.viewByTitle(viewTitle).toolbarPushButton(tooltip).click();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public boolean existstoolbarButonInView(String viewTitle)
        throws RemoteException {
        try {
            bot.viewByTitle(viewTitle).bot().toolbarButton();
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean existsToolbarButtonOnview(String viewTitle,
        String tooltipText) throws RemoteException {
        for (SWTBotToolbarButton toolbarButton : getAllToolbarButtonsOnView(viewTitle)) {
            if (toolbarButton.getToolTipText().equals(tooltipText))
                return true;
        }
        return false;
    }

    public boolean isToolbarButtonOnViewEnabled(String viewTitle,
        String tooltipText) throws RemoteException {
        SWTBotToolbarButton button = getToolbarButtonWithRegexTooltipOnView(
            viewTitle, tooltipText);
        if (button == null)
            return false;
        return button.isEnabled();
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

    /**
     * @param viewTitle
     *            the title on the view tab.
     * @return all {@link SWTBotToolbarButton} located in the given view.
     */
    public List<SWTBotToolbarButton> getAllToolbarButtonsOnView(String viewTitle) {
        return bot.viewByTitle(viewTitle).getToolbarButtons();
    }

    public SWTBotToolbarButton getToolbarButtonWithRegexTooltipOnView(
        String viewTitle, String tooltipText) {
        for (SWTBotToolbarButton toolbarButton : bot.viewByTitle(viewTitle)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(
                ".*" + tooltipText + ".*")) {
                return toolbarButton;
            }
        }
        return null;
    }
}
