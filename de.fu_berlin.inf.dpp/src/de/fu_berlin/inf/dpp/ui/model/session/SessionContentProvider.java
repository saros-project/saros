package de.fu_berlin.inf.dpp.ui.model.session;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;

public class SessionContentProvider implements IStructuredContentProvider,
    ISharedProjectListener {
    protected ISarosSession sarosSession;
    private TableViewer viewer;

    /**
     * Comparator for comparing users. The host has a lower rank than any client
     * (compare(host, client) = -1, compare(client, host) = 1. Clients are
     * compared alphabetically by JID (not case sensitive).
     */
    protected Comparator<User> alphabeticalUserComparator = new Comparator<User>() {
        public int compare(User user1, User user2) {
            if (user1.equals(user2))
                return 0;
            if (user1.isHost())
                return -1;
            if (user2.isHost())
                return +1;
            return user1.getJID().toString().toLowerCase()
                .compareTo(user2.getJID().toString().toLowerCase());
        }
    };

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {

        viewer = (TableViewer) v;

        if (oldInput != null && oldInput instanceof ISarosSession) {
            ISarosSession oldSarosSession = (ISarosSession) oldInput;
            oldSarosSession.removeListener(this);
        }

        if (newInput != null && newInput instanceof ISarosSession) {
            sarosSession = (ISarosSession) newInput;
            sarosSession.addListener(this);
        } else {
            sarosSession = null;
        }

    }

    public void dispose() {
        if (sarosSession != null) {
            sarosSession.removeListener(this);
        }
    }

    public Object[] getElements(Object inputElement) {

        if (inputElement instanceof ISarosSession) {
            User[] participants = ((ISarosSession) inputElement)
                .getParticipants().toArray(new User[] {});
            Arrays.sort(participants, alphabeticalUserComparator);

            return participants;
        } else {
            return new Object[0];
        }
    }

    public void roleChanged(User user) {
        refreshTable();
    }

    public void invitationCompleted(User user) {
        refreshTable();
    }

    public void userJoined(User user) {
        refreshTable();
    }

    public void userLeft(User user) {
        refreshTable();
    }

    public void refreshTable() {
        ViewerUtils.refresh(viewer, true);
    }

    public void permissionChanged(User user) {
        refreshTable();
    }
}
