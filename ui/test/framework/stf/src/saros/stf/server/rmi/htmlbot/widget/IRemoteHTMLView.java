package saros.stf.server.rmi.htmlbot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a conceptual view of the Saros HTML GUI, regardless of whether it is realized as an
 * HTML overlay (e.g. simple forms), or in a separate browser (e.g. complex forms and wizards).
 * Allows access to its elements, such as {@linkplain #button(String) buttons}.
 */
public interface IRemoteHTMLView extends Remote {
  /**
   * Checks whether this view is currently open and visible on the remote side.
   *
   * @return <code>true</code> if the view is open and visible, <code>false</code> if not
   * @throws RemoteException if the openness could not be determined
   */
  boolean isOpen() throws RemoteException;

  /**
   * Opens this View on the remote side
   *
   * @throws RemoteException if the view could not be opened
   */
  void open() throws RemoteException;

  /**
   * Checks whether this view contains a button with the given ID.
   *
   * @param id the value of the ID attribute of the button
   * @return <code>true</code> if a button with the given id exists, <code>false</code> if not
   * @throws RemoteException if the presence could not be determined
   */
  boolean hasElementWithId(String id) throws RemoteException;

  /**
   * Checks whether this view contains a element with the given name.
   *
   * @param name the value of the name attribute of the element
   * @return <code>true</code> if a element with the given name exists, <code>false</code> if not
   * @throws RemoteException if the presence could not be determined
   */
  boolean hasElementWithName(String name) throws RemoteException;

  /**
   * Gets a remote representation of the HTML button with the given ID from within this view.
   *
   * @param id the value of the ID attribute of the button
   * @return an instance of {@link IRemoteHTMLButton}, if such a button exists in this view
   * @throws RemoteException e.g. if no such button exist in this view
   */
  IRemoteHTMLButton button(String id) throws RemoteException;

  /**
   * Gets a remote representation of the contact-list item with the given JID from within this view.
   * The item is used as a button.
   *
   * @param jid the value of the JID of the contact
   * @return an instance of {@link IRemoteHTMLButton}, if such a item exists in this view
   * @throws RemoteException e.g. if no such item exist in this view
   */
  IRemoteHTMLButton contactListItem(String jid) throws RemoteException;

  /**
   * Gets a remote representation of the HTML input field with the given name from within this view.
   *
   * @param name the value of the name attribute of the input field
   * @return an instance of {@link IRemoteHTMLInputField}, if such a input field exists in this view
   * @throws RemoteException e.g. if no such input field exist in this view
   */
  IRemoteHTMLInputField inputField(String name) throws RemoteException;

  /**
   * Gets a remote representation of the HTML checkbox with the given name from within this view.
   *
   * @param name the value of the name attribute of the checkbox
   * @return an instance of {@link IRemoteHTMLCheckbox}, if such a checkbox exists in this view
   * @throws RemoteException e.g. if no such checkbox exist in this view
   */
  IRemoteHTMLCheckbox checkbox(String name) throws RemoteException;

  /**
   * Gets a remote representation of the HTML radio group with the given name from within this view.
   *
   * @param name the value of the name attribute of the radio group
   * @return an instance of {@link IRemoteHTMLRadioGroup}, if such a radio group exists in this view
   * @throws RemoteException e.g. if no such radio group exist in this view
   */
  IRemoteHTMLRadioGroup radioGroup(String name) throws RemoteException;

  /**
   * Gets a remote representation of the HTML select with the given name from within this view.
   *
   * @param name the value of the name attribute of the select
   * @return an instance of {@link IRemoteHTMLSelect}, if such a select exists in this view
   * @throws RemoteException e.g. if no such select exist in this view
   */
  IRemoteHTMLSelect select(String name) throws RemoteException;

  /**
   * Gets a remote representation of the HTML multi select with the given name from within this
   * view.
   *
   * @param name the value of the name attribute of the multi select
   * @return an instance of {@link IRemoteHTMLSelect}, if such a multi select exists in this view
   * @throws RemoteException e.g. if no such multi select exist in this view
   */
  IRemoteHTMLMultiSelect multiSelect(String name) throws RemoteException;

  /**
   * Gets a remote representation of the HTML progressbar with the given name from within this view.
   *
   * @param name the value of the name attribute of the progressbar
   * @return an instance of {@link IRemoteHTMLSelect}, if such a progressbar exists in this view
   * @throws RemoteException e.g. if no such progressbar exist in this view
   */
  IRemoteHTMLProgressBar progressBar(String name) throws RemoteException;

  /**
   * Gets a remote representation of the HTML Element (div, span, ... ) with the given id from
   * within this view.
   *
   * @param id the value of the id of the element
   * @return an instance of {@link IRemoteHTMLTextElement}, if such a element exists in this view
   * @throws RemoteException e.g. if no such element exist in this view
   */
  IRemoteHTMLTextElement textElement(String id) throws RemoteException;

  /**
   * Gets a remote representation of the React Tree Element with the given className from within
   * this view.
   *
   * @param className the value of the identifying class of the element
   * @return an instance of {@link IRemoteHTMLTree}, if such a element exists in this view
   * @throws RemoteException e.g. if no such element exist in this view
   */
  IRemoteHTMLTree tree(String className) throws RemoteException;
}
