package saros.ui.actions;

import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import saros.SarosPluginContext;
import saros.communication.extensions.JoinSessionRequestExtension;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.Messages;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

/**
 * Action for requesting an invitation to a session from a contact.
 *
 * <p>This currently relies on the fact, that only Saros/S has a working JoinSessionRequestHandler.
 * To make this feature generic in the future we need to add another XMPP namespace
 */
public class RequestSessionInviteAction extends Action implements Disposable {

  @Inject private ISarosSessionManager sessionManager;
  @Inject private ITransmitter transmitter;

  private ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateActionState();
        }
      };

  public static final String ACTION_ID = RequestSessionInviteAction.class.getName();

  public RequestSessionInviteAction() {
    super(Messages.RequestSessionInviteAction_title);
    setId(ACTION_ID);
    SarosPluginContext.initComponent(this);
    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
    updateActionState();
  }

  @Override
  public void run() {
    ISarosSession session = sessionManager.getSession();
    JID jid = getSelectedJID();
    if (session != null || jid == null) {
      return;
    }

    transmitter.sendPacketExtension(
        jid, JoinSessionRequestExtension.PROVIDER.create(new JoinSessionRequestExtension()));
  }

  private JID getSelectedJID() {
    List<JID> selected = SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    if (selected.size() != 1) return null;

    return selected.get(0);
  }

  private void updateActionState() {
    ISarosSession session = sessionManager.getSession();
    setEnabled(session == null && getSelectedJID() != null);
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }
}
