package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import java.rmi.RemoteException;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.workbench.SarosWorkbenchComponentImp;

/**
 * This class contains basic API to find widget menu in SWTBot and to perform
 * the operations on it, which is only used by rmi server side and not exported.
 * 
 * @author lchen
 */
public class MenuPart extends EclipseComponent {

    public final static String MENU_TITLE_OTHER = "Other...";
    public final static String MENU_TITLE_SHOW_VIEW = "Show View";
    public final static String MENU_TITLE_WINDOW = "Window";

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

    /**
     * Open a view using menus Window->Show View->Other... The method is defined
     * as helper method and should not be exported by rmi. <br/>
     * Operational steps:
     * <ol>
     * <li>If the view is already open, return.</li>
     * <li>Activate the saros-instance workbench(alice / bob / carl). If the
     * workbench isn't active, bot can't find the main menus.</li>
     * <li>Click main menus Window -> Show View -> Other....</li>
     * <li>Confirm the pop-up window "Show View".</li>
     * </ol>
     * 
     * @param title
     *            the title on the view tab.
     * @param category
     *            example: "General"
     * @param nodeName
     *            example: "Console"
     * @see SarosWorkbenchComponentImp#activateEclipseShell()
     * @see MenuPart#clickMenuWithTexts(String...)
     * 
     */
    public void openViewWithName(String category, String nodeName)
        throws RemoteException {
        workbenchC.activateEclipseShell();
        menuPart.clickMenuWithTexts(MENU_TITLE_WINDOW, MENU_TITLE_SHOW_VIEW,
            MENU_TITLE_OTHER);
        windowPart.confirmWindowWithTreeWithFilterText(MENU_TITLE_SHOW_VIEW,
            category, nodeName, OK);

    }

    // private final static String WINDOW = "Window";
    // private final static String SHOW_VIEW =
}
