package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class ToolbarButtonImp extends EclipsePart implements ToolbarButton {

    private static transient ToolbarButtonImp ToolbarButtonImp;

    /**
     * {@link ToolbarButtonImp} is a singleton, but inheritance is possible.
     */
    public static ToolbarButtonImp getInstance() {
        if (ToolbarButtonImp != null)
            return ToolbarButtonImp;
        ToolbarButtonImp = new ToolbarButtonImp();
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

    public void clickToolbarButtonWithTooltipInView(String viewTitle,
        String tooltipText) throws RemoteException {
        bot.viewByTitle(viewTitle).toolbarButton(tooltipText).click();
    }

    public void clickToolbarButtonWithRegexTooltipInView(String viewTitle,
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

    public void clickToolbarPushButtonWithTooltipInView(String viewTitle,
        String tooltip) throws RemoteException {
        bot.viewByTitle(viewTitle).toolbarPushButton(tooltip).click();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean existsToolbarButtonInview(String viewTitle,
        String tooltipText) throws RemoteException {
        for (SWTBotToolbarButton toolbarButton : getAllToolbarButtonsInView(viewTitle)) {
            if (toolbarButton.getToolTipText().equals(tooltipText))
                return true;
        }
        return false;
    }

    public boolean isToolbarButtonInViewEnabled(String viewTitle,
        String tooltipText) throws RemoteException {
        SWTBotToolbarButton button = getToolbarButtonWithRegexTooltipInView(
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
    public List<SWTBotToolbarButton> getAllToolbarButtonsInView(String viewTitle) {
        return bot.viewByTitle(viewTitle).getToolbarButtons();
    }

    public SWTBotToolbarButton getToolbarButtonWithRegexTooltipInView(
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
