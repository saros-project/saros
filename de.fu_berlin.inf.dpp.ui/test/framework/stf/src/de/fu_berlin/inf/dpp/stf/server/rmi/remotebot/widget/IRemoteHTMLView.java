package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a conceptual view of the Saros HTML GUI, regardless of whether it
 * is realized as an HTML overlay (e.g. simple forms), or in a separate browser
 * (e.g. complex forms and wizards). Allows access to its elements, such as
 * {@linkplain #button(String) buttons}.
 */
public interface IRemoteHTMLView extends Remote {
    /**
     * Each of the conceptual views of the Saros GUI is represented by a
     * {@link View} value, i.e. to get access to an {@link IRemoteHTMLView}
     * instance callers must go through by {@link View} value. It's the duty of
     * the implementors of {@link IRemoteHTMLView} to map the abstract names to
     * their technical counterpart.
     */
    public enum View {
        /**
         * The permanently accessible view of Saros, which provides access to
         * most of its features.
         */
        MAIN_VIEW,
        /**
         * The form to add a new contact with
         */
        ADD_CONTACT;
    }

    /**
     * Checks whether this view is currently open and visible on the remote
     * side.
     * 
     * @return <code>true</code> if the view is open and visible,
     *         <code>false</code> if not
     * @throws RemoteException
     *             if the openness could not be determined
     */
    boolean isOpen() throws RemoteException;

    /**
     * Gets a remote representation of the HTML button with the given ID from
     * within this view.
     * 
     * @param id
     *            the value of the ID attribute of the button
     * 
     * @return an instance of {@link IRemoteHTMLButton}, if such a button exists
     *         in this view
     * 
     * @throws RemoteException
     *             e.g. if no such button exist in this view
     */
    IRemoteHTMLButton button(String id) throws RemoteException;

    /**
     * Checks whether this view contains a button with the given ID.
     * 
     * @param id
     *            the value of the ID attribute of the button
     * @return <code>true</code> if a button with the given id exists,
     *         <code>false</code> if not
     * @throws RemoteException
     *             if the presence could not be determined
     */
    boolean hasButton(String id) throws RemoteException;

    /**
     * Gets a remote representation of the HTML input field with the given name
     * from within this view.
     * 
     * @param name
     *            the value of the name attribute of the input field
     * 
     * @return an instance of {@link IRemoteHTMLInputField}, if such a input
     *         field exists in this view
     * 
     * @throws RemoteException
     *             e.g. if no such input field exist in this view
     */
    IRemoteHTMLInputField inputField(String name) throws RemoteException;

    /**
     * Checks whether this view contains a input field with the given name.
     * 
     * @param name
     *            the value of the name attribute of the input field
     * @return <code>true</code> if a input field with the given name exists,
     *         <code>false</code> if not
     * @throws RemoteException
     *             if the presence could not be determined
     */
    boolean hasInputField(String name) throws RemoteException;
}
