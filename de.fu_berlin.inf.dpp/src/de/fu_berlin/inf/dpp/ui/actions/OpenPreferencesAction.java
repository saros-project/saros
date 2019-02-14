package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

public class OpenPreferencesAction extends Action {

  public static final String ACTION_ID = OpenPreferencesAction.class.getName();

  private static final Logger LOG = Logger.getLogger(OpenPreferencesAction.class);

  public OpenPreferencesAction() {
    super(Messages.OpenPreferencesAction_title);

    setId(ACTION_ID);
    setToolTipText(Messages.OpenPreferencesAction_tooltip);
    setImageDescriptor(ImageManager.ELCL_OPEN_PREFERENCES);
    setEnabled(true);
  }

  @Override
  public void run() {
    IHandlerService service =
        (IHandlerService)
            PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage()
                .getActivePart()
                .getSite()
                .getService(IHandlerService.class);
    try {
      service.executeCommand(
          "de.fu_berlin.inf.dpp.ui.commands.OpenSarosPreferences", //$NON-NLS-1$
          null);
    } catch (Exception e) {
      LOG.error("Could not execute command", e); // $NON-NLS-1$
    }
  }
}
