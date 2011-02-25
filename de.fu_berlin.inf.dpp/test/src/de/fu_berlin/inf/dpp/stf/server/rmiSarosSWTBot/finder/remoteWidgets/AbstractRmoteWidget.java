package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.stf.STF;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.STFWorkbenchBotImp;

public class AbstractRmoteWidget extends STF {

    protected static STFBotMenuImp stfBotMenu = STFBotMenuImp.getInstance();

    protected static STFBotTableItemImp stfBotTableItem = STFBotTableItemImp
        .getInstance();
    protected static STFBotTreeItemImp stfBotTreeItem = STFBotTreeItemImp
        .getInstance();

    protected static STFBotViewMenuImp stfViewMenu = STFBotViewMenuImp
        .getInstance();
    protected static STFBotToolbarDropDownButtonImp stfToolbarDropDownButton = STFBotToolbarDropDownButtonImp
        .getInstance();
    protected static STFBotToolbarPushButtonImp stfToolbarPushButton = STFBotToolbarPushButtonImp
        .getInstance();
    protected static STFBotToolbarRadioButtonImp stfToolbarRadioButton = STFBotToolbarRadioButtonImp
        .getInstance();
    protected static STFBotToolbarToggleButtonImp stfToolbarToggleButton = STFBotToolbarToggleButtonImp
        .getInstance();
    protected static STFBotToolbarButtonImp stfToolbarButton = STFBotToolbarButtonImp
        .getInstance();

    protected STFWorkbenchBotImp stfBot = STFWorkbenchBotImp.getInstance();

    protected String getFileContentNoGUI(String filePath) {
        Bundle bundle = saros.getBundle();
        String content;
        try {
            content = FileUtils.read(bundle.getEntry(filePath));
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not open " + filePath);
        }
        return content;
    }

}
