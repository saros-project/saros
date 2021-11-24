package saros.ui.handlers.popup;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.ui.wizards.ColorChooserWizard;

/**
 * This action opens a color dialog and checks whether the chosen color is different enough from
 * other colors. If yes, the new color will be sent to the sessionmembers If no, you can change a
 * new color or abort the process
 */
@Component(module = "action")
public final class ChangeColorHandler {

  public static final String ID = ChangeColorHandler.class.getName();

  private ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateEnablement();
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  private MDirectMenuItem changeColorMenuItem;

  public ChangeColorHandler() {
    SarosPluginContext.initComponent(this);
  }

  @PostConstruct
  public void postConstruct(
      ESelectionService selectionService, EModelService modelService, MPart sarosView) {
    selectionService.addSelectionListener(selectionListener);

    MPopupMenu popupMenu = null;

    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }
    MUIElement menuItem = modelService.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      changeColorMenuItem = (MDirectMenuItem) menuItem;
    }

    updateEnablement();
  }

  public void updateEnablement() {
    if (changeColorMenuItem == null) return;

    List<User> participants =
        SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();

    ISarosSession session = sessionManager.getSession();

    changeColorMenuItem.setEnabled(
        session != null
            && participants.size() == 1
            && participants.get(0).equals(session.getLocalUser()));
  }

  @Execute
  public void execute() {

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

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
  }
}
