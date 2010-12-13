package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

import de.fu_berlin.inf.dpp.stf.client.Tester;
import de.fu_berlin.inf.dpp.stf.client.testProject.helpers.TestPattern;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noGUI.StateImp;

/**
 * This interface contains convenience API to perform actions in the editor
 * area, then you can start off as follows:
 * <ol>
 * <li>
 * At first you need to create a {@link Tester} object in your junit-test.
 * (How to do it please look at the javadoc in class {@link TestPattern} or read
 * the user guide in TWiki https://www.inf.fu-berlin.de/w/SE/SarosSTFTests).</li>
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
     *            the filename on the editor tab
     * @return <tt>true</tt>, if the editor specified with the given fileName is
     *         open
     * @throws RemoteException
     */
    public boolean isEditorOpen(String fileName) throws RemoteException;

    /**
     * waits until the editor specified with the given fileName is open
     * 
     * @param fileName
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void waitUntilEditorOpen(String fileName) throws RemoteException;

    /**
     * 
     * @param className
     *            the filename on the editor tab
     * @return<tt>true</tt>, if the java editor specified with the given
     *                       className is open
     * @throws RemoteException
     */
    public boolean isJavaEditorOpen(String className) throws RemoteException;

    /**
     * waits until the java editor specified with the given className is open
     * 
     * @param className
     *            the filename on the editor tab
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
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void activateJavaEditor(String className) throws RemoteException;

    /**
     * waits until the java editor specified with the given className is active
     * 
     * @param className
     *            the filename on the editor tab
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
     *            the filename on the editor tab
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
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void closeJavaEditorWithSave(String className)
        throws RemoteException;

    /**
     * close the java editor without saving it.
     * 
     * @param className
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void closejavaEditorWithoutSave(String className)
        throws RemoteException;

    /**
     * waits until the java editor specified with the given className is closed
     * 
     * @param className
     *            the filename on the editor tab
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

    /**
     * @param projectName
     *            name of the java project, e.g. Foo_Saros.
     * @param packageName
     *            name of the package, e.g. my.pkg
     * @param className
     *            name of the class, e.g. MyClass
     * @param line
     *            the line number, 0 based.
     * @return the text on the given line number, without the line delimiters.
     * @throws RemoteException
     */
    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException;

    /**
     * 
     * @param line
     *            the line number, 0 based.
     * @param fileName
     *            the filename on the editor tab
     * @throws RemoteException
     * @see SWTBotStyledText#selectLine(int)
     */
    public void selectLineInEditor(int line, String fileName)
        throws RemoteException;

    /**
     * 
     * @param line
     *            the line number, 0 based.
     * @param className
     *            the filename on the editor tab
     * @throws RemoteException
     */
    public void selectLineInJavaEditor(int line, String className)
        throws RemoteException;

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
     *            the filename on the editor tab
     * @param line
     *            the line number, 0 based.
     * @return the color of the background on the specified line.
     * @throws RemoteException
     */
    public RGB getJavaLineBackground(String className, int line)
        throws RemoteException;

    /**
     * 
     * @param fileName
     *            the filename on the editor tab
     * @return an editor specified by the given fileName which provides methods
     *         for text editors.
     * @throws RemoteException
     */
    public SWTBotEclipseEditor getEditor(String fileName)
        throws RemoteException;

    /**
     * 
     * @param className
     *            the filename on the editor tab
     * @return an editor specified by the given className which provides methods
     *         for text editors.
     * @throws RemoteException
     */
    public SWTBotEclipseEditor getJavaEditor(String className)
        throws RemoteException;

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
     * {@link StateImp#waitUntilClassContentsSame(String, String, String, String)}
     * , which compare the contents of the class files which isn't dirty.
     * </p>
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
     *            name of the class, e.g. MyClass
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
     * {@link StateImp#waitUntilClassContentsSame(String, String, String, String)}
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
     *            name of the class, e.g. MyClass
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
     *            name of the class, e.g. MyClass
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
     *            name of the class, e.g. MyClass
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
     *            name of the class, e.g. MyClass
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
     *            name of the class, e.g. MyClass
     * @throws RemoteException
     */
    public void setBreakPoint(int line, String projectName, String pkg,
        String className) throws RemoteException;

}
