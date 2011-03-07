package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.RemoteWorkbenchBotImp;

public class AbstractRmoteWidget extends STF {

    protected static RemoteBotMenuImp stfBotMenu = RemoteBotMenuImp.getInstance();

    protected static RemoteBotTableItemImp stfBotTableItem = RemoteBotTableItemImp
        .getInstance();
    protected static RemoteBotTreeItemImp stfBotTreeItem = RemoteBotTreeItemImp
        .getInstance();

    protected static RemoteBotViewMenuImp stfViewMenu = RemoteBotViewMenuImp
        .getInstance();
    protected static RemoteBotToolbarDropDownButtonImp stfToolbarDropDownButton = RemoteBotToolbarDropDownButtonImp
        .getInstance();
    protected static RemoteBotToolbarPushButtonImp stfToolbarPushButton = RemoteBotToolbarPushButtonImp
        .getInstance();
    protected static RemoteBotToolbarRadioButtonImp stfToolbarRadioButton = RemoteBotToolbarRadioButtonImp
        .getInstance();
    protected static RemoteBotToolbarToggleButtonImp stfToolbarToggleButton = RemoteBotToolbarToggleButtonImp
        .getInstance();
    protected static RemoteBotToolbarButtonImp stfToolbarButton = RemoteBotToolbarButtonImp
        .getInstance();

    protected RemoteWorkbenchBotImp stfBot = RemoteWorkbenchBotImp.getInstance();

}
