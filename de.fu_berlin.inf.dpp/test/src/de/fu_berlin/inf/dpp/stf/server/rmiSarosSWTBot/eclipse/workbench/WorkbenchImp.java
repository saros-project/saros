package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipseComponentImp;

public class WorkbenchImp extends EclipseComponentImp implements Workbench {

    private static transient WorkbenchImp self;

    /**
     * {@link WorkbenchImp} is a singleton, but inheritance is possible.
     */
    public static WorkbenchImp getInstance() {
        if (self != null)
            return self;
        self = new WorkbenchImp();
        return self;
    }

    /**************************************************************
     * 
     * exported functions
     * 
     **************************************************************/
    /**********************************************
     * 
     * action
     * 
     **********************************************/

    public void sleep(long millis) throws RemoteException {
        bot.sleep(millis);
    }

    public void captureScreenshot(String filename) throws RemoteException {
        bot.captureScreenshot(filename);
    }

    public void activateWorkbench() throws RemoteException {
        getEclipseShell().activate().setFocus();
    }

    public void resetWorkbench() throws RemoteException {
        windowM.openPerspectiveJava();
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                final IWorkbench wb = PlatformUI.getWorkbench();
                final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                Shell[] shells = Display.getCurrent().getShells();
                for (Shell shell : shells) {
                    if (shell != null && shell != win.getShell()) {
                        shell.close();
                    }
                }
                IWorkbenchPage page = win.getActivePage();
                if (page != null) {
                    page.closeAllEditors(false);
                }
            }
        });
    }

    public void setUpWorkbench() throws RemoteException {
        resetWorkbench();
        editM.deleteAllProjectsNoGUI();
        editM.deleteAllProjects(VIEW_PACKAGE_EXPLORER);

    }

    public void closeUnnecessaryViews() throws RemoteException {

        if (bot().isViewOpen("Problems"))
            bot().view("Problems").close();

        if (bot().isViewOpen("Javadoc"))
            bot().view("Javadoc").close();

        if (bot().isViewOpen("Declaration"))
            bot().view("Declaration").close();

        if (bot().isViewOpen("Task List"))
            bot().view("Task List").close();

        if (bot().isViewOpen("Outline"))
            bot().view("Outline").close();
    }

    /**********************************************
     * 
     * state
     * 
     **********************************************/

    public String getPathToScreenShot() {
        Bundle bundle = saros.getBundle();
        log.debug("screenshot's directory: "
            + bundle.getLocation().substring(16) + SCREENSHOTDIR);
        if (getOS() == TypeOfOS.WINDOW)
            return bundle.getLocation().substring(16) + SCREENSHOTDIR;
        else if (getOS() == TypeOfOS.MAC) {
            return "/" + bundle.getLocation().substring(16) + SCREENSHOTDIR;
        }
        return bundle.getLocation().substring(16) + SCREENSHOTDIR;
    }

    /**********************************************
     * 
     * inner functions
     * 
     **********************************************/

    public SWTBotShell getEclipseShell() throws RemoteException {
        SWTBotShell[] shells = bot.shells();
        for (SWTBotShell shell : shells) {
            if (shell.getText().matches(".+? - .+")) {
                log.debug("shell found matching \"" + ".+? - .+" + "\"");
                return shell;
            }
        }
        final String message = "No shell found matching \"" + ".+? - .+"
            + "\"!";
        log.error(message);
        throw new RemoteException(message);
    }

}
