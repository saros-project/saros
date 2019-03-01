package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.wizards.ColorChooserWizard;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;

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
