package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

/** Action which triggers the viewport of the local user to be changed to a local user's one. */
@Component(module = "action")
public class JumpToUserWithWriteAccessPositionAction extends Action implements Disposable {

  public static final String ACTION_ID = JumpToUserWithWriteAccessPositionAction.class.getName();

  private static final Logger LOG = Logger.getLogger(JumpToUserWithWriteAccessPositionAction.class);

  protected ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  @Inject protected ISarosSessionManager sessionManager;

  @Inject protected EditorManager editorManager;

  public JumpToUserWithWriteAccessPositionAction() {
    super(Messages.JumpToUserWithWriteAccessPositionAction_title);

    setId(ACTION_ID);
    setToolTipText(Messages.JumpToUserWithWriteAccessPositionAction_tooltip);
    setImageDescriptor(ImageManager.getImageDescriptor("icons/elcl16/jump.png")); // $NON-NLS-1$

    SarosPluginContext.initComponent(this);

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
    updateEnablement();
  }

  public void updateEnablement() {
    try {
      List<User> participants =
          SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();
      setEnabled(
          sessionManager.getSession() != null
              && participants.size() == 1
              && !participants.get(0).equals(sessionManager.getSession().getLocalUser()));
    } catch (NullPointerException e) {
      this.setEnabled(false);
    } catch (Exception e) {
      if (!PlatformUI.getWorkbench().isClosing())
        LOG.error("Unexpected error while updating enablement", e); // $NON-NLS-1$
    }
  }

  /** @review runSafe OK */
  @Override
  public void run() {
    ThreadUtils.runSafeSync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            List<User> participants =
                SelectionRetrieverFactory.getSelectionRetriever(User.class).getSelection();
            if (participants.size() == 1) {
              editorManager.jumpToUser(participants.get(0));
            } else {
              LOG.warn("More than one participant selected."); // $NON-NLS-1$
            }
          }
        });
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }
}
