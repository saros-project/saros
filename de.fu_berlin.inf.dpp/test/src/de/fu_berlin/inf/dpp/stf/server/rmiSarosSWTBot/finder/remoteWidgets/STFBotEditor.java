package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;

/**
 * This interface contains convenience API to perform actions in the editor
 * area, then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test. (How
 * to do it please look at the javadoc in class {@link TestPattern} or read the
 * user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
 * <li>
 * then you can use the object editor initialized in {@link Tester} to access
 * the API :), e.g.
 * 
 * <pre>
 * alice.editor.isEditorOpen(&quot;MyClass.java&quot;);
 * </pre>
 * 
 * </li>
 * 
 * @author Lin
 */
public interface STFBotEditor extends Remote {

    /**********************************************
     * 
     * open/activate/close a editor
     * 
     **********************************************/

    /**
     * activate the editor specified with the given fileName
     * 
     * @throws RemoteException
     */
    public void show() throws RemoteException;

    public void setFocus() throws RemoteException;

    /**
     * waits until the editor specified with the given fileName is active
     * 
     * 
     * @throws RemoteException
     */
    public void waitUntilIsActive() throws RemoteException;

    /**
     * Saves and closes the given editor.
     * 
     * 
     * @throws RemoteException
     */
    public void closeWithSave() throws RemoteException;

    public void save() throws RemoteException;

    /**
     * close the editor without saving it. The editor must belong to this
     * workbench page.
     * <p>
     * Any unsaved changes are discard, if the editor has unsaved content.
     * </p>
     * 
     * @throws RemoteException
     */
    public void closeWithoutSave() throws RemoteException;

    /**********************************************
     * 
     * get content infos of a editor
     * 
     **********************************************/
    /**
     * 
     * 
     * @return the content of the editor specified with the last element of the
     *         given array, which my be dirty.
     * @throws RemoteException
     */
    public String getText() throws RemoteException;

    public String getTextOnCurrentLine() throws RemoteException;

    public String getTextOnLine(int line) throws RemoteException;

    public int getCursorLine() throws RemoteException;

    public int getCursorColumn() throws RemoteException;

    /**
     * 
     * @param line
     *            the line number, 0 based.
     * @return the color of the background on the specified line.
     * @throws RemoteException
     */
    public RGB getLineBackground(int line) throws RemoteException;

    public boolean isActive() throws RemoteException;

    /**********************************************
     * 
     * set contents of a editor
     * 
     **********************************************/
    /**
     * 
     * @param contentPath
     *            the path to the test file whose content should be inserted in
     *            the text editor. All such test files are located in the
     *            directory [Saros]/test/STF.
     * 
     * @throws RemoteException
     */
    public void setTexWithSave(String contentPath) throws RemoteException;

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes. So it will
     * be a good idea that you give bob some time before you compare the two
     * files between Alice and Bob.
     * 
     * 
     * @param otherClassContent
     *            the content of another class, to which you want to compare.
     */
    public void waitUntilIsTextSame(String otherClassContent)
        throws RemoteException;

    /**
     * Opens the specified file in an editor, set the given contents to the
     * editor without saving.
     * 
     * @param contentPath
     *            the path to the test file whose content should be set in the
     *            text editor. All such test files are located in the directory
     *            [Saros]/test/STF.
     * 
     * @throws RemoteException
     */
    public void setTextWithoutSave(String contentPath) throws RemoteException;

    /**
     * 
     * TODO: This function doesn't work exactly. It would be happen that the
     * text isn't typed in the right editor, When your saros-instances are fresh
     * started.
     * 
     * @param text
     *            the text to type.
     * 
     * @throws RemoteException
     */
    public void typeText(String text) throws RemoteException;

    /**
     * <p>
     * <b>Note:</b> if the class file isn't open, it will be opened first using
     * the given parameter "idOfEditor".
     * </p>
     * 
     * @return <code>true</code> if the contents have been modified and need
     *         saving, and <code>false</code> if they have not changed since the
     *         last save.
     */
    public boolean isDirty() throws RemoteException;

    /**********************************************
     * 
     * infos about debug
     * 
     **********************************************/

    /**
     * Changes the cursor position in editor.
     * 
     * 
     * @param line
     *            the line number, 0 based.
     * @param column
     *            the column number, 0 based.
     * @see SWTBotStyledText#navigateTo(int, int)
     * @throws RemoteException
     */
    public void navigateTo(int line, int column) throws RemoteException;

    /**
     * Presses the shortcut specified by the given keys.
     * 
     * 
     * @param keys
     *            the formal representation for key strokes
     * @throws RemoteException
     * @see IKeyLookup
     * @see SWTBotEclipseEditor#pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke...)
     */
    public void pressShortcut(String... keys) throws RemoteException;

    /**
     * 
     * 
     * @param insertText
     * @param proposalText
     * @throws RemoteException
     * @see SWTBotEclipseEditor#autoCompleteProposal(String, String)
     */
    public void autoCompleteProposal(String insertText, String proposalText)
        throws RemoteException;

    public List<String> getAutoCompleteProposals(String insertText)
        throws RemoteException;

    public void quickfix(String quickFixName) throws RemoteException;

    public void quickfix(int index) throws RemoteException;

    public void selectCurrentLine() throws RemoteException;

    public void selectLine(int line) throws RemoteException;

    public void selectRange(int line, int column, int length)
        throws RemoteException;

    public String getSelection() throws RemoteException;

    public void pressShortCutDelete() throws RemoteException;

    public void pressShortCutEnter() throws RemoteException;

    public void pressShortCutSave() throws RemoteException;

    public void pressShortRunAsJavaApplication() throws RemoteException;

    public void pressShortCutNextAnnotation() throws RemoteException;

    public void pressShortCutQuickAssignToLocalVariable()
        throws RemoteException;

    public void pressShortCut(int modificationKeys, char c)
        throws RemoteException;
}
