package saros.ui.actions;

import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.ui.wizards.ColorChooserWizard;

/**
 * This action opens a color dialog and checks whether the chosen color is different enough from
 * other colors. If yes, the new color will be sent to the sessionmembers If no, you can change a
 * new color or abort the process
 *
 * @author cnk and tobi
 */
@Component(module = "action")
public final class ChangeColorAction extends Action implements Disposable {

  public static final String ACTION_ID = ChangeColorAction.class.getName();

  private ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  public ChangeColorAction() {
    super(Messages.ChangeColorAction_title);
    SarosPluginContext.initComponent(this);

    setId(ACTION_ID);
    setToolTipText(Messages.ChangeColorAction_tooltip);

    setImageDescriptor(
        ImageManager.getImageDescriptor("icons/elcl16/changecolor.png")); // $NON-NLS-1$

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);

    updateEnablement();
  }

  public void updateEnablement() {
    List<User> participants =
        SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    ISarosSession session = sessionManager.getSession();

    setEnabled(
        session != null
            && participants.size() == 1
            && participants.get(0).equals(session.getLocalUser()));
  }

  @Override
  public void run() {

    ColorChooserWizard wizard = new ColorChooserWizard();

    WizardDialog dialog = new WizardDialog(SWTUtils.getShell(), wizard);
    dialog.setHelpAvailable(false);

    dialog.setBlockOnOpen(true);
    dialog.create();

    if (dialog.open() != Window.OK) return;

    ISarosSession session = sessionManager.getSession();

    if (session == null) return;

    int colorID = wizard.getChosenColor();

    if (!session.getUnavailableColors().contains(colorID)) session.changeColor(colorID);
    else
      MessageDialog.openInformation(
          SWTUtils.getShell(),
          Messages.ChangeColorAction_message_title,
          Messages.ChangeColorAction_message_text);
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }
}
