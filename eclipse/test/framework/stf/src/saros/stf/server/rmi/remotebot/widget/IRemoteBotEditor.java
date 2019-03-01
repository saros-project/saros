package saros.stf.server.rmi.remotebot.widget;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import saros.stf.client.tester.AbstractTester;

/**
 * This interface contains convenience API to perform actions in the editor area, then you can start
 * off as follows:
 *
 * <ol>
 *   <li>At first you need to create a {@link AbstractTester} object in your junit-test. (How to do
 *       it please read the user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).
 *   <li>then you can use the object editor initialized in {@link AbstractTester} to access the API
 *       :), e.g.
 *       <pre>
 * alice.editor.isEditorOpen(&quot;MyClass.java&quot;);
 * </pre>
 *
 * @author Lin
 */
public interface IRemoteBotEditor extends Remote {

  /**
   * ********************************************
   *
   * <p>Actions
   *
   * <p>********************************************
   */

  /** @see SWTBotEditor#show() */
  public void show() throws RemoteException;

  /** @see SWTBotEditor#setFocus() */
  public void setFocus() throws RemoteException;

  /**
   * Saves and closes the given editor.
   *
   * @throws RemoteException
   */
  public void closeWithSave() throws RemoteException;

  /** @see SWTBotEditor#save() */
  public void save() throws RemoteException;

  /**
   * close the editor without saving it. The editor must belong to this workbench page.
   *
   * <p>Any unsaved changes are discard, if the editor has unsaved content.
   *
   * @throws RemoteException
   */
  public void closeWithoutSave() throws RemoteException;

  /**
   * sets the editor content to the content of string
   *
   * @param text the text to set
   * @throws RemoteException
   */
  public void setText(String text) throws RemoteException;

  /**
   * sets the editor content to the content of the file
   *
   * @param path the path to the test file whose content should be inserted in the text editor. All
   *     such test files are located in the directory [Saros]/test/STF.
   * @throws RemoteException
   */
  public void setTextFromFile(String path) throws RemoteException;

  /**
   * TODO: This function doesn't work exactly. It may be happen that the text isn't typed in the
   * right editor, When your saros-instances are fresh started.
   *
   * @param text the text to type.
   * @throws RemoteException
   */
  public void typeText(String text) throws RemoteException;

  /**
   * Changes the cursor position in editor.
   *
   * @param line the line number, 0 based.
   * @param column the column number, 0 based.
   * @see SWTBotStyledText#navigateTo(int, int)
   * @throws RemoteException
   */
  public void navigateTo(int line, int column) throws RemoteException;

  /**
   * Presses the shortcut specified by the given keys.
   *
   * @param keys the formal representation for key strokes
   * @throws RemoteException
   * @see IKeyLookup
   * @see SWTBotEclipseEditor#pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke...)
   */
  public void pressShortcut(String... keys) throws RemoteException;

  /** @see SWTBotEclipseEditor#autoCompleteProposal(String, String) */
  public void autoCompleteProposal(String insertText, String proposalText) throws RemoteException;

  /** @see SWTBotEclipseEditor#quickfix(String) */
  public void quickfix(String quickFixName) throws RemoteException;

  /** @see SWTBotEclipseEditor#quickfix(int) */
  public void quickfix(int index) throws RemoteException;

  /** @see SWTBotEclipseEditor#selectCurrentLine() */
  public void selectCurrentLine() throws RemoteException;

  /** @see SWTBotEclipseEditor#selectLine(int) */
  public void selectLine(int line) throws RemoteException;

  /** @see SWTBotEclipseEditor#selectRange(int, int, int) */
  public void selectRange(int line, int column, int length) throws RemoteException;

  /**
   * press short cut "Delete"
   *
   * @see SWTBotEclipseEditor#pressShortcut(int, char)
   */
  public void pressShortCutDelete() throws RemoteException;

  /**
   * press short cut "Enter"
   *
   * @see SWTBotEclipseEditor#pressShortcut(int, char)
   */
  public void pressShortCutEnter() throws RemoteException;

  /**
   * press short cut "Save"
   *
   * @see SWTBotEclipseEditor#pressShortcut(int, char)
   */
  public void pressShortCutSave() throws RemoteException;

  /**
   * press short cut "Run as java application"
   *
   * @see SWTBotEclipseEditor#pressShortcut(int, char)
   */
  public void pressShortRunAsJavaApplication() throws RemoteException;

  /**
   * press short cut "Next annotation"
   *
   * @see SWTBotEclipseEditor#pressShortcut(int, char)
   */
  public void pressShortCutNextAnnotation() throws RemoteException;

  /**
   * press short cut "Assign to local variable"
   *
   * @see SWTBotEclipseEditor#pressShortcut(int, char)
   */
  public void pressShortCutQuickAssignToLocalVariable() throws RemoteException;

  /** @see SWTBotEclipseEditor#pressShortcut(int, char) */
  public void pressShortCut(int modificationKeys, char c) throws RemoteException;

  /**
   * ********************************************
   *
   * <p>States
   *
   * <p>********************************************
   */

  /**
   * @return the content of the editor specified with the last element of the given array, which my
   *     be dirty.
   * @throws RemoteException
   */
  public String getText() throws RemoteException;

  /** @see SWTBotEclipseEditor#getLineCount() */
  public int getLineCount() throws RemoteException;

  /** @see SWTBotEclipseEditor#getLines() */
  public List<String> getLines() throws RemoteException;

  /** @see SWTBotEclipseEditor#getTextOnCurrentLine() */
  public String getTextOnCurrentLine() throws RemoteException;

  /** @see SWTBotEclipseEditor#getTextOnLine(int) */
  public String getTextOnLine(int line) throws RemoteException;

  /** @see SWTBotEclipseEditor#cursorPosition */
  public int getCursorLine() throws RemoteException;

  /** @see SWTBotEclipseEditor#cursorPosition */
  public int getCursorColumn() throws RemoteException;

  /** @see SWTBotEclipseEditor#getLineBackground(int) */
  public RGB getLineBackground(int line) throws RemoteException;

  /** @see SWTBotEclipseEditor#isActive() */
  public boolean isActive() throws RemoteException;

  /**
   * @return <code>true</code> if the contents have been modified and need saving, and <code>false
   *     </code> if they have not changed since the last save.
   */
  public boolean isDirty() throws RemoteException;

  public List<String> getAutoCompleteProposals(String insertText) throws RemoteException;

  /**
   * Returns the current selected text
   *
   * @return the current selected text
   * @throws RemoteException
   */
  public String getSelection() throws RemoteException;

  /**
   * Returns the current selected text which is marked by an annotation. Use this method to retrieve
   * the selected text in follow mode.
   *
   * @return the current selected text marked by an annotation
   * @throws RemoteException
   */
  public String getSelectionByAnnotation() throws RemoteException;

  /**
   * Returns the current view port for the editor
   *
   * @return the current view port where the first entry in the list is the starting line and the
   *     second entry is the number of lines
   * @throws RemoteException
   */
  public List<Integer> getViewport() throws RemoteException;

  /**
   * ********************************************
   *
   * <p>States
   *
   * <p>********************************************
   */

  /**
   * waits until the editor specified with the given fileName is active
   *
   * @throws RemoteException
   */
  public void waitUntilIsActive() throws RemoteException;

  /**
   * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of file, which is modified
   * by another peer (e.g. Alice). Because of data transfer delay Bob need to wait a minute to see
   * the changes. So it will be a good idea that you give bob some time before you compare the two
   * files between Alice and Bob.
   *
   * @param otherClassContent the content of another class, to which you want to compare.
   */
  public void waitUntilIsTextSame(String otherClassContent) throws RemoteException;
}
