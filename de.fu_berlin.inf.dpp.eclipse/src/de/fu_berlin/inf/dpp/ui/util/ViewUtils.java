package de.fu_berlin.inf.dpp.ui.util;

import de.fu_berlin.inf.dpp.ui.views.SarosView;
import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ViewUtils {

  private static final Logger LOG = Logger.getLogger(ViewUtils.class);

  public static void openSarosView() {
    /*
     * TODO What to do if no WorkbenchWindows are are active?
     */
    final IWorkbench workbench = PlatformUI.getWorkbench();

    if (workbench == null) return;

    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

    if (window == null) return;

    try {
      window.getActivePage().showView(SarosView.ID, null, IWorkbenchPage.VIEW_CREATE);
    } catch (PartInitException e) {
      LOG.error("could not open Saros view (id: " + SarosView.ID + ")", e); // $NON-NLS-1$
    }
  }
}
