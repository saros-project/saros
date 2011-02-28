package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.noFinder;

import java.io.IOException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotEditor;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponent;

public interface NoBot extends EclipseComponent {

    /**
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     * @return only the saved content of the specified file, if it is dirty.
     *         This method is different from
     *         {@link STFBotEditor#getTextOfEditor(String...)}, which can return
     *         a not saved content.
     * @throws RemoteException
     * @throws IOException
     * @throws CoreException
     */
    public String getFileContent(String... fileNodes) throws RemoteException,
        IOException, CoreException;

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link STFBotEditor#waitUntilEditorContentSame(String, String...)}, which
     * compare the contents which may be dirty.
     * </p>
     * 
     * @param otherFileContent
     *            the file content of another peer, with which you want to
     *            compare your file content.
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     */
    public void waitUntilFileContentSame(String otherFileContent,
        String... fileNodes) throws RemoteException;

    /**
     * 
     * change the account specified by the given jid with
     * {@link XMPPAccountStore#changeAccountData(int, String, String, String)}
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @param newUserName
     *            the new username
     * @param newPassword
     *            the new password
     * @param newServer
     *            the new server
     * @throws RemoteException
     */
    public void changeAccountNoGUI(JID jid, String newUserName,
        String newPassword, String newServer) throws RemoteException;

    /**
     * delete the account specified by the given jid with
     * {@link XMPPAccountStore#deleteAccount(XMPPAccount)}
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void deleteAccountNoGUI(JID jid) throws RemoteException;

    /**
     * activate the account specified by the given jid with
     * XMPPAccountStore#setAccountActive(XMPPAccount)
     * 
     * @param jid
     *            a JID which is used to identify the users of the Jabber
     *            network, more about it please see {@link JID}.
     * @throws RemoteException
     */
    public void activateAccountNoGUI(JID jid) throws RemoteException;

    /**
     * Creates an account with
     * {@link XMPPAccountStore#createNewAccount(String, String, String)}.
     * 
     * @param server
     *            the server of the new account.
     * @param username
     *            the username of the new account.
     * @param password
     *            the password of the new account.
     * 
     * 
     * @throws RemoteException
     */
    public void createAccountNoGUI(String server, String username,
        String password) throws RemoteException;

    /**
     * Set feeback disabled without GUI.<br/>
     * To simplify Testing you can disable the automatic reminder, so that you
     * will never get the feedback popup window.
     * 
     * @see FeedbackManager#setFeedbackDisabled(boolean)
     * 
     * @throws RemoteException
     */
    public void disableAutomaticReminderNoGUI() throws RemoteException;

    /**
     * Perform the action "switch to another Branch/Tag" which should be done
     * with the following steps:
     * 
     * <ol>
     * <li>Select the given project and click "Team" >
     * "Switch to another Branch/Tag/Revision..."</li>
     * <li>Enter the given URL to the combobox text field with the title
     * "To URL:"</li>
     * <li>click "OK" to confirm the switch</li>
     * <li>Waits until the shell "SVN Switch" is closed. It guarantee that the
     * "switch to another Branch/Tag/revision" action is completely done.</li>
     * </ol>
     * <p>
     * <b>Attention:</b>
     * <ol>
     * <li>Makes sure, the package explorer view is open and active.</li>
     * <li>The function should treat all the recursive following actions, which
     * are activated or indirectly activated by clicking the sub menu
     * "switch to another Branch/Tag" . I mean, after clicking the sub menu you
     * need to treat the following popup window too.</li>
     * 
     * 
     * @param projectName
     *            the name of the project located in the package explorer view,
     *            which you want to share with other peers.
     * @param url
     *            Update working copy to the url.
     * @throws RemoteException
     */
    public void switchProject(String projectName, String url)
        throws RemoteException;

    public void switchResource(String fullPath, String url, String revision)
        throws RemoteException;

    public void switchResource(String fullPath, String url)
        throws RemoteException;

    /**
     * Delete the project using FileUntil.delete(resource). This delete-method
     * costs less time than the method using GUI
     * 
     * @param projectName
     *            name of the project, which you want to delete.
     */
    public void deleteProjectNoGUI(String projectName) throws RemoteException;

    /**
     * Delete the specified folder using FileUntil.delete(resource).
     * 
     * @param folderNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}
     */
    public void deleteFolderNoGUI(String... folderNodes) throws RemoteException;

    /**
     * Delete the specified package using FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which package you want to delete.
     * @param pkg
     *            name of the package, which you want to delete.
     */
    public void deletePkgNoGUI(String projectName, String pkg)
        throws RemoteException;

    /**
     * Delete a class of the specified java project using
     * FileUntil.delete(resource).
     * 
     * @param projectName
     *            name of the project, which class you want to delete.
     * @param pkg
     *            name of the package, which class you want to delete.
     * @param className
     *            name of the class, which you want to delete.
     */
    public void deleteClassNoGUI(String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Delete all the projects in this workspace.
     * 
     * @throws RemoteException
     */
    public void deleteAllProjectsNoGUI() throws RemoteException;
}
