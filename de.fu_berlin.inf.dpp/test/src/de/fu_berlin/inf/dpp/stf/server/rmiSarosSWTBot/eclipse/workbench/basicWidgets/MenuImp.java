package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.basicWidgets;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class MenuImp extends EclipsePart implements Menu {

    private static transient MenuImp menuImp;

    /**
     * {@link TableImp} is a singleton, but inheritance is possible.
     */
    public static MenuImp getInstance() {
        if (menuImp != null)
            return menuImp;
        menuImp = new MenuImp();
        return menuImp;
    }

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

}
