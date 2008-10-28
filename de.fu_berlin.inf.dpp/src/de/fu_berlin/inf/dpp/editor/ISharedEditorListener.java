package de.fu_berlin.inf.dpp.editor;

import org.eclipse.core.runtime.IPath;

public interface ISharedEditorListener {
	/**
	 * The resource that the driver is currently editting has changed.
	 * 
	 * @param path
	 *            the project-relative path of the resource that is the new
	 *            driver resource.
	 * 
	 * @param replicated
	 *            <code>false</code> if this event was created by this client.
	 *            <code>true</code> if it was created by another client and
	 *            only replicated to this client.
	 */
	public void activeDriverEditorChanged(IPath path, boolean replicated);

	/**
	 * Is fired when the given editor is removed from the list of editors that
	 * the driver is currently using.
	 * 
	 * @param path
	 *            the path to the resource that the driver was editting.
	 * @param replicated
	 *            <code>false</code> if this action originates on this client.
	 *            <code>false</code> if it is an replication of an action from
	 *            another participant of the shared project.
	 */
	public void driverEditorRemoved(IPath path, boolean replicated);

	/**
	 * Is fired when the driver editor is saved.
	 * 
	 * @param path
	 *            the path to the resource that the driver was editting.
	 * @param replicated
	 *            <code>false</code> if this action originates on this client.
	 *            <code>false</code> if it is an replication of an action from
	 *            another participant of the shared project.
	 */
	public void driverEditorSaved(IPath path, boolean replicated);

	/**
	 * Is fired when the follow mode is changed.
	 * 
	 * @param enabled
	 *            <code>true</code> if follow mode is enabled.
	 *            <code>false</code> otherwise.
	 */
	public void followModeChanged(boolean enabled);
}
