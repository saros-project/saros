package saros.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import saros.ui.ImageManager;
import saros.ui.Messages;

public class OpenPreferencesAction extends Action {

  public static final String ACTION_ID = OpenPreferencesAction.class.getName();

  private static final Logger log = Logger.getLogger(OpenPreferencesAction.class);

  public OpenPreferencesAction() {
    super(Messages.OpenPreferencesAction_title);

    setId(ACTION_ID);
    setToolTipText(Messages.OpenPreferencesAction_tooltip);
    setImageDescriptor(ImageManager.getImageDescriptor(ImageManager.ELCL_PREFERENCES_OPEN));
    setEnabled(true);
  }

  @Override
  public void run() {
    IHandlerService service =
        PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage()
            .getActivePart()
            .getSite()
            .getService(IHandlerService.class);
    try {
      service.executeCommand(
          "saros.ui.commands.OpenSarosPreferences", //$NON-NLS-1$
          null);
    } catch (Exception e) {
      log.error("Could not execute command", e); // $NON-NLS-1$
    }
  }
}
