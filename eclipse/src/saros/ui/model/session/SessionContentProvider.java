package saros.ui.model.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jface.viewers.Viewer;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.activities.SPath;
import saros.awareness.AwarenessInformationCollector;
import saros.editor.EditorManager;
import saros.editor.FollowModeManager;
import saros.editor.IFollowModeListener;
import saros.editor.ISharedEditorListener;
import saros.net.mdns.MDNSService;
import saros.net.xmpp.roster.IRosterListener;
import saros.project.internal.FollowingActivitiesManager;
import saros.project.internal.IFollowModeChangesListener;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;
import saros.ui.model.HeaderElement;
import saros.ui.model.TreeContentProvider;
import saros.ui.model.mdns.MDNSContentProvider;
import saros.ui.model.mdns.MDNSHeaderElement;
import saros.ui.model.roster.RosterContentProvider;
import saros.ui.model.roster.RosterHeaderElement;
import saros.ui.util.SWTUtils;
import saros.ui.util.ViewerUtils;

public class SessionContentProvider extends TreeContentProvider {

  private Viewer viewer;

  private TreeContentProvider additionalContentProvider;

  private HeaderElement sessionHeaderElement;
  private HeaderElement contentHeaderElement;

  private Roster currentRoster;
  private ISarosSession currentSession;

  @Inject private EditorManager editorManager;

  @Inject private AwarenessInformationCollector collector;

  public SessionContentProvider(TreeContentProvider additionalContent) {
    SarosPluginContext.initComponent(this);

    this.additionalContentProvider = additionalContent;

    editorManager.addSharedEditorListener(sharedEditorListener);
  }

  private FollowingActivitiesManager followingTracker;

  private final IFollowModeChangesListener remoteFollowModeChanges =
      new IFollowModeChangesListener() {

        @Override
        public void followModeChanged() {
          ViewerUtils.refresh(viewer, true);
          // FIXME expand the sessionHeaderElement not the whole viewer
          ViewerUtils.expandAll(viewer);
        }
      };

  private FollowModeManager followModeManager;

  private final IFollowModeListener localFollowModeChanges =
      new IFollowModeListener() {

        @Override
        public void stoppedFollowing(Reason reason) {
          ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void startedFollowing(User target) {
          ViewerUtils.update(viewer, new UserElement(target, editorManager, collector), null);
        }
      };

  private final ISharedEditorListener sharedEditorListener =
      new ISharedEditorListener() {
        @Override
        public void editorActivated(final User user, SPath filePath) {
          SWTUtils.runSafeSWTAsync(
              null,
              new Runnable() {
                @Override
                public void run() {
                  if (viewer.getControl().isDisposed()) return;

                  viewer.refresh();
                  viewer.getControl().redraw();
                }
              });
        }
      };

  // TODO call update and not refresh
  private final RosterListener rosterListener =
      new IRosterListener() {
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

  /*
   * as we have a filter installed that will hide contacts from the contact
   * list that are currently part of the session we must currently do a full
   * refresh otherwise the viewer is not correctly updated
   */
  private final ISessionListener sessionListener =
      new ISessionListener() {
        @Override
        public void userLeft(User user) {
          // UserElement userElement = getUserElement(currentRoster, user);
          // if (userElement != null)
          // ViewerUtils.remove(viewer, userElement);
          ViewerUtils.refresh(viewer, true);
        }

        @Override
        public void userJoined(User user) {
          // UserElement userElement = getUserElement(currentRoster, user);
          // if (userElement != null)
          // ViewerUtils.add(viewer, sessionHeaderElement, userElement);

          ViewerUtils.refresh(viewer, true);

          // FIXME expand the sessionHeaderElement not the whole viewer
          ViewerUtils.expandAll(viewer);
        }

        @Override
        public void permissionChanged(User user) {
          ViewerUtils.update(viewer, new UserElement(user, editorManager, collector), null);
        }

        @Override
        public void userColorChanged(User user) {

          // does not force a redraw
          // ViewerUtils.refresh(viewer, true);

          SWTUtils.runSafeSWTAsync(
              null,
              new Runnable() {
                @Override
                public void run() {
                  if (viewer.getControl().isDisposed()) return;

                  viewer.getControl().redraw();
                }
              });
        }
      };

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    this.viewer = viewer;

    final Roster oldRoster = getRoster(oldInput);

    final Roster newRoster = currentRoster = getRoster(newInput);

    final ISarosSession oldSession = getSession(oldInput);

    final ISarosSession newSession = currentSession = getSession(newInput);

    if (followingTracker != null) followingTracker.removeListener(remoteFollowModeChanges);

    if (followModeManager != null) followModeManager.removeListener(localFollowModeChanges);

    if (additionalContentProvider != null)
      additionalContentProvider.inputChanged(viewer, getContent(oldInput), getContent(newInput));

    if (oldRoster != null) oldRoster.removeRosterListener(rosterListener);

    if (oldSession != null) oldSession.removeListener(sessionListener);

    disposeHeaderElements();

    if (!(newInput instanceof SessionInput)) return;

    createHeaders((SessionInput) newInput);

    if (newRoster != null) newRoster.addRosterListener(rosterListener);

    if (newSession != null) {
      newSession.addListener(sessionListener);

      followingTracker = newSession.getComponent(FollowingActivitiesManager.class);

      if (followingTracker != null) followingTracker.addListener(remoteFollowModeChanges);

      followModeManager = newSession.getComponent(FollowModeManager.class);

      if (followModeManager != null) followModeManager.addListener(localFollowModeChanges);
    }
  }

  private void disposeHeaderElements() {
    if (sessionHeaderElement != null) sessionHeaderElement.dispose();

    if (contentHeaderElement != null) contentHeaderElement.dispose();

    sessionHeaderElement = null;
    contentHeaderElement = null;
  }

  // TODO abstract !
  private void createHeaders(SessionInput input) {

    sessionHeaderElement =
        new SessionHeaderElement(viewer.getControl().getFont(), input, editorManager, collector);

    if (additionalContentProvider instanceof RosterContentProvider) {
      contentHeaderElement =
          new RosterHeaderElement(
              viewer.getControl().getFont(),
              (RosterContentProvider) additionalContentProvider,
              (Roster) input.getCustomContent());
    } else if (additionalContentProvider instanceof MDNSContentProvider) {
      contentHeaderElement =
          new MDNSHeaderElement(
              viewer.getControl().getFont(),
              (MDNSContentProvider) additionalContentProvider,
              (MDNSService) input.getCustomContent());
    }
  }

  @Override
  public void dispose() {
    if (currentSession != null) currentSession.removeListener(sessionListener);

    if (currentRoster != null) currentRoster.removeRosterListener(rosterListener);

    editorManager.removeSharedEditorListener(sharedEditorListener);

    if (followingTracker != null) followingTracker.removeListener(remoteFollowModeChanges);

    if (followModeManager != null) followModeManager.removeListener(localFollowModeChanges);

    if (additionalContentProvider != null) additionalContentProvider.dispose();

    disposeHeaderElements();

    /* ENSURE GC */
    currentSession = null;
    currentRoster = null;
    editorManager = null;
    additionalContentProvider = null;
    followingTracker = null;
    followModeManager = null;
  }

  /**
   * Returns {@link RosterGroup}s followed by {@link RosterEntry}s which don't belong to any {@link
   * RosterGroup}.
   */
  @Override
  public Object[] getElements(Object inputElement) {

    if (!(inputElement instanceof SessionInput)) return new Object[0];

    List<Object> elements = new ArrayList<Object>();

    if (sessionHeaderElement != null) elements.add(sessionHeaderElement);

    if (contentHeaderElement != null) elements.add(contentHeaderElement);

    return elements.toArray();
  }

  private ISarosSession getSession(Object input) {

    if (!(input instanceof SessionInput)) return null;

    return ((SessionInput) input).getSession();
  }

  private Roster getRoster(Object input) {
    if (!(input instanceof SessionInput)) return null;

    Object roster = ((SessionInput) input).getCustomContent();

    if (roster instanceof Roster) return (Roster) roster;

    return null;
  }

  private Object getContent(Object input) {
    if (!(input instanceof SessionInput)) return null;

    return ((SessionInput) input).getCustomContent();
  }
}
