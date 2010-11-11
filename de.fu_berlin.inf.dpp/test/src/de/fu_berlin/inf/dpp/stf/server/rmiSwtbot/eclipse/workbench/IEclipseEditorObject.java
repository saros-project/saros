package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;

public interface IEclipseEditorObject extends Remote {

    /**
     * Sometimes you want to know, if a peer(e.g. Bob) can see the changes of
     * file, which is modified by another peer (e.g. Alice). Because of data
     * transfer delay Bob need to wait a minute to see the changes . So it will
     * be a good idea that you give bob some time before you compare the two
     * files from Alice and Bob.
     * 
     * <p>
     * <b>Note:</b> the mothod is different from
     * {@link #waitUntilClassContentsSame(String, String, String, String)}. this
     * method compare only the contents of the class files which may be not
     * saved.
     * </p>
     * * *
     */
    public void waitUntilEditorContentSame(String otherClassContent,
        String... filePath) throws RemoteException;

    public void waitUntilJavaEditorContentSame(String otherClassContent,
        String projectName, String pkg, String className)
        throws RemoteException;

    public void waitUntilJavaEditorActive(String className)
        throws RemoteException;

    public void waitUntilJavaEditorOpen(String className)
        throws RemoteException;

    public void waitUntilJavaEditorClosed(String className)
        throws RemoteException;

    public void waitUntilEditorClosed(String name) throws RemoteException;

    public void waitUntilEditorOpen(String name) throws RemoteException;

    public void waitUntilEditorActive(String name) throws RemoteException;

    public boolean isClassOpen(String className) throws RemoteException;

    public boolean isFileOpen(String fileName) throws RemoteException;

    public boolean isJavaEditorActive(String className) throws RemoteException;

    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException;

    public int getJavaCursorLinePosition(String projectName,
        String packageName, String className) throws RemoteException;

    public RGB getJavaLineBackground(String projectName, String packageName,
        String className, int line) throws RemoteException;

    public SWTBotEclipseEditor getJavaEditor(String className)
        throws RemoteException;

    public void activateJavaEditor(String className) throws RemoteException;

    public void activateEditor(String fileName) throws RemoteException;

    /**
     * get content of a class file, which may be not saved.
     */
    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException;

    public String getTextOfEditor(String... filepath) throws RemoteException;

    public void selectLineInJavaEditor(int line, String fileName)
        throws RemoteException;

    public void setBreakPoint(int line, String projectName, String packageName,
        String className) throws RemoteException;

    public void closeJavaEditorWithSave(String className)
        throws RemoteException;

    public void closejavaEditorWithoutSave(String className)
        throws RemoteException;

    /**
     * Returns whether the contents of this class file have changed since the
     * last save operation.
     * <p>
     * <b>Note:</b> if the class file isn't open, it will be opened first using
     * the defined editor (parameter: idOfEditor).
     * </p>
     * 
     * @return <code>true</code> if the contents have been modified and need
     *         saving, and <code>false</code> if they have not changed since the
     *         last save
     */
    public boolean isClassDirty(String projectName, String pkg,
        String className, final String idOfEditor) throws RemoteException;

    public void setTextInJavaEditorWithSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException;

    public void setTextInEditorWithSave(String contentPath, String... filePath)
        throws RemoteException;

    public void setTextInJavaEditorWithoutSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException;

    public void typeTextInJavaEditor(String contentPath, String projectName,
        String packageName, String className) throws RemoteException;
}
