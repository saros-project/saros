package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotViewMenu;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarRadioButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBot;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBotImp;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotViewImp extends EclipseComponentImp implements STFBotView {

    private static transient STFBotViewImp self;
    private String viewTitle;
    private String viewId;

    private SWTBotView swtbotView;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotViewImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotViewImp();
        return self;
    }

    public void setViewTitle(String title) throws RemoteException {
        if (this.viewTitle == null || !viewTitle.equals(title)) {
            this.viewTitle = title;
            if (viewTitlesAndIDs.containsKey(viewTitle))
                this.viewId = viewTitlesAndIDs.get(viewTitle);
            swtbotView = bot.viewByTitle(title);
        }

    }

    public void setId(String id) {
        if (this.viewId == null || !this.viewId.equals(id)) {
            this.viewId = id;
            if (viewTitlesAndIDs.containsValue(viewId)) {
                String[] titles = (String[]) viewTitlesAndIDs.keySet()
                    .toArray();
                for (int i = 0; i < viewTitlesAndIDs.values().size(); i++) {
                    if (viewTitlesAndIDs.values().toArray()[i].equals(id))
                        this.viewTitle = titles[i];
                }
                swtbotView = bot.viewById(id);
            }
        }
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/

    /**********************************************
     * 
     * Finders
     * 
     **********************************************/
    public STFBot bot_() {
        STFBotImp botImp = STFBotImp.getInstance();
        botImp.setBot(bot.viewByTitle(viewTitle).bot());
        return botImp;
    }

    public STFBotViewMenu menu(String label) throws RemoteException {
        SWTBotViewMenu viewMenu = swtbotView.menu(label);
        stfViewMenu.setSwtBotViewMenu(viewMenu);
        return stfViewMenu;
    }

    public STFBotViewMenu menu(String label, int index) throws RemoteException {
        SWTBotViewMenu viewMenu = swtbotView.menu(label, index);
        stfViewMenu.setSwtBotViewMenu(viewMenu);
        return stfViewMenu;
    }

    public STFBotToolbarButton toolbarButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarButton toolbarButton = swtbotView.toolbarButton(tooltip);
        stfToolbarButton.setSwtBotToolbarButton(toolbarButton);
        return stfToolbarButton;
    }

    public STFBotToolbarDropDownButton toolbarDropDownButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarDropDownButton toolbarButton = swtbotView
            .toolbarDropDownButton(tooltip);
        stfToolbarDropDownButton.setSwtBotToolbarDropDownButton(toolbarButton);
        return stfToolbarDropDownButton;
    }

    public STFBotToolbarRadioButton toolbarRadioButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarRadioButton toolbarButton = swtbotView
            .toolbarRadioButton(tooltip);
        stfToolbarRadioButton.setSwtBotToolbarRadioButton(toolbarButton);
        return stfToolbarRadioButton;
    }

    public STFBotToolbarPushButton toolbarPushButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarPushButton toolbarButton = swtbotView
            .toolbarPushButton(tooltip);
        stfToolbarPushButton.setSwtBotToolbarPushButton(toolbarButton);
        return stfToolbarPushButton;
    }

    public STFBotToolbarToggleButton toolbarToggleButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarToggleButton toolbarButton = swtbotView
            .toolbarToggleButton(tooltip);
        stfToolbarToggleButton.setSwtBotToolbarToggleButton(toolbarButton);
        return stfToolbarToggleButton;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void close() throws RemoteException {
        swtbotView.close();
    }

    public void setFocus() throws RemoteException {
        swtbotView.setFocus();
        waitUntilIsActive();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public boolean isActive() throws RemoteException {
        return swtbotView.isActive();
    }

    public String getTitle() throws RemoteException {
        return swtbotView.getTitle();
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsActive() throws RemoteException {

        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isActive();
            }

            public String getFailureMessage() {
                return "Can't activate this view.";
            }
        });
    }

    /**************************************************************
     * 
     * inner functions
     * 
     **************************************************************/

}
