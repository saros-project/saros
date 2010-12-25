package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
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
public interface EditorComponent extends Remote {

    /**********************************************
     * 
     * open/activate/close a editor
     * 
     **********************************************/

    /**
     * 
     * @param fileName
     *            the filename on the editor tab, e.g. myFile.xml or
     *            MyClass.java
     * @return <tt>true</tt>, if the editor specified with the given fileName is
     *         open
     * @throws RemoteException
     */
    public boolean isEditorOpen(String fileName) throws RemoteException;

    /**
     * waits until the editor specified with the given fileName is open
     * 
     * @param fileName
     *            the filename on the editor tab, e.g. myFile.xml or
     *            MyClass.java
     * @throws RemoteException
     */
    public void waitUntilEditorOpen(String fileName) throws RemoteException;

    /**
     * This method is special for java file, you need to only pass the name of
     * the java file without suffix ".java".
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @return<tt>true</tt>, if the java editor specified with the given
     *                       className is open
     * @throws RemoteException
     */
    public boolean isJavaEditorOpen(String className) throws RemoteException;

    /**
     * waits until the java editor specified with the given className is open
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @throws RemoteException
     */
    public void waitUntilJavaEditorOpen(String className)
        throws RemoteException;

    /**
     * activate the editor specified with the given fileName
     * 
     * @param fileName
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void activateEditor(String fileName) throws RemoteException;

    /**
     * waits until the editor specified with the given fileName is active
     * 
     * @param fileName
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void waitUntilEditorActive(String fileName) throws RemoteException;

    /**
     * activate the java editor specified with the given className
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @throws RemoteException
     */
    public void activateJavaEditor(String className) throws RemoteException;

    /**
     * waits until the java editor specified with the given className is active
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @throws RemoteException
     */
    public void waitUntilJavaEditorActive(String className)
        throws RemoteException;

    /**
     * 
     * @param fileName
     *            the filename on the editor tab
     * @return <tt>true</tt>, if the editor specified with the given fileName is
     *         active
     * @throws RemoteException
     */
    public boolean isEditorActive(String fileName) throws RemoteException;

    /**
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @return<tt>true</tt>, if the java editor specified with the given
     *                       className is active
     * @throws RemoteException
     */
    public boolean isJavaEditorActive(String className) throws RemoteException;

    /**
     * Saves and closes the given editor.
     * 
     * @param fileName
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void closeEditorWithSave(String fileName) throws RemoteException;

    /**
     * close the editor without saving it. The editor must belong to this
     * workbench page.
     * <p>
     * Any unsaved changes are discard, if the editor has unsaved content.
     * </p>
     * 
     * @param fileName
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void closeEditorWithoutSave(String fileName) throws RemoteException;

    /**
     * waits until the editor specified with the given fileName is closed
     * 
     * @param fileName
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void waitUntilEditorClosed(String fileName) throws RemoteException;

    /**
     * Save the java editor and close it.
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @throws RemoteException
     */
    public void closeJavaEditorWithSave(String className)
        throws RemoteException;

    /**
     * close the java editor without saving it.
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @throws RemoteException
     */
    public void closejavaEditorWithoutSave(String className)
        throws RemoteException;

    /**
     * waits until the java editor specified with the given className is closed
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @throws RemoteException
     */
    public void waitUntilJavaEditorClosed(String className)
        throws RemoteException;

    /**
     * if you try to close a editor which is dirty, you will get this popup
     * window.
     * 
     * @param buttonType
     *            YES or NO
     * @throws RemoteException
     */
    public void confirmWindowSaveSource(String buttonType)
        throws RemoteException;

    /**********************************************
     * 
     * get content infos of a editor
     * 
     **********************************************/
    /**
     * 
     * @param filenodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     * @return the content of the editor specified with the last element of the
     *         given array, which my be dirty.
     * @throws RemoteException
     */
    public String getTextOfEditor(String... filenodes) throws RemoteException;

    /**
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param packageName
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     * @return the content of the editor specified with the given className,
     *         which may be dirty.
     * @throws RemoteException
     */
    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException;

    public String getTextOnCurrentLine(String fileName) throws RemoteException;

    public String getTextOnLine(String fileName, int line)
        throws RemoteException;

    /**
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param packageName
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     * @param line
     *            the line number, 0 based.
     * @return the text on the given line number, without the line delimiters.
     * @throws RemoteException
     */
    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException;

    public int getCursorLine(String fileName) throws RemoteException;

    public int getCursorColumn(String fileName) throws RemoteException;

    /**
     * 
     * @param className
     *            the filename on the editor tab
     * @return the line number of the current position of the cursor
     * @throws RemoteException
     */
    public int getJavaCursorLinePosition(String className)
        throws RemoteException;

    /**
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @param line
     *            the line number, 0 based.
     * @return the color of the background on the specified line.
     * @throws RemoteException
     */
    public RGB getJavaLineBackground(String className, int line)
        throws RemoteException;

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link EditorComponent#waitUntilEditorContentSame(String, String...)},
     * which compare the contents which may be dirty.
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
     * @param otherClassContent
     *            the class content of another peer, with which you want to
     *            compare your class content.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     * 
     * @throws RemoteException
     * @see EditorComponent#waitUntilFileContentSame(String, String...)
     */
    public void waitUntilClassContentsSame(String projectName, String pkg,
        String className, String otherClassContent) throws RemoteException;

    /**
     * 
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     * @return only the saved content of the specified file, if it is dirty.
     *         This method is different from
     *         {@link EditorComponent#getTextOfEditor(String...)}, which can
     *         return a not saved content.
     * @throws RemoteException
     * @throws IOException
     * @throws CoreException
     */
    public String getFileContent(String... fileNodes) throws RemoteException,
        IOException, CoreException;

    /**
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     * @return only the saved content of the specified class file, if it is
     *         dirty. This method is different from
     *         {@link EditorComponent#getTextOfJavaEditor(String, String, String)}
     *         , which can return a not saved content.
     * @throws RemoteException
     * @throws IOException
     * @throws CoreException
     */
    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException;

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
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     * @throws RemoteException
     */
    public void setTextInEditorWithSave(String contentPath, String... fileNodes)
        throws RemoteException;

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes. So it will
     * be a good idea that you give bob some time before you compare the two
     * files between Alice and Bob.
     * <p>
     * <b>Note:</b> the method is different from
     * {@link EditorComponent#waitUntilClassContentsSame(String, String, String, String)}
     * , which compare the contents of the class files which isn't dirty.
     * </p>
     * 
     * @param otherClassContent
     *            the content of another class, to which you want to compare.
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     */
    public void waitUntilEditorContentSame(String otherClassContent,
        String... fileNodes) throws RemoteException;

    /**
     * Opens the specified class in an editor, set the given contents to the
     * editor and saves it.
     * 
     * @param contentPath
     *            the path to the test file whose content should be set in the
     *            text editor. All such test files are located in the directory
     *            [Saros]/test/STF.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param packageName
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     * @throws RemoteException
     */
    public void setTextInJavaEditorWithSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException;

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes. So it will
     * be a good idea that you give bob some time before you compare the two
     * class files between Alice and Bob.
     * <p>
     * <b>Note:</b> the method is different from
     * {@link EditorComponent#waitUntilClassContentsSame(String, String, String, String)}
     * , which compare the contents of the class files which isn't dirty.
     * </p>
     * 
     * @param otherClassContent
     *            the class content of another peer, to which you want to
     *            compare.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     */
    public void waitUntilJavaEditorContentSame(String otherClassContent,
        String projectName, String pkg, String className)
        throws RemoteException;

    /**
     * Opens the specified file in an editor, set the given contents to the
     * editor without saving.
     * 
     * @param contentPath
     *            the path to the test file whose content should be set in the
     *            text editor. All such test files are located in the directory
     *            [Saros]/test/STF.
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     * @throws RemoteException
     */
    public void setTextInEditorWithoutSave(String contentPath,
        String... fileNodes) throws RemoteException;

    /**
     * 
     * @param contentPath
     *            the path to the test file whose content should be set in the
     *            text editor. All such test files are located in the directory
     *            [Saros]/test/STF.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     * @throws RemoteException
     */
    public void setTextInJavaEditorWithoutSave(String contentPath,
        String projectName, String pkg, String className)
        throws RemoteException;

    /**
     * 
     * TODO: This function doesn't work exactly. It would be happen that the
     * text isn't typed in the right editor, When your saros-instances are fresh
     * started.
     * 
     * @param text
     *            the text to type.
     * @param fileNodes
     *            node path to expand. Attempts to expand all nodes along the
     *            path specified by the node array parameter.e.g.
     *            {"Foo-saros","parentFolder" ,"myFolder"}.
     * @throws RemoteException
     */
    public void typeTextInEditor(String text, String... fileNodes)
        throws RemoteException;

    /**
     * 
     * @param text
     *            the text to type.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     * @throws RemoteException
     * @see EditorComponent#typeTextInEditor(String, String...)
     */
    public void typeTextInJavaEditor(String text, String projectName,
        String pkg, String className) throws RemoteException;

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
    public boolean isFileDirty(String... fileNodes) throws RemoteException;

    /**
     * <p>
     * <b>Note:</b> if the class file isn't open, it will be opened first using
     * the given parameter "idOfEditor".
     * </p>
     * 
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     * @return <code>true</code> if the contents have been modified and need
     *         saving, and <code>false</code> if they have not changed since the
     *         last save
     */
    public boolean isClassDirty(String projectName, String pkg,
        String className, final String idOfEditor) throws RemoteException;

    /**********************************************
     * 
     * infos about debug
     * 
     **********************************************/
    /**
     * set break point.
     * 
     * @param line
     *            the line number, 0 based.
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param pkg
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class without suffix, e.g. MyClass
     * @throws RemoteException
     */
    public void setBreakPoint(int line, String projectName, String pkg,
        String className) throws RemoteException;

    /**
     * Changes the cursor position in editor.
     * 
     * @param fileName
     *            name of the file with suffix, e.g. myFile.xml
     * @param line
     *            the line number, 0 based.
     * @param column
     *            the column number, 0 based.
     * @see SWTBotStyledText#navigateTo(int, int)
     * @throws RemoteException
     */
    public void navigateInEditor(String fileName, int line, int column)
        throws RemoteException;

    /**
     * Presses the shortcut specified by the given keys.
     * 
     * @param fileName
     *            name of the file with suffix, e.g. myFile.xml
     * @param keys
     *            the formal representation for key strokes
     * @throws RemoteException
     * @see IKeyLookup
     * @see SWTBotEclipseEditor#pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke...)
     */
    public void pressShortcut(String fileName, String... keys)
        throws RemoteException;

    /**
     * 
     * @param fileName
     * @param insertText
     * @param proposalText
     * @throws RemoteException
     * @see SWTBotEclipseEditor#autoCompleteProposal(String, String)
     */
    public void autoCompleteProposal(String fileName, String insertText,
        String proposalText) throws RemoteException;

    public List<String> getAutoCompleteProposals(String fileName,
        String insertText) throws RemoteException;

    public void quickfix(String fileName, String quickFixName)
        throws RemoteException;

    public void quickfix(String fileName, int index) throws RemoteException;

    public void selectCurrentLine(String fileName) throws RemoteException;

    public void selectLine(String fileName, int line) throws RemoteException;

    public void selectRange(String fileName, int line, int column, int length)
        throws RemoteException;

    public String getSelection(String fileName) throws RemoteException;

    /**
     * 
     * @param fileName
     * @throws RemoteException
     */
    public void pressShortCutDelete(String fileName) throws RemoteException;

    public void pressShortCutEnter(String fileName) throws RemoteException;

    public void pressShortCutSave(String fileName) throws RemoteException;

    public void pressShortRunAsJavaApplication(String fileName)
        throws RemoteException;

    public void pressShortCutNextAnnotation(String fileName)
        throws RemoteException;

    public void pressShortCutQuickAssignToLocalVariable(String fileName)
        throws RemoteException;

    public void pressShortCut(String fileName, int modificationKeys, char c)
        throws RemoteException;
}
