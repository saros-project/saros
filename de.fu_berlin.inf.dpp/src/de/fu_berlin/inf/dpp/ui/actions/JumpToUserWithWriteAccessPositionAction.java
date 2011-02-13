package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Action which triggers the viewport of the local user to be changed to a local
 * user's one.
 */
@Component(module = "action")
public class JumpToUserWithWriteAccessPositionAction extends SelectionProviderAction
    implements Disposable {

    private static final Logger log = Logger
        .getLogger(JumpToUserWithWriteAccessPositionAction.class.getName());

    protected SarosSessionManager sessionManager;

    protected EditorManager editorManager;

    protected Saros saros;

    public JumpToUserWithWriteAccessPositionAction(Saros saros,
        SarosSessionManager sessionManager, EditorManager editorManager,
        ISelectionProvider provider) {
        super(provider, "Jump to position of selected buddy");

        setToolTipText("Jump to position of selected buddy");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/elcl16/jump.png"));

        this.saros = saros;
        this.editorManager = editorManager;
        this.sessionManager = sessionManager;

        selectionChanged(getStructuredSelection());
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(sessionManager.getSarosSession() != null
            && getSelectedUser() != null);
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                User jumpTo = getSelectedUser();
                assert jumpTo != null;
                editorManager.jumpToUser(jumpTo);
            }
        });
    }

    public User getSelectedUser() {
        Object selected = getStructuredSelection().getFirstElement();

        if (!(selected instanceof User))
            return null;

        User selectedUser = (User) selected;

        if (selectedUser.isLocal())
            return null;
        else
            return selectedUser;
    }
}
