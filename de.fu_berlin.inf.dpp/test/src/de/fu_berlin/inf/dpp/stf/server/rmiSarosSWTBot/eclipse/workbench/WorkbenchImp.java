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
        if (viewW.isViewOpen("Problems"))
            viewW.closeViewByTitle("Problems");
        if (viewW.isViewOpen("Javadoc"))
            viewW.closeViewByTitle("Javadoc");
        if (viewW.isViewOpen("Declaration"))
            viewW.closeViewByTitle("Declaration");
        if (viewW.isViewOpen("Task List"))
            viewW.closeViewByTitle("Task List");
        if (viewW.isViewOpen("Outline"))
            viewW.closeViewByTitle("Outline");
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
