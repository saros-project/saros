package saros.core.editor;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;
import saros.activities.AbstractActivityReceiver;
import saros.activities.EditorActivity;
import saros.activities.IActivity;
import saros.activities.IActivityReceiver;
import saros.activities.SPath;
import saros.core.util.AutoHashMap;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;
import saros.session.User.Permission;

/**
 * This class manages state of open editors of all users with {@link Permission#WRITE_ACCESS} and
 * connects to/disconnects from the corresponding DocumentProviders to make sure that
 * TextEditActivities can be executed.
 *
 * <p>The main idea is to connect at the site of user with {@link Permission#READONLY_ACCESS}, when
 * a user with {@link Permission#WRITE_ACCESS} activates his editor with the document. Disconnect
 * happens, when last user with {@link Permission#WRITE_ACCESS} closes the editor.
 */
public class RemoteWriteAccessManager {

  private static final Logger LOG = Logger.getLogger(RemoteWriteAccessManager.class);

  /** stores users and their opened files (identified by their path) */
  protected Map<SPath, Set<User>> editorStates = AutoHashMap.getSetHashMap();

  /**
   * stores files (identified by their path) connected by at least user with {@link
   * Permission#WRITE_ACCESS}
   */
  protected Set<SPath> connectedUserWithWriteAccessFiles = new HashSet<SPath>();

  protected ISarosSession sarosSession;

  /**
   * Creates a new RemoteWriteAccessManager and adds the session listener to the session.
   *
   * @param sarosSession
   */
  public RemoteWriteAccessManager(final ISarosSession sarosSession) {
    this.sarosSession = sarosSession;
    this.sarosSession.addListener(sessionListener);
  }

  protected ISessionListener sessionListener =
      new ISessionListener() {

        /**
         * Remove the user and potentially disconnect from the document providers which only this
         * user was connected to.
         */
        @Override
        public void userLeft(User user) {
          for (Entry<SPath, Set<User>> entry : editorStates.entrySet()) {
            if (entry.getValue().remove(user)) updateConnectionState(entry.getKey());
          }
        }

        /**
         * This method takes care of maintaining correct state of class-internal tables with paths
         * of connected documents, if the permission of a user changes.
         */
        @Override
        public void permissionChanged(User user) {
          for (Entry<SPath, Set<User>> entry : editorStates.entrySet()) {
            if (entry.getValue().contains(user)) updateConnectionState(entry.getKey());
          }
        }
      };

  protected IActivityReceiver activityReceiver =
      new AbstractActivityReceiver() {

        @Override
        public void receive(final EditorActivity editorActivity) {
          User sender = editorActivity.getSource();
          SPath path = editorActivity.getPath();
          if (path == null) {
            /*
             * sPath == null means that the user has no active editor any
             * more.
             */
            return;
          }

          switch (editorActivity.getType()) {
            case ACTIVATED:
              editorStates.get(path).add(sender);
              break;
            case SAVED:
              break;
            case CLOSED:
              editorStates.get(path).remove(sender);
              break;
            default:
              LOG.warn(".receive() Unknown Activity type");
          }
          updateConnectionState(path);
        }
      };

  /**
   * This method is called from the shared project when a new Activity arrives
   *
   * @param activity activity to dispatch
   */
  public void exec(final IActivity activity) {
    activity.dispatch(activityReceiver);
  }

  /** Removes all listener and clears all editorStates. */
  public void dispose() {
    sarosSession.removeListener(sessionListener);

    for (Entry<SPath, Set<User>> entry : editorStates.entrySet()) {
      entry.getValue().clear();
      updateConnectionState(entry.getKey());
    }

    editorStates.clear();

    if (!connectedUserWithWriteAccessFiles.isEmpty()) {
      LOG.warn(
          "RemoteWriteAccessManager could not"
              + " be dispose correctly. Still connect to: "
              + connectedUserWithWriteAccessFiles.toString());
    }
  }

  /**
   * Updates the state of the document provider of a document under the given path. This method
   * looks if this document is already connected, and whether it needs to get connected/disconnected
   * now.
   */
  protected void updateConnectionState(final SPath path) {

    LOG.trace(".updateConnectionState(" + path.toString() + ")");

    boolean hadUserWithWriteAccess = connectedUserWithWriteAccessFiles.contains(path);
    boolean hasUserWithWriteAccess = false;

    for (User user : editorStates.get(path)) {
      if (user.hasWriteAccess()) {
        hasUserWithWriteAccess = true;
        break;
      }
    }
    // FIXME: Test if this works without this commentS
    /* if (!hadUserWithWriteAccess && hasUserWithWriteAccess) {
        LOG.trace(".updateConnectionState File " + path.toString()
                + " will be connected ");
        connectDocumentProvider(path);
    }

    if (hadUserWithWriteAccess && !hasUserWithWriteAccess) {
        LOG.trace(".updateConnectionState File " + path.toString()
                + " will be disconnected ");
        disconnectDocumentProvider(path);
    }*/
  }
}
