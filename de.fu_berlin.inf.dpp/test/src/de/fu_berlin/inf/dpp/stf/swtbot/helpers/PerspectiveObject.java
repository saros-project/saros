package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

/**
 * org.eclipse.cdt.ui.CPerspective org.eclipse.debug.ui.DebugPerspective
 * org.eclipse.jdt.ui.JavaPerspective
 * org.eclipse.jdt.ui.JavaHierarchyPerspective
 * org.eclipse.jdt.ui.JavaBrowsingPerspective
 * org.eclipse.mylyn.tasks.ui.perspectives.planning
 * org.eclipse.pde.ui.PDEPerspective org.eclipse.team.cvs.ui.cvsPerspective
 * org.eclipse.team.ui.TeamSynchronizingPerspective
 * org.eclipse.ui.resourcePerspective
 * org.tigris.subversion.subclipse.ui.svnPerspective
 */
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
     * @param persID
     *            example: "org.eclipse.jdt.ui.JavaPerspective"
     */
    public void openPerspectiveWithId(final String persID)
        throws RemoteException {
        if (!isPerspectiveActive(persID)) {
            rmiBot.activateEclipseShell();
            try {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        final IWorkbench wb = PlatformUI.getWorkbench();
                        final IWorkbenchWindow win = wb
                            .getActiveWorkbenchWindow();
                        try {
                            wb.showPerspective(persID, win);
                        } catch (WorkbenchException e) {
                            log.debug("couldn't open perspective wit ID"
                                + persID, e);
                        }
                    }
                });
            } catch (IllegalArgumentException e) {
                log.debug("Couldn't initialize perspective with ID" + persID,
                    e.getCause());
            }

        }

    }

    public boolean isPerspectiveActive(String id) {
        return bot.perspectiveById(id).isActive();
    }

    // public boolean isPerspectiveOpen(String title) {
    // return getPerspectiveTitles().contains(title);
    // // try {
    // // return delegate.perspectiveByLabel(title) != null;
    // // } catch (WidgetNotFoundException e) {
    // // log.warn("perspective '" + title + "' doesn't exist!");
    // // return false;
    // // }
    // }

    // protected List<String> getPerspectiveTitles() {
    // ArrayList<String> list = new ArrayList<String>();
    // for (SWTBotPerspective perspective : bot.perspectives())
    // list.add(perspective.getLabel());
    // return list;
    // }
}
