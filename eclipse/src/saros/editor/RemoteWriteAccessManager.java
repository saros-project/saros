package saros.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.ui.part.FileEditorInput;
import saros.activities.EditorActivity;
import saros.editor.internal.EditorAPI;
import saros.filesystem.IFile;
import saros.filesystem.ResourceConverter;
import saros.session.AbstractActivityConsumer;
import saros.session.IActivityConsumer;
import saros.session.ISarosSession;
import saros.session.ISessionListener;
import saros.session.User;
import saros.session.User.Permission;
import saros.util.StackTrace;

/**
 * This class manages state of open editors of all users with {@link Permission#WRITE_ACCESS} and
 * connects to/disconnects from the corresponding DocumentProviders to make sure that
 * TextEditActivities can be executed.<br>
 * The main idea is to connect at the site of user with {@link Permission#READONLY_ACCESS}, when a
 * user with {@link Permission#WRITE_ACCESS} activates his editor with the document. Disconnect
 * happens, when last user with {@link Permission#WRITE_ACCESS} closes the editor.
 *
 * <p>This class is an {@link IActivityConsumer} and it expects it's {@link
 * #exec(saros.activities.IActivity) exec()} method to be called -- it won't register itself as an
 * {@link IActivityConsumer}.
 */
public class RemoteWriteAccessManager extends AbstractActivityConsumer {

  private static final Logger log = Logger.getLogger(RemoteWriteAccessManager.class);

  /** stores users and their opened files */
  private final Map<IFile, Set<User>> editorStates = new HashMap<>();

  /** stores files connected by at least user with {@link Permission#WRITE_ACCESS} */
  private final Set<IFile> connectedUserWithWriteAccessFiles = new HashSet<>();

  private final ISarosSession sarosSession;

  RemoteWriteAccessManager(final ISarosSession sarosSession) {
    this.sarosSession = sarosSession;
    this.sarosSession.addListener(sessionListener);
  }

  @Override
  public void receive(final EditorActivity editorActivity) {
    User sender = editorActivity.getSource();
    IFile file = editorActivity.getResource();
    if (file == null) {
      /*
       * file == null means that the user has no active editor any more.
       */
      return;
    }

    switch (editorActivity.getType()) {
      case ACTIVATED:
        getEditorState(file).add(sender);
        break;
      case SAVED:
        break;
      case CLOSED:
        getEditorState(file).remove(sender);
        break;
      default:
        log.warn(".receive() Unknown Activity type");
    }
    updateConnectionState(file);
  }

  private ISessionListener sessionListener =
      new ISessionListener() {

        /**
         * Remove the user and potentially disconnect from the document providers which only this
         * user was connected to.
         */
        @Override
        public void userLeft(User user) {
          for (Entry<IFile, Set<User>> entry : editorStates.entrySet()) {
            if (entry.getValue().remove(user)) updateConnectionState(entry.getKey());
          }
        }

        /**
         * This method takes care of maintaining correct state of class-internal tables with files
         * of connected documents, if the permission of a user changes.
         */
        @Override
        public void permissionChanged(User user) {
          for (Entry<IFile, Set<User>> entry : editorStates.entrySet()) {
            if (entry.getValue().contains(user)) updateConnectionState(entry.getKey());
          }
        }
      };

  void dispose() {
    sarosSession.removeListener(sessionListener);

    for (Entry<IFile, Set<User>> entry : editorStates.entrySet()) {
      entry.getValue().clear();
      updateConnectionState(entry.getKey());
    }

    editorStates.clear();

    if (!connectedUserWithWriteAccessFiles.isEmpty()) {
      log.warn(
          "RemoteWriteAccessManager could not"
              + " be dispose correctly. Still connect to: "
              + connectedUserWithWriteAccessFiles);
    }
  }

  /**
   * Connects a document for the given file as a reaction on a remote Activity of a user with {@link
   * Permission#WRITE_ACCESS} (e.g. Activate Editor).
   */
  private void connectDocumentProvider(IFile fileWrapper) {

    assert !connectedUserWithWriteAccessFiles.contains(fileWrapper);

    org.eclipse.core.resources.IFile file = ResourceConverter.getDelegate(fileWrapper);
    if (!file.exists()) {
      log.error(
          "Attempting to connect to file which" + " is not available locally: " + fileWrapper,
          new StackTrace());
      return;
    }

    if (EditorAPI.connect(new FileEditorInput(file)) != null) {
      connectedUserWithWriteAccessFiles.add(fileWrapper);
    }
  }

  /**
   * Disconnects a document for the given file as a reaction on a remote Activity of a user with
   * {@link Permission#WRITE_ACCESS} (e.g. Close Editor)
   */
  private void disconnectDocumentProvider(final IFile fileWrapper) {

    assert connectedUserWithWriteAccessFiles.contains(fileWrapper);

    connectedUserWithWriteAccessFiles.remove(fileWrapper);

    org.eclipse.core.resources.IFile file = ResourceConverter.getDelegate(fileWrapper);
    EditorAPI.disconnect(new FileEditorInput(file));
  }

  /**
   * Updates the state of the document provider of a document for the given file. This method looks
   * if this document is already connected, and whether it needs to get connected/disconnected now.
   */
  private void updateConnectionState(final IFile file) {

    log.trace(".updateConnectionState(" + file.toString() + ")");

    boolean hadUserWithWriteAccess = connectedUserWithWriteAccessFiles.contains(file);
    boolean hasUserWithWriteAccess = false;

    for (User user : getEditorState(file)) {
      if (user.hasWriteAccess()) {
        hasUserWithWriteAccess = true;
        break;
      }
    }

    if (!hadUserWithWriteAccess && hasUserWithWriteAccess) {
      log.trace(".updateConnectionState File " + file.toString() + " will be connected ");
      connectDocumentProvider(file);
    }

    if (hadUserWithWriteAccess && !hasUserWithWriteAccess) {
      log.trace(".updateConnectionState File " + file.toString() + " will be disconnected ");
      disconnectDocumentProvider(file);
    }
  }

  private Set<User> getEditorState(final IFile file) {
    Set<User> editorState = editorStates.get(file);

    if (editorState == null) {
      editorState = new HashSet<User>();
      editorStates.put(file, editorState);
    }

    return editorState;
  }
}
