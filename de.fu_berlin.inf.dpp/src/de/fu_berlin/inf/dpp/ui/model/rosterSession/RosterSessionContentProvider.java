package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.model.TreeContentProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterContentProvider;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;

/**
 * {@link IContentProvider} for use in conjunction with a {@link Roster} input.
 * <p>
 * Automatically keeps track of changes of buddies.
 * 
 * @author bkahlert
 */
public class RosterSessionContentProvider extends TreeContentProvider {
    protected Viewer viewer;
    protected RosterContentProvider rosterContentProvider = new RosterContentProvider();
    protected RosterSessionInput rosterSessionInput;

    @Inject
    /*
     * TODO: see
     * https://sourceforge.net/tracker/?func=detail&aid=3102858&group_id
     * =167540&atid=843362
     */
    protected EditorManager editorManager;
    protected ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user, boolean isFollowed) {
            UserElement userElement = getUserElement(rosterSessionInput, user);
            if (userElement != null)
                ViewerUtils.update(viewer, userElement, null);
        }

        @Override
        public void colorChanged() {
            ViewerUtils.refresh(viewer, true);
            ViewerUtils.expandAll(viewer);
        }
    };

    protected RosterListener rosterListener = new RosterListener() {
        public void entriesAdded(Collection<String> addresses) {
            // handled by RosterContentProvider
        }

        public void entriesUpdated(Collection<String> addresses) {
            // handled by RosterContentProvider
        }

        public void entriesDeleted(Collection<String> addresses) {
            // handled by RosterContentProvider
        }

        public void presenceChanged(Presence presence) {
            UserElement userElement = getUserElement(rosterSessionInput,
                presence.getFrom());
            if (userElement != null)
                ViewerUtils.update(viewer, userElement, null);
        }
    };

    protected ISharedProjectListener sharedProjectListener = new ISharedProjectListener() {
        public void userLeft(User user) {
            UserElement userElement = getUserElement(rosterSessionInput, user);
            if (userElement != null)
                ViewerUtils.remove(viewer, userElement);
        }

        public void userJoined(User user) {
            UserElement userElement = getUserElement(rosterSessionInput, user);
            if (userElement != null)
                ViewerUtils.add(viewer, sessionHeaderElement, userElement);
        }

        public void permissionChanged(User user) {
            UserElement userElement = getUserElement(rosterSessionInput, user);
            if (userElement != null)
                ViewerUtils.update(viewer, userElement, null);
        }

        public void invitationCompleted(User user) {
            UserElement userElement = getUserElement(rosterSessionInput, user);
            if (userElement != null)
                ViewerUtils.update(viewer, userElement, null);
        }
    };

    protected SessionHeaderElement sessionHeaderElement;
    protected RosterHeaderElement rosterHeaderElement;

    public RosterSessionContentProvider() {
        super();
        SarosPluginContext.initComponent(this);
        editorManager.addSharedEditorListener(sharedEditorListener);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;

        this.rosterContentProvider
            .inputChanged(
                viewer,
                (oldInput instanceof RosterSessionInput) ? ((RosterSessionInput) oldInput)
                    .getRoster() : null,
                (newInput instanceof RosterSessionInput) ? ((RosterSessionInput) newInput)
                    .getRoster() : null);

        if (oldInput instanceof RosterSessionInput) {
            RosterSessionInput oldRosterSessionInput = (RosterSessionInput) oldInput;
            if (oldRosterSessionInput.getRoster() != null) {
                oldRosterSessionInput.getRoster().removeRosterListener(
                    this.rosterListener);
            }
        }

        if (newInput instanceof RosterSessionInput) {
            RosterSessionInput newRosterSessionInput = (RosterSessionInput) newInput;
            this.rosterSessionInput = newRosterSessionInput;
            if (newRosterSessionInput.getRoster() != null) {
                newRosterSessionInput.getRoster().addRosterListener(
                    this.rosterListener);
            }
            if (newRosterSessionInput.getSarosSession() != null) {
                newRosterSessionInput.getSarosSession().addListener(
                    sharedProjectListener);
            }
        } else {
            this.rosterSessionInput = null;
        }

        createHeaders();
    }

    protected void createHeaders() {
        if (sessionHeaderElement != null)
            sessionHeaderElement.dispose();
        if (rosterHeaderElement != null)
            rosterHeaderElement.dispose();

        sessionHeaderElement = new SessionHeaderElement(viewer.getControl()
            .getFont(), rosterSessionInput);

        rosterHeaderElement = new RosterHeaderElement(viewer.getControl()
            .getFont(), this.rosterContentProvider,
            this.rosterSessionInput.getRoster());
    }

    @Override
    public void dispose() {
        if (this.rosterSessionInput != null) {
            if (this.rosterSessionInput.getSarosSession() != null) {
                this.rosterSessionInput.getSarosSession().removeListener(
                    sharedProjectListener);
            }
            if (this.rosterSessionInput.getRoster() != null) {
                this.rosterSessionInput.getRoster().removeRosterListener(
                    this.rosterListener);
            }
        }
        this.rosterContentProvider.dispose();
        editorManager.removeSharedEditorListener(sharedEditorListener);
    }

    /**
     * Returns {@link RosterGroup}s followed by {@link RosterEntry}s which don't
     * belong to any {@link RosterGroup}.
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement != null && inputElement instanceof RosterSessionInput) {
            List<Object> elements = new ArrayList<Object>();

            if (sessionHeaderElement != null)
                elements.add(sessionHeaderElement);

            if (rosterHeaderElement != null)
                elements.add(rosterHeaderElement);

            return elements.toArray();
        }

        return new Object[0];
    }

    /**
     * Creates an {@link UserElement} from an input element and some
     * {@link Presence} instance.
     * 
     * @param inputElement
     *            only {@link RosterSessionInput} is supported; other input
     *            results in null as the return
     * @param jid
     * @return
     */
    protected static UserElement getUserElement(Object inputElement, String jid) {
        if (inputElement instanceof RosterSessionInput) {
            ISarosSession sarosSession = ((RosterSessionInput) inputElement)
                .getSarosSession();
            if (sarosSession != null) {
                JID rqJID = sarosSession.getResourceQualifiedJID(new JID(jid));
                User user = sarosSession.getUser(rqJID);
                return getUserElement(inputElement, user);
            }
        }

        return null;
    }

    /**
     * Creates an {@link UserElement} from an input element and a {@link User}.
     * 
     * @param inputElement
     *            only {@link RosterSessionInput} is supported; other input
     *            results in null as the return
     * @param user
     * @return
     */
    protected static UserElement getUserElement(Object inputElement, User user) {
        if (inputElement instanceof RosterSessionInput) {
            Roster roster = ((RosterSessionInput) inputElement).getRoster();
            if (roster != null) {
                UserElement userElement = new UserElement(user, roster);
                return userElement;
            }
        }
        return null;
    }
}
