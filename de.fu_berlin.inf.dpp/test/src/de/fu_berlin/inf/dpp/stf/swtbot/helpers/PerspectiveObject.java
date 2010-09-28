package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class PerspectiveObject {
    private static final transient Logger log = Logger
        .getLogger(PerspectiveObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private MenuObject menuObject;

    private static SarosSWTWorkbenchBot bot = new SarosSWTWorkbenchBot();

    public PerspectiveObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.wUntil = rmiBot.wUntilObject;
        this.menuObject = rmiBot.menuObject;

    }

    /**
     * Open a perspective using Window->Open Perspective->Other... The method is
     * defined as helper method for other openPerspective* methods and should
     * not be exported using rmi.
     * 
     * 1. if the perspective already exist, return.
     * 
     * 2. activate the saros-instance-window(alice / bob / carl). If the
     * workbench isn't active, delegate can't find the main menus.
     * 
     * 3. click main menus Window -> Open perspective -> Other....
     * 
     * 4. confirm the pop-up window "Open Perspective".
     * 
     * @param nodeName
     *            example: "Java"
     */
    public void openPerspectiveWithName(String nodeName) throws RemoteException {
        if (!isPerspectiveActive(nodeName)) {
            rmiBot.activateEclipseShell();
            menuObject.clickMenuWithTexts(SarosConstant.MENU_TITLE_WINDOW,
                SarosConstant.MENU_TITLE_OPEN_PERSPECTIVE,
                SarosConstant.MENU_TITLE_OTHER);
            rmiBot.confirmWindowWithTable(
                SarosConstant.MENU_TITLE_OPEN_PERSPECTIVE, nodeName,
                SarosConstant.BUTTON_OK);
        }
    }

    public boolean isPerspectiveActive(String title) {
        if (!isPerspectiveOpen(title))
            return false;
        return bot.activePerspective().getLabel().equals(title);
        // try {
        // return delegate.perspectiveByLabel(title).isActive();
        // } catch (WidgetNotFoundException e) {
        // log.warn("perspective '" + title + "' doesn't exist!");
        // return false;
        // }
    }

    public boolean isPerspectiveOpen(String title) {
        return getPerspectiveTitles().contains(title);
        // try {
        // return delegate.perspectiveByLabel(title) != null;
        // } catch (WidgetNotFoundException e) {
        // log.warn("perspective '" + title + "' doesn't exist!");
        // return false;
        // }
    }

    protected List<String> getPerspectiveTitles() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotPerspective perspective : bot.perspectives())
            list.add(perspective.getLabel());
        return list;
    }
}
