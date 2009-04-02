package de.fu_berlin.inf.dpp.editor;

import org.eclipse.core.runtime.IPath;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.User;

public interface ISharedEditorListener {

    /**
     * The editor that the given user is currently editing has changed.
     * 
     * This method implies that the editor is being opened.
     * 
     * @param path
     *            the project-relative path of the resource that is the new
     *            driver resource.
     * @param user
     *            the user which removed an editor (can be the local user)
     * 
     */
    public void activeEditorChanged(User user, IPath path);

    /**
     * Is fired when the given editor is removed from the list of editors that
     * the given user has currently open.
     * 
     * @param path
     *            the path to the resource that the driver was editing.
     * 
     * @param user
     *            the user which removed an editor (can be the local user)
     */
    public void editorRemoved(User user, IPath path);

    /**
     * Is fired when the driver editor is saved.
     * 
     * @param path
     *            the project-relative path of the resource that is the new
     *            driver resource.
     * 
     * @param replicated
     *            <code>false</code> if this action originates on this client.
     *            <code>false</code> if it is an replication of an action from
     *            another participant of the shared project.
     */
    public void driverEditorSaved(IPath path, boolean replicated);

    /**
     * Is fired when the follow mode is changed.
     * 
     * @param user
     *            which is now being followed (may be null if no user is
     *            followed)
     */
    public void followModeChanged(@Nullable User user);
}
