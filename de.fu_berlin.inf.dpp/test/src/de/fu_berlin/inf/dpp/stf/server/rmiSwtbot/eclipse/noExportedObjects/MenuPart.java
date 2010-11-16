package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

/**
 * This class contains basic API to find widget menu in SWTBot and to perform
 * the operations on it, which is only used by rmi server side and not exported.
 * 
 * @author lchen
 */
public class MenuPart extends EclipseComponent {

    public void clickMenuWithTexts(String... texts) {
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

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub

    }
}
