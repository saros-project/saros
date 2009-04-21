package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class JumpToDriverPositionAction extends SelectionProviderAction
    implements Disposable {

    private static final Logger log = Logger
        .getLogger(JumpToDriverPositionAction.class.getName());

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected Saros saros;

    public JumpToDriverPositionAction(ISelectionProvider provider) {
        super(provider, "Jump to position of selected user");

        setToolTipText("Jump to position of selected user");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/table_edit.png"));

        Saros.getDefault().reinject(this);

        selectionChanged(getStructuredSelection());
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(sessionManager.getSharedProject() != null
            && getSelectedUser() != null);
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
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

        if (selectedUser.equals(sessionManager.getSharedProject()
            .getLocalUser()))
            return null;

        return selectedUser;
    }
}
