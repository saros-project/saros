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
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.net.RosterAdapter;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.internal.FollowingActivitiesManager;
import de.fu_berlin.inf.dpp.project.internal.IFollowModeChangesListener;
import de.fu_berlin.inf.dpp.ui.model.TreeContentProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterContentProvider;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;

/**
 * {@link IContentProvider} for use in conjunction with a {@link Roster roster}
 * input.
 * <p>
 * Automatically keeps track of changes of contacts.
 * 
 * @author bkahlert
 */
public final class RosterSessionContentProvider extends TreeContentProvider {

    private Viewer viewer;
    private RosterContentProvider rosterContentProvider = new RosterContentProvider();

    private SessionHeaderElement sessionHeaderElement;
    private RosterHeaderElement rosterHeaderElement;

    private Roster currentRoster;
    private ISarosSession currentSession;

    @Inject
    private EditorManager editorManager;

    @Inject
    private FollowingActivitiesManager followingActivitiesManager;

    private final IFollowModeChangesListener followModeChangesListener = new IFollowModeChangesListener() {

        @Override
        public void followModeChanged() {
            ViewerUtils.refresh(viewer, true);
            // FIXME expand the sessionHeaderElement not the whole viewer
            ViewerUtils.expandAll(viewer);
        }
    };

    private final ISharedEditorListener sharedEditorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user, boolean isFollowed) {
            UserElement userElement = getUserElement(currentRoster, user);
            if (userElement != null)
                ViewerUtils.update(viewer, userElement, null);
        }

        @Override
        public void activeEditorChanged(final User user, SPath path) {
            SWTUtils.runSafeSWTAsync(null, new Runnable() {
                @Override
                public void run() {
                    if (viewer.getControl().isDisposed())
                        return;

                    viewer.refresh();
                    viewer.getControl().redraw();
                }
            });
        }

        @Override
        public void colorChanged() {

            // does not force a redraw
            // ViewerUtils.refresh(viewer, true);

            SWTUtils.runSafeSWTAsync(null, new Runnable() {
                @Override
                public void run() {
                    if (viewer.getControl().isDisposed())
                        return;

                    viewer.getControl().redraw();
                }
            });
        }
    };

    // TODO call update and not refresh
    private final RosterListener rosterListener = new RosterAdapter() {
        // update nicknames
        @Override
        public void entriesUpdated(Collection<String> addresses) {
            ViewerUtils.refresh(viewer, true);
        }

        // update away icons
        @Override
        public void presenceChanged(Presence presence) {
            ViewerUtils.refresh(viewer, true);
        }
    };

    private final ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(User user) {
            UserElement userElement = getUserElement(currentRoster, user);
            if (userElement != null)
                ViewerUtils.remove(viewer, userElement);
        }

        @Override
        public void userJoined(User user) {
            UserElement userElement = getUserElement(currentRoster, user);
            if (userElement != null)
                ViewerUtils.add(viewer, sessionHeaderElement, userElement);

            // FIXME expand the sessionHeaderElement not the whole viewer
            ViewerUtils.expandAll(viewer);
        }

        @Override
        public void permissionChanged(User user) {
            UserElement userElement = getUserElement(currentRoster, user);
            if (userElement != null)
                ViewerUtils.update(viewer, userElement, null);
        }
    };

    public RosterSessionContentProvider() {
        SarosPluginContext.initComponent(this);
        editorManager.addSharedEditorListener(sharedEditorListener);
        followingActivitiesManager
            .addIinternalListener(followModeChangesListener);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;

        final Roster oldRoster = (oldInput instanceof RosterSessionInput) ? ((RosterSessionInput) oldInput)
            .getRoster() : null;

        final Roster newRoster = currentRoster = (newInput instanceof RosterSessionInput) ? ((RosterSessionInput) newInput)
            .getRoster() : null;

        final ISarosSession oldSession = (oldInput instanceof RosterSessionInput) ? ((RosterSessionInput) oldInput)
            .getSarosSession() : null;

        final ISarosSession newSession = currentSession = (newInput instanceof RosterSessionInput) ? ((RosterSessionInput) newInput)
            .getSarosSession() : null;

        rosterContentProvider.inputChanged(viewer, oldRoster, newRoster);

        if (oldRoster != null)
            oldRoster.removeRosterListener(rosterListener);

        if (oldSession != null)
            oldSession.removeListener(sharedProjectListener);

        disposeHeaderElements();

        if (!(newInput instanceof RosterSessionInput))
            return;

        createHeaders((RosterSessionInput) newInput);

        if (newRoster != null)
            newRoster.addRosterListener(rosterListener);

        if (newSession != null)
            newSession.addListener(sharedProjectListener);

    }

    private void disposeHeaderElements() {
        if (sessionHeaderElement != null)
            sessionHeaderElement.dispose();

        if (rosterHeaderElement != null)
            rosterHeaderElement.dispose();

        sessionHeaderElement = null;
        rosterHeaderElement = null;
    }

    private void createHeaders(RosterSessionInput input) {

        sessionHeaderElement = new SessionHeaderElement(viewer.getControl()
            .getFont(), input);

        rosterHeaderElement = new RosterHeaderElement(viewer.getControl()
            .getFont(), rosterContentProvider, input.getRoster());
    }

    @Override
    public void dispose() {
        if (currentSession != null)
            currentSession.removeListener(sharedProjectListener);

        if (currentRoster != null)
            currentRoster.removeRosterListener(rosterListener);

        editorManager.removeSharedEditorListener(sharedEditorListener);

        followingActivitiesManager
            .removeIinternalListener(followModeChangesListener);

        rosterContentProvider.dispose();

        disposeHeaderElements();

        /* ENSURE GC */
        currentSession = null;
        currentRoster = null;
        editorManager = null;
        rosterContentProvider = null;
        followingActivitiesManager = null;
    }

    /**
     * Returns {@link RosterGroup}s followed by {@link RosterEntry}s which don't
     * belong to any {@link RosterGroup}.
     */
    @Override
    public Object[] getElements(Object inputElement) {

        if (!(inputElement instanceof RosterSessionInput))
            return new Object[0];

        List<Object> elements = new ArrayList<Object>();

        if (sessionHeaderElement != null)
            elements.add(sessionHeaderElement);

        if (rosterHeaderElement != null)
            elements.add(rosterHeaderElement);

        return elements.toArray();
    }

    /**
     * Creates a {@link UserElement} from an input element and a {@link User}.
     * 
     * @param roster
     * @param user
     * @return a {@link UserElement} or <code>null</code> if the roster is
     *         <code>null</code>
     */
    private UserElement getUserElement(Roster roster, User user) {
        return roster == null ? null : new UserElement(user, roster);
    }
}
