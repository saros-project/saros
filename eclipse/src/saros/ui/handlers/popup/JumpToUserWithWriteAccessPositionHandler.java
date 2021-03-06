package saros.ui.handlers.popup;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.viewers.ISelection;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.editor.EditorManager;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.ui.util.selection.SelectionUtils;
import saros.util.ThreadUtils;

/** Action which triggers the viewport of the local user to be changed to a local user's one. */
@Component(module = "action")
public class JumpToUserWithWriteAccessPositionHandler {

  public static final String ID = JumpToUserWithWriteAccessPositionHandler.class.getName();

  private static final Logger log =
      Logger.getLogger(JumpToUserWithWriteAccessPositionHandler.class);

  protected ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateEnablement();
        }
      };

  @Inject protected ISarosSessionManager sessionManager;

  @Inject protected EditorManager editorManager;

  private ESelectionService selectionService;
  private MDirectMenuItem jumpToUserWithWriteAccessPositionItem;

  private boolean workbenchIsClosing = false;

  public JumpToUserWithWriteAccessPositionHandler() {
    SarosPluginContext.initComponent(this);
  }

  @PostConstruct
  public void postConstruct(
      ESelectionService selectionService,
      EModelService modelService,
      MPart sarosView,
      IEventBroker eb) {
    this.selectionService = selectionService;

    MPopupMenu popupMenu = null;
    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }

    eb.subscribe(
        UILifeCycle.APP_SHUTDOWN_STARTED,
        new EventHandler() {

          @Override
          public void handleEvent(Event event) {
            workbenchIsClosing = true;
          }
        });

    MUIElement menuItem = modelService.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      jumpToUserWithWriteAccessPositionItem = (MDirectMenuItem) menuItem;
    }

    selectionService.addSelectionListener(selectionListener);
    updateEnablement();
  }

  public void updateEnablement() {
    try {
      List<User> participants =
          SelectionUtils.getAdaptableObjects(
              (ISelection) selectionService.getSelection(), User.class);
      jumpToUserWithWriteAccessPositionItem.setEnabled(
          sessionManager.getSession() != null
              && participants.size() == 1
              && !participants.get(0).equals(sessionManager.getSession().getLocalUser()));
    } catch (NullPointerException e) {
      jumpToUserWithWriteAccessPositionItem.setEnabled(false);
    } catch (Exception e) {
      if (!workbenchIsClosing)
        log.error("Unexpected error while updating enablement", e); // $NON-NLS-1$
    }
  }

  /** @review runSafe OK */
  @Execute
  public void execute() {
    ThreadUtils.runSafeSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            List<User> participants =
                SelectionUtils.getAdaptableObjects(
                    (ISelection) selectionService.getSelection(), User.class);
            if (participants.size() == 1) {
              editorManager.jumpToUser(participants.get(0));
            } else {
              log.warn("More than one participant selected."); // $NON-NLS-1$
            }
          }
        });
  }

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
  }
}
