package de.fu_berlin.inf.dpp.editor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivityReceiver;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.AutoHashMap;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * This class manages state of open editors of all drivers and connects
 * to/disconnects from the corresponding DocumentProviders to make sure that
 * TextEditActivities can be executed.
 * 
 * The main idea is to connect at observer site, when a driver activates his
 * editor with the document. Disconnect happens, when last driver closes the
 * editor.
 */
public class RemoteDriverManager {

    private static final Logger log = Logger
        .getLogger(RemoteDriverManager.class);

    // stores users and their opened files (identified by their path)
    protected Map<IPath, Set<User>> editorStates = AutoHashMap.getSetHashMap();

    // stores files (identified by their path) connected by at least one driver
    protected Set<IPath> connectedDriverFiles = new HashSet<IPath>();

    protected ISharedProject sharedProject;

    public RemoteDriverManager(final ISharedProject sharedProject) {
        this.sharedProject = sharedProject;
        this.sharedProject.addListener(sharedProjectListener);
    }

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        /**
         * Remove the user and potentially disconnect from the document
         * providers which only this user was connected to.
         */
        @Override
        public void userLeft(User user) {
            for (Entry<IPath, Set<User>> entry : editorStates.entrySet()) {
                if (entry.getValue().remove(user))
                    updateConnectionState(entry.getKey());
            }
        }

        /**
         * This method takes care of maintaining correct state of class-internal
         * tables with paths of connected documents, if the role of a user
         * changes.
         */
        @Override
        public void roleChanged(User user) {
            for (Entry<IPath, Set<User>> entry : editorStates.entrySet()) {
                if (entry.getValue().contains(user))
                    updateConnectionState(entry.getKey());
            }
        }
    };

    protected IActivityReceiver activityDataObjectReceiver = new AbstractActivityReceiver() {

        @Override
        public void receive(final EditorActivity editorActivityDataObject) {
            User sender = sharedProject.getUser(editorActivityDataObject
                .getSource());
            IPath path = editorActivityDataObject.getPath();

            if (path == null) {
                /**
                 * path == null means that the user has no active editor any
                 * more
                 */
                return;
            }

            switch (editorActivityDataObject.getType()) {
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
        sharedProject.removeListener(sharedProjectListener);

        for (Entry<IPath, Set<User>> entry : editorStates.entrySet()) {
            entry.getValue().clear();
            updateConnectionState(entry.getKey());
        }

        editorStates.clear();

        if (!connectedDriverFiles.isEmpty()) {
            log.warn("RemoteDriverManager could not"
                + " be dispose correctly. Still connect to: "
                + connectedDriverFiles.toString());
        }
    }

    /**
     * Connects a document under the given path as a reaction on a remote
     * activityDataObject of a driver (e.g. Activate Editor).
     */
    protected void connectDocumentProvider(IPath path) {

        assert !connectedDriverFiles.contains(path);

        IFile file = sharedProject.getProject().getFile(path);
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

        connectedDriverFiles.add(path);
    }

    /**
     * Disconnects a document under the given path as a reaction on a remote
     * activityDataObject of a driver (e.g. Close Editor)
     */
    protected void disconnectDocumentProvider(final IPath path) {

        assert connectedDriverFiles.contains(path);

        connectedDriverFiles.remove(path);

        IFile file = sharedProject.getProject().getFile(path);
        FileEditorInput input = new FileEditorInput(file);
        IDocumentProvider provider = EditorManager.getDocumentProvider(input);
        provider.disconnect(input);
    }

    /**
     * Updates the state of the document provider of a document under the given
     * path. This method looks if this document is already connected, and
     * whether it needs to get connected/disconnected now.
     */
    protected void updateConnectionState(final IPath path) {

        log.trace(".updateConnectionState(" + path.toOSString() + ")");

        boolean hadDriver = connectedDriverFiles.contains(path);
        boolean hasDriver = false;

        for (User user : editorStates.get(path)) {
            if (user.isDriver()) {
                hasDriver = true;
                break;
            }
        }

        if (!hadDriver && hasDriver) {
            log.trace(".updateConnectionState File " + path.toOSString()
                + " will be connected ");
            connectDocumentProvider(path);
        }

        if (hadDriver && !hasDriver) {
            log.trace(".updateConnectionState File " + path.toOSString()
                + " will be disconnected ");
            disconnectDocumentProvider(path);
        }
    }
}
