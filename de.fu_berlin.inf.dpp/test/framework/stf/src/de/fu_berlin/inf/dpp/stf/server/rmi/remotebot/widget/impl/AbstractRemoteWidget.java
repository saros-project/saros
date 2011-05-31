package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import de.fu_berlin.inf.dpp.stf.server.STFMessage;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.impl.RemoteWorkbenchBot;

public class AbstractRemoteWidget extends STFMessage {

    protected static RemoteBotMenu stfBotMenu = RemoteBotMenu.getInstance();

    protected static RemoteBotTableItem stfBotTableItem = RemoteBotTableItem
        .getInstance();
    protected static RemoteBotTreeItem stfBotTreeItem = RemoteBotTreeItem
        .getInstance();

    protected static RemoteBotViewMenu stfViewMenu = RemoteBotViewMenu
        .getInstance();
    protected static RemoteBotToolbarDropDownButton stfToolbarDropDownButton = RemoteBotToolbarDropDownButton
        .getInstance();
    protected static RemoteBotToolbarPushButton stfToolbarPushButton = RemoteBotToolbarPushButton
        .getInstance();
    protected static RemoteBotToolbarRadioButton stfToolbarRadioButton = RemoteBotToolbarRadioButton
        .getInstance();
    protected static RemoteBotToolbarToggleButton stfToolbarToggleButton = RemoteBotToolbarToggleButton
        .getInstance();
    protected static RemoteBotToolbarButton stfToolbarButton = RemoteBotToolbarButton
        .getInstance();

    protected RemoteWorkbenchBot stfBot = RemoteWorkbenchBot.getInstance();

}
