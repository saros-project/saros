package de.fu_berlin.inf.dpp.ui.util;

import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.ui.views.SarosView;

public class ViewUtils {

    private static final Logger LOG = Logger.getLogger(ViewUtils.class);

    public static void openSarosView() {
        createView(SarosView.ID);
    }

    public static void bringViewToFront(String id) {
        showView(id, IWorkbenchPage.VIEW_VISIBLE);
    }

    public static void activateView(String id) {
        showView(id, IWorkbenchPage.VIEW_ACTIVATE);
    }

    public static void createView(String id) {
        showView(id, IWorkbenchPage.VIEW_CREATE);
    }

    /*
     * TODO What to do if no WorkbenchWindows are are active?
     */
    private static void showView(String id, int mode) {
        final IWorkbench workbench = PlatformUI.getWorkbench();

        if (workbench == null)
            return;

        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

        if (window == null)
            return;

        try {
            window.getActivePage().showView(id, null, mode);
        } catch (PartInitException e) {
            LOG.error(
                "could not access view with id: " + id + " [mode=" + mode + "]", e); //$NON-NLS-1$
        }
    }
}
