package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class EditorComponenttImp extends EclipseComponent implements
    EditorComponent {
    // public static EclipseEditorObjectImp classVariable;

    private static transient EditorComponenttImp self;

    /**
     * {@link EditorComponenttImp} is a singleton, but inheritance is possible.
     */
    public static EditorComponenttImp getInstance() {
        if (self != null)
            return self;
        self = new EditorComponenttImp();
        return self;
    }

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
        String... filePath) throws RemoteException {
        waitUntil(SarosConditions.isEditorContentsSame(this, otherClassContent,
            filePath));
    }

    public void waitUntilJavaEditorContentSame(String otherClassContent,
        String projectName, String pkg, String className)
        throws RemoteException {
        waitUntil(SarosConditions.isEditorContentsSame(this, otherClassContent,
            projectName, "src", pkg, className + ".java"));
    }

    public void waitUntilJavaEditorActive(String className)
        throws RemoteException {
        waitUntilEditorActive(className + ".java");
    }

    public void waitUntilJavaEditorOpen(String className)
        throws RemoteException {
        waitUntilEditorOpen(className + ".java");
    }

    public void waitUntilJavaEditorClosed(String className)
        throws RemoteException {
        waitUntilEditorClosed(className + ".java");
    }

    public void waitUntilEditorClosed(String name) throws RemoteException {
        waitUntil(SarosConditions.isEditorClosed(this, name));
    }

    public void waitUntilEditorOpen(String name) throws RemoteException {
        waitUntil(SarosConditions.isEditorOpen(this, name));
    }

    public void waitUntilEditorActive(String name) throws RemoteException {
        waitUntil(SarosConditions.isEditorActive(editorPart, name));
    }

    public boolean isJavaEditorOpen(String javaEditorName)
        throws RemoteException {
        return editorPart.isEditorOpen(javaEditorName + ".java");
    }

    public boolean isFileOpen(String fileName) throws RemoteException {
        return editorPart.isEditorOpen(fileName);
    }

    public boolean isJavaEditorActive(String className) throws RemoteException {
        if (!isJavaEditorOpen(className))
            return false;
        return editorPart.isEditorActive(className + ".java");
    }

    public String getJavaTextOnLine(String projectName, String packageName,
        String className, int line) throws RemoteException {
        peVC.openFile(getClassNodes(projectName, packageName, className));
        activateJavaEditor(className);
        return getJavaEditor(className).getTextOnLine(line);
    }

    public int getJavaCursorLinePosition(String projectName,
        String packageName, String className) throws RemoteException {
        // openJavaFileWithEditor(projectName, packageName, className);
        activateJavaEditor(className);
        SWTBotEclipseEditor editor = getJavaEditor(className);
        log.info("cursorPosition: " + editor.cursorPosition().line);
        return editor.cursorPosition().line;
    }

    public RGB getJavaLineBackground(String projectName, String packageName,
        String className, int line) throws RemoteException {
        peVC.openFile(getClassNodes(projectName, packageName, className));
        activateJavaEditor(className);
        return getJavaEditor(className).getLineBackground(line);
    }

    public SWTBotEclipseEditor getJavaEditor(String className)
        throws RemoteException {
        return editorPart.getTextEditor(className + ".java");
    }

    public void activateJavaEditor(String className) throws RemoteException {
        editorPart.activateEditor(className + ".java");
    }

    public void activateEditor(String fileName) throws RemoteException {
        editorPart.activateEditor(fileName);
    }

    /**
     * get content of a class file, which may be not saved.
     */
    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException {
        String[] classNodes = getClassNodes(projectName, packageName, className);
        String[] regexNodes = helperPart.changeToRegex(classNodes);
        peVC.openFile(regexNodes);
        activateJavaEditor(className);
        return editorPart.getTextEditor(className + ".java").getText();
    }

    public String getTextOfEditor(String... filepath) throws RemoteException {
        peVC.openFile(filepath);
        String fileName = filepath[filepath.length - 1];
        activateEditor(fileName);
        return editorPart.getTextEditor(fileName).getText();
    }

    public void selectLineInJavaEditor(int line, String fileName)
        throws RemoteException {
        editorPart.selectLineInEditor(line, fileName + ".java");
    }

    public void setBreakPoint(int line, String projectName, String packageName,
        String className) throws RemoteException {
        peVC.openFile(getClassNodes(projectName, packageName, className));
        activateJavaEditor(className);
        selectLineInJavaEditor(line, className);
        menuPart.clickMenuWithTexts("Run", "Toggle Breakpoint");

    }

    // public void closeAllOpenedEditors() throws RemoteException {
    // delegate.closeAllEditors();
    // }

    public void closeJavaEditorWithSave(String className)
        throws RemoteException {
        activateJavaEditor(className);
        getJavaEditor(className).save();
        getJavaEditor(className).close();
        // Display.getDefault().syncExec(new Runnable() {
        // public void run() {
        // final IWorkbench wb = PlatformUI.getWorkbench();
        // final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        // IWorkbenchPage page = win.getActivePage();
        // if (page != null) {
        // page.closeEditor(page.getActiveEditor(), true);
        // Shell activateShell = Display.getCurrent().getActiveShell();
        // activateShell.close();
        //
        // }
        // }
        // });
    }

    public void closeEditorWithSave(String fileName) throws RemoteException {
        activateEditor(fileName);
        editorPart.getTextEditor(fileName).save();
        editorPart.getTextEditor(fileName).close();
    }

    public void closeEditorWithoutSave(String fileName) throws RemoteException {
        activateEditor(fileName);
        editorPart.getTextEditor(fileName).close();
        windowPart.confirmWindow("Save Resource", YES);
    }

    public void closejavaEditorWithoutSave(String className)
        throws RemoteException {
        activateJavaEditor(className);
        getJavaEditor(className).close();
        windowPart.confirmWindow("Save Resource", YES);
    }

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
        String className, final String idOfEditor) throws RemoteException {
        if (!editorC.isJavaEditorOpen(className)) {
            peVC.openClass(projectName, pkg, className);
        }
        return getJavaEditor(className).isDirty();
        // final List<Boolean> results = new ArrayList<Boolean>();
        // IPath path = new Path(getClassPath(projectName, pkg, className));
        // final IFile file = ResourcesPlugin.getWorkspace().getRoot()
        // .getFile(path);
        //
        // Display.getDefault().syncExec(new Runnable() {
        // public void run() {
        // final IWorkbench wb = PlatformUI.getWorkbench();
        // final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        //
        // IWorkbenchPage page = win.getActivePage();
        // if (page != null) {
        // IEditorInput editorInput = new FileEditorInput(file);
        // try {
        // page.openEditor(editorInput, idOfEditor);
        // } catch (PartInitException e) {
        // log.debug("", e);
        // }
        // results.add(page.findEditor(editorInput).isDirty());
        // }
        // }
        // });
        // return results.get(0);
    }

    public void setTextInJavaEditorWithSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException {
        String contents = state.getTestFileContents(contentPath);
        // activateEclipseShell();

        peVC.openFile(getClassNodes(projectName, packageName, className));
        activateJavaEditor(className);
        SWTBotEditor editor;
        editor = bot.editorByTitle(className + ".java");
        SWTBotEclipseEditor e = editor.toTextEditor();

        // Display.getDefault().syncExec(new Runnable() {
        // public void run() {
        // final IWorkbench wb = PlatformUI.getWorkbench();
        // final IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        // log.debug("shell name: " + win.getShell().getText());
        // win.getShell().forceActive();
        // win.getShell().forceFocus();
        // }
        // });
        // e.setFocus();
        e.setText(contents);
        // e.typeText("hallo wie geht es dir !%%%");
        // e.pressShortcut(Keystrokes.LF);
        // e.typeText("mir geht es gut!");
        // delegate.sleep(2000);
        //
        // delegate.sleep(2000);

        e.save();
        // editorObject.setTextinEditorWithSave(contents, className + ".java");
    }

    public void setTextInEditorWithSave(String contentPath, String... filePath)
        throws RemoteException {

        String contents = state.getTestFileContents(contentPath);
        String fileName = filePath[filePath.length - 1];
        peVC.openFile(filePath);
        activateEditor(fileName);
        editorPart.setTextInEditorWithSave(contents, fileName);
    }

    public void setTextInJavaEditorWithoutSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException {
        String contents = state.getTestFileContents(contentPath);
        peVC.openFile(getClassNodes(projectName, packageName, className));
        activateJavaEditor(className);
        editorPart.setTextinEditorWithoutSave(contents, className + ".java");
    }

    public void typeTextInJavaEditor(String contentPath, String projectName,
        String packageName, String className) throws RemoteException {
        String contents = state.getTestFileContents(contentPath);
        workbenchC.activateEclipseShell();
        peVC.openFile(getClassNodes(projectName, packageName, className));
        activateJavaEditor(className);
        editorPart.typeTextInEditor(contents, className + ".java");
    }

    public void confirmSaveSourceWindow(String buttonType)
        throws RemoteException {
        windowPart.waitUntilShellActive("Save Resource");
        windowPart.confirmWindow("Save Resource", buttonType);
    }

    @Override
    protected void precondition() throws RemoteException {
        // TODO Auto-generated method stub

    }
}
