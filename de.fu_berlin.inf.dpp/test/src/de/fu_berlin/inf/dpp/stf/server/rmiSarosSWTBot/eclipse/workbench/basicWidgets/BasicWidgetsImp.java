package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
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
     * actions on the basic widget: {@link SWTBotButton}.
     * 
     **********************************************/

    // actions
    public void clickButton(String mnemonicText) throws RemoteException {
        bot.button(mnemonicText).click();
    }

    public void clickButtonWithTooltip(String tooltip) throws RemoteException {
        bot.buttonWithTooltip(tooltip).click();
    }

    // states
    public boolean isButtonEnabled(String mnemonicText) throws RemoteException {
        return bot.button(mnemonicText).isEnabled();
    }

    public boolean isButtonWithTooltipEnabled(String tooltip)
        throws RemoteException {
        return bot.buttonWithTooltip(tooltip).isEnabled();
    }

    public boolean existsButtonInGroup(String mnemonicText, String inGroup)
        throws RemoteException {
        try {
            bot.buttonInGroup(mnemonicText, inGroup);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    // waits until
    public void waitUntilButtonEnabled(String mnemonicText)
        throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(bot.button(mnemonicText)));
    }

    public void waitUnitButtonWithTooltipIsEnabled(String tooltip)
        throws RemoteException {
        waitUntil(Conditions.widgetIsEnabled(bot.buttonWithTooltip(tooltip)));
    }

    /**********************************************
     * 
     * actions on the basic widget: {@link SWTBotToolbarButton}.
     * 
     **********************************************/
    // actions
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

    // states
    public boolean isToolbarButtonInViewEnabled(String viewTitle,
        String tooltipText) throws RemoteException {
        SWTBotToolbarButton button = getToolbarButtonWithTooltipInView(
            viewTitle, tooltipText);
        if (button == null)
            return false;
        return button.isEnabled();
    }

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
            getView(viewTitle).bot().label();
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
    // actions
    public void openViewById(final String viewId) throws RemoteException {
        try {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    final IWorkbench wb = PlatformUI.getWorkbench();
                    final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

                    IWorkbenchPage page = win.getActivePage();
                    try {
                        IViewReference[] registeredViews = page
                            .getViewReferences();
                        for (IViewReference registeredView : registeredViews) {
                            log.debug("registered view ID: "
                                + registeredView.getId());
                        }

                        page.showView(viewId);
                    } catch (PartInitException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            log.debug("Couldn't initialize " + viewId, e.getCause());
        }
    }

    public void closeViewByTitle(String title) throws RemoteException {
        if (isViewOpen(title)) {
            bot.viewByTitle(title).close();
        }
    }

    public void closeViewById(final String viewId) throws RemoteException {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();
                final IViewPart view = page.findView(viewId);
                if (view != null) {
                    page.hideView(view);
                }
            }
        });
    }

    public void setFocusOnViewByTitle(String title) throws RemoteException {
        try {
            bot.viewByTitle(title).setFocus();
        } catch (WidgetNotFoundException e) {
            log.warn("view not found '" + title + "'", e);
        }
    }

    // states
    public boolean isViewOpen(String title) throws RemoteException {
        return getTitlesOfOpenedViews().contains(title);
    }

    public boolean isViewActive(String title) throws RemoteException {
        if (!isViewOpen(title))
            return false;
        try {
            return bot.activeView().getTitle().equals(title);
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public List<String> getTitlesOfOpenedViews() throws RemoteException {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotView view : bot.views())
            list.add(view.getTitle());
        return list;
    }

    // waits until
    public void waitUntilViewActive(String viewName) throws RemoteException {
        waitUntil(SarosConditions.isViewActive(bot, viewName));
    }

    /**********************************************
     * 
     * actions on the widget: {@link SWTBotMenu}.
     * 
     **********************************************/
    public void clickMenuWithTexts(String... texts) throws RemoteException {
        workbenchC.activateEclipseShell();
        SWTBotMenu selectedmenu = null;
        for (String text : texts) {
            try {
                if (selectedmenu == null) {
                    selectedmenu = bot.menu(text);
                } else {
                    selectedmenu = selectedmenu.menu(text);
                }
            } catch (WidgetNotFoundException e) {
                log.error("menu \"" + text + "\" not found!");
                throw e;
            }
        }
        if (selectedmenu != null)
            selectedmenu.click();
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
        return getView(viewTitle).getToolbarButtons();
    }

    public SWTBotToolbarButton getToolbarButtonWithTooltipInView(
        String viewTitle, String tooltipText) {
        for (SWTBotToolbarButton toolbarButton : getView(viewTitle)
            .getToolbarButtons()) {
            if (toolbarButton.getToolTipText().matches(
                ".*" + tooltipText + ".*")) {
                return toolbarButton;
            }
        }
        return null;
    }

    /**
     * @param viewTitle
     *            the title on the view tab.
     * @return the {@link SWTBotView} specified with the given title.
     */
    public SWTBotView getView(String viewTitle) {
        return bot.viewByTitle(viewTitle);
    }

}
