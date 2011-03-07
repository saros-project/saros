package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotViewMenu;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarRadioButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.RemoteBot;

public class RemoteBotViewImp extends AbstractRmoteWidget implements RemoteBotView {

    private static transient RemoteBotViewImp self;

    private SWTBotView widget;

    /**
     * {@link RemoteBotViewImp} is a singleton, but inheritance is possible.
     */
    public static RemoteBotViewImp getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotViewImp();
        return self;
    }

    public RemoteBotView setWidget(SWTBotView view) {
        widget = view;
        return this;
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
    public RemoteBot bot() {
        stfBot.setBot(widget.bot());
        return stfBot;
    }

    // menu
    public RemoteBotViewMenu menu(String label) throws RemoteException {
        stfViewMenu.setWidget(widget.menu(label));
        return stfViewMenu;
    }

    public RemoteBotViewMenu menu(String label, int index) throws RemoteException {
        stfViewMenu.setWidget(widget.menu(label, index));
        return stfViewMenu;
    }

    public List<RemoteBotViewMenu> menus() throws RemoteException {
        List<RemoteBotViewMenu> menus = new ArrayList<RemoteBotViewMenu>();
        for (SWTBotViewMenu menu : widget.menus()) {
            menus.add(stfViewMenu.setWidget(menu));
        }
        return menus;
    }

    // toolbarButton
    public RemoteBotToolbarButton toolbarButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarButton toolbarButton = widget.toolbarButton(tooltip);
        stfToolbarButton.setWidget(toolbarButton);

        return stfToolbarButton;
    }

    public RemoteBotToolbarButton toolbarButtonWithRegex(String regex)
        throws RemoteException {
        for (String tooltip : getToolTipTextOfToolbarButtons()) {
            System.out.println(tooltip + ":");
            if (tooltip.matches(regex)) {
                SWTBotToolbarButton toolbarButton = widget
                    .toolbarButton(tooltip);
                stfToolbarButton.setWidget(toolbarButton);
                return stfToolbarButton;
            }
        }
        throw new WidgetNotFoundException("The toolBarButton doesn't exist!");
    }

    public List<RemoteBotToolbarButton> getToolbarButtons() throws RemoteException {
        List<RemoteBotToolbarButton> toolbarButtons = new ArrayList<RemoteBotToolbarButton>();
        for (SWTBotToolbarButton button : widget.getToolbarButtons()) {
            toolbarButtons.add(stfToolbarButton.setWidget(button));
        }
        return toolbarButtons;
    }

    // toolbarDropDownButton
    public RemoteBotToolbarDropDownButton toolbarDropDownButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarDropDownButton toolbarButton = widget
            .toolbarDropDownButton(tooltip);
        stfToolbarDropDownButton.setWidget(toolbarButton);
        return stfToolbarDropDownButton;
    }

    // toolbarRadioButton
    public RemoteBotToolbarRadioButton toolbarRadioButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarRadioButton toolbarButton = widget
            .toolbarRadioButton(tooltip);
        stfToolbarRadioButton.setWidget(toolbarButton);
        return stfToolbarRadioButton;
    }

    // toolbarPushButton
    public RemoteBotToolbarPushButton toolbarPushButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarPushButton toolbarButton = widget
            .toolbarPushButton(tooltip);
        stfToolbarPushButton.setWidget(toolbarButton);
        return stfToolbarPushButton;
    }

    // toolbarToggleButton
    public RemoteBotToolbarToggleButton toolbarToggleButton(String tooltip)
        throws RemoteException {
        SWTBotToolbarToggleButton toolbarButton = widget
            .toolbarToggleButton(tooltip);
        stfToolbarToggleButton.setWidget(toolbarButton);
        return stfToolbarToggleButton;
    }

    /**********************************************
     * 
     * actions
     * 
     **********************************************/

    public void close() throws RemoteException {
        widget.close();
    }

    public void show() throws RemoteException {
        widget.show();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public List<String> getToolTipOfAllToolbarbuttons() throws RemoteException {
        List<String> tooltips = new ArrayList<String>();
        for (SWTBotToolbarButton button : widget.getToolbarButtons()) {
            tooltips.add(button.getToolTipText());
        }
        return tooltips;
    }

    public boolean existsToolbarButton(String tooltip) throws RemoteException {
        return getToolTipOfAllToolbarbuttons().contains(tooltip);
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
    }

    public String getTitle() throws RemoteException {
        return widget.getTitle();
    }

    public List<String> getToolTipTextOfToolbarButtons() throws RemoteException {
        List<String> toolbarButtons = new ArrayList<String>();
        for (SWTBotToolbarButton toolbarButton : widget.getToolbarButtons()) {
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

        stfBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isActive();
            }

            public String getFailureMessage() {
                return "Can't activate this view.";
            }
        });
    }

}
