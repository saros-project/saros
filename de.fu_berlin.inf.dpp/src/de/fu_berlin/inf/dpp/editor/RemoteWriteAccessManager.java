package de.fu_berlin.inf.dpp.editor;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * This class manages state of open editors of all users with
 * {@link User.Permission#WRITE_ACCESS} and connects to/disconnects from the
 * corresponding DocumentProviders to make sure that TextEditActivities can be
 * executed.
 * 
 * The main idea is to connect at the site of user with
 * {@link User.Permission#READONLY_ACCESS}, when a user with
 * {@link User.Permission#WRITE_ACCESS} activates his editor with the document.
 * Disconnect happens, when last user with {@link User.Permission#WRITE_ACCESS}
 * closes the editor.
 */
public class RemoteWriteAccessManager {

    private static final Logger log = Logger
        .getLogger(RemoteWriteAccessManager.class);

    /** stores users and their opened files (identified by their path) */
    protected Map<SPath, Set<User>> editorStates = AutoHashMap.getSetHashMap();

    /**
     * stores files (identified by their path) connected by at least user with
     * {@link User.Permission#WRITE_ACCESS}
     */
    protected Set<SPath> connectedUserWithWriteAccessFiles = new HashSet<SPath>();

    protected ISarosSession sarosSession;

    public RemoteWriteAccessManager(final ISarosSession sarosSession) {
        this.sarosSession = sarosSession;
        this.sarosSession.addListener(sharedProjectListener);
    }

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        /**
         * Remove the user and potentially disconnect from the document
         * providers which only this user was connected to.
         */
        @Override
        public void userLeft(User user) {
            for (Entry<SPath, Set<User>> entry : editorStates.entrySet()) {
                if (entry.getValue().remove(user))
                    updateConnectionState(entry.getKey());
            }
        }

        /**
         * This method takes care of maintaining correct state of class-internal
         * tables with paths of connected documents, if the permission of a user
         * changes.
         */
        @Override
        public void permissionChanged(User user) {
            for (Entry<SPath, Set<User>> entry : editorStates.entrySet()) {
                if (entry.getValue().contains(user))
                    updateConnectionState(entry.getKey());
            }
        }
    };

    protected IActivityReceiver activityDataObjectReceiver = new AbstractActivityReceiver() {

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
            case Activated:
                editorStates.get(path).add(sender);
                break;
            case Saved:
                break;
            case Closed:
                editorStates.get(path).remove(sender);
                break;
            default:
                log.warn(".receive() Unknown Activity type");
            }
            updateConnectionState(path);
        }

    };

    /**
     * This method is called from the shared project when a new
     * activityDataObject arrives
     */
    public void exec(final IActivity activityDataObject) {
        activityDataObject.dispatch(activityDataObjectReceiver);
    }

    public void dispose() {
        sarosSession.removeListener(sharedProjectListener);

        for (Entry<SPath, Set<User>> entry : editorStates.entrySet()) {
            entry.getValue().clear();
            updateConnectionState(entry.getKey());
        }

        editorStates.clear();

        if (!connectedUserWithWriteAccessFiles.isEmpty()) {
            log.warn("RemoteWriteAccessManager could not"
                + " be dispose correctly. Still connect to: "
                + connectedUserWithWriteAccessFiles.toString());
        }
    }

    /**
     * Connects a document under the given path as a reaction on a remote
     * activityDataObject of a user with {@link User.Permission#WRITE_ACCESS}
     * (e.g. Activate Editor).
     */
    protected void connectDocumentProvider(SPath path) {

        assert !connectedUserWithWriteAccessFiles.contains(path);

        IFile file = path.getFile();
        if (!file.exists()) {
            log.error("Attempting to connect to file which"
                + " is not available locally: " + path, new StackTrace());
            return;
        }

        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = EditorManager.getDocumentProvider(input);
        try {
            provider.connect(input);
        } catch (CoreException e) {
            log.error("Could not connect to a document provider on file '"
                + file.toString() + "':", e);
            return;
        }

        connectedUserWithWriteAccessFiles.add(path);
    }

    /**
     * Disconnects a document under the given path as a reaction on a remote
     * activityDataObject of a user with {@link User.Permission#WRITE_ACCESS}
     * (e.g. Close Editor)
     */
    protected void disconnectDocumentProvider(final SPath path) {

        assert connectedUserWithWriteAccessFiles.contains(path);

        connectedUserWithWriteAccessFiles.remove(path);

        IFile file = path.getFile();
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = EditorManager.getDocumentProvider(input);
        provider.disconnect(input);
    }

    /**
     * Updates the state of the document provider of a document under the given
     * path. This method looks if this document is already connected, and
     * whether it needs to get connected/disconnected now.
     */
    protected void updateConnectionState(final SPath path) {

        log.trace(".updateConnectionState(" + path.toString() + ")");

        boolean hadUserWithWriteAccess = connectedUserWithWriteAccessFiles
            .contains(path);
        boolean hasUserWithWriteAccess = false;

        for (User user : editorStates.get(path)) {
            if (user.hasWriteAccess()) {
                hasUserWithWriteAccess = true;
                break;
            }
        }

        if (!hadUserWithWriteAccess && hasUserWithWriteAccess) {
            log.trace(".updateConnectionState File " + path.toString()
                + " will be connected ");
            connectDocumentProvider(path);
        }

        if (hadUserWithWriteAccess && !hasUserWithWriteAccess) {
            log.trace(".updateConnectionState File " + path.toString()
                + " will be disconnected ");
            disconnectDocumentProvider(path);
        }
    }
}
