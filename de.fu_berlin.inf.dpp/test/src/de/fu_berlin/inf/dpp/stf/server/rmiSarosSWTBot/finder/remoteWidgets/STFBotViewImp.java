package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarRadioButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFBot;

public class STFBotViewImp extends AbstractRmoteWidget implements STFBotView {

    private static transient STFBotViewImp self;

    private SWTBotView swtbotView;

    private static STFBotViewMenuImp stfViewMenu;
    private static STFBotToolbarDropDownButtonImp stfToolbarDropDownButton;
    private static STFBotToolbarPushButtonImp stfToolbarPushButton;
    private static STFBotToolbarRadioButtonImp stfToolbarRadioButton;
    private static STFBotToolbarToggleButtonImp stfToolbarToggleButton;
    public static STFBotToolbarButtonImp stfToolbarButton;

    /**
     * {@link STFBotTableImp} is a singleton, but inheritance is possible.
     */
    public static STFBotViewImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotViewImp();
        stfViewMenu = STFBotViewMenuImp.getInstance();
        stfToolbarDropDownButton = STFBotToolbarDropDownButtonImp.getInstance();
        stfToolbarPushButton = STFBotToolbarPushButtonImp.getInstance();
        stfToolbarRadioButton = STFBotToolbarRadioButtonImp.getInstance();
        stfToolbarToggleButton = STFBotToolbarToggleButtonImp.getInstance();
        stfToolbarButton = STFBotToolbarButtonImp.getInstance();
        return self;
    }

    public void setWidget(SWTBotView view) {

        swtbotView = view;

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
        bot.setBot(swtbotView.bot());
        return bot;
    }

    public STFBotViewMenu menu(String label) throws RemoteException {
        stfViewMenu.setSwtBotViewMenu(swtbotView.menu(label));
        return stfViewMenu;
    }

    public STFBotViewMenu menu(String label, int index) throws RemoteException {
        stfViewMenu.setSwtBotViewMenu(swtbotView.menu(label, index));
        return stfViewMenu;
    }

    public STFBotToolbarButton toolbarButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarButton toolbarButton = swtbotView.toolbarButton(tooltip);
        stfToolbarButton.setSwtBotToolbarButton(toolbarButton);

        return stfToolbarButton;
    }

    public STFBotToolbarButton toolbarButtonWithRegex(String regex)
        throws RemoteException {
        for (String tooltip : getToolTipTextOfToolbarButtons()) {
            System.out.println(tooltip + ":");
            if (tooltip.matches(regex)) {
                SWTBotToolbarButton toolbarButton = swtbotView
                    .toolbarButton(tooltip);
                stfToolbarButton.setSwtBotToolbarButton(toolbarButton);
                return stfToolbarButton;
            }
        }
        return null;
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
        swtbotView.show();
        // swtbotView.setFocus();
        // waitUntilIsActive();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/
    public List<String> getToolTipOfAllToolbarbuttons() throws RemoteException {
        List<String> tooltips = new ArrayList<String>();
        for (SWTBotToolbarButton button : swtbotView.getToolbarButtons()) {
            tooltips.add(button.getToolTipText());
        }
        return tooltips;
    }

    public boolean existsToolbarButton(String tooltip) throws RemoteException {
        return getToolTipOfAllToolbarbuttons().contains(tooltip);
    }

    public boolean isActive() throws RemoteException {
        return swtbotView.isActive();
    }

    public String getTitle() throws RemoteException {
        return swtbotView.getTitle();
    }

    public List<String> getToolTipTextOfToolbarButtons() throws RemoteException {
        List<String> toolbarButtons = new ArrayList<String>();
        for (SWTBotToolbarButton toolbarButton : swtbotView.getToolbarButtons()) {
            toolbarButtons.add(toolbarButton.getToolTipText());
        }
        return toolbarButtons;
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/
    public void waitUntilIsActive() throws RemoteException {

        bot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isActive();
            }

            public String getFailureMessage() {
                return "Can't activate this view.";
            }
        });
    }

}
