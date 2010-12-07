package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseComponent;

public class EditorComponenttImp extends EclipseComponent implements
    EditorComponent {

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

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * open/activate/close a editor
     * 
     **********************************************/

    public boolean isEditorOpen(String fileName) throws RemoteException {
        return getTitlesOfAllOpenedEditors().contains(fileName);
    }

    public void waitUntilEditorOpen(final String fileName)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isEditorOpen(fileName);
            }

            public String getFailureMessage() {
                return "The editor " + fileName + "is not open.";
            }
        });

    }

    public boolean isJavaEditorOpen(String className) throws RemoteException {
        return isEditorOpen(className + SUFIX_JAVA);
    }

    public void waitUntilJavaEditorOpen(String className)
        throws RemoteException {
        waitUntilEditorOpen(className + SUFIX_JAVA);
    }

    public void activateEditor(String fileName) throws RemoteException {
        try {
            getEditor(fileName).setFocus();
        } catch (TimeoutException e) {
            log.warn("The tab" + fileName + " does not activate '", e);
        }
    }

    public void waitUntilEditorActive(String name) throws RemoteException {
        waitUntil(SarosConditions.isEditorActive(this, name));
    }

    public void activateJavaEditor(String className) throws RemoteException {
        try {
            getJavaEditor(className).setFocus();
        } catch (TimeoutException e) {
            log.warn("The tab" + className + SUFIX_JAVA
                + " does not activate '", e);
        }

    }

    public void waitUntilJavaEditorActive(String className)
        throws RemoteException {
        waitUntilEditorActive(className + SUFIX_JAVA);
    }

    public boolean isEditorActive(String fileName) throws RemoteException {
        try {
            return bot.activeEditor().getTitle().equals(fileName);
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public boolean isJavaEditorActive(String className) throws RemoteException {
        if (!isJavaEditorOpen(className))
            return false;
        return isEditorActive(className + SUFIX_JAVA);
    }

    public void closeEditorWithSave(String fileName) throws RemoteException {
        if (isEditorOpen(fileName)) {
            activateEditor(fileName);
            getEditor(fileName).save();
            getEditor(fileName).close();
        }
    }

    public void closeEditorWithoutSave(String fileName) throws RemoteException {
        if (isEditorOpen(fileName)) {
            activateEditor(fileName);
            getEditor(fileName).close();
            if (windowPart.isShellActive("Save Resource"))
                confirmWindowSaveSource(YES);
        }
    }

    public void waitUntilEditorClosed(String fileName) throws RemoteException {
        waitUntil(SarosConditions.isEditorClosed(this, fileName));
    }

    public void closeJavaEditorWithSave(String className)
        throws RemoteException {
        closeEditorWithSave(className + SUFIX_JAVA);
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

    public void closejavaEditorWithoutSave(String className)
        throws RemoteException {
        closeEditorWithoutSave(className + SUFIX_JAVA);
    }

    public void waitUntilJavaEditorClosed(String className)
        throws RemoteException {
        waitUntilEditorClosed(className + SUFIX_JAVA);
    }

    public void confirmWindowSaveSource(String buttonType)
        throws RemoteException {
        windowPart.waitUntilShellActive("Save Resource");
        windowPart.confirmWindow("Save Resource", buttonType);
    }

    // public void closeAllOpenedEditors() throws RemoteException {
    // bot.closeAllEditors();
    // }

    /**********************************************
     * 
     * get contents infos of a editor
     * 
     **********************************************/

    public String getTextOfEditor(String... fileNodes) throws RemoteException {
        String fileName = fileNodes[fileNodes.length - 1];
        precondition(fileNodes);
        return getEditor(fileName).getText();
    }

    public String getTextOfJavaEditor(String projectName, String packageName,
        String className) throws RemoteException {
        if (!isJavaEditorOpen(className))
            peVC.openClass(projectName, packageName, className);
        if (!isJavaEditorActive(className))
            activateJavaEditor(className);
        return getJavaEditor(className).getText();
    }

    public String getJavaTextOnLine(String projectName, String pkg,
        String className, int line) throws RemoteException {
        precondition(getClassNodes(projectName, pkg, className));
        return getJavaEditor(className).getTextOnLine(line);
    }

    public void selectLineInEditor(int line, String fileName)
        throws RemoteException {
        getEditor(fileName).selectLine(line);
    }

    public void selectLineInJavaEditor(int line, String className)
        throws RemoteException {
        selectLineInEditor(line, className + SUFIX_JAVA);
    }

    public int getJavaCursorLinePosition(String className)
        throws RemoteException {
        activateJavaEditor(className);
        return getJavaEditor(className).cursorPosition().line;
    }

    public RGB getJavaLineBackground(String className, int line)
        throws RemoteException {
        return getJavaEditor(className).getLineBackground(line);
    }

    public SWTBotEclipseEditor getEditor(String fileName)
        throws RemoteException {
        SWTBotEditor editor = bot.editorByTitle(fileName);
        return editor.toTextEditor();
    }

    public SWTBotEclipseEditor getJavaEditor(String className)
        throws RemoteException {
        return getEditor(className + SUFIX_JAVA);
    }

    /**********************************************
     * 
     * set contents of a editor
     * 
     **********************************************/
    public void setTextInEditorWithSave(String contentPath, String... fileNodes)
        throws RemoteException {
        String contents = getTestFileContents(contentPath);
        String fileName = fileNodes[fileNodes.length - 1];
        precondition(fileNodes);
        // e.setFocus();
        // e.pressShortcut(Keystrokes.LF);
        getEditor(fileName).setText(contents);
        getEditor(fileName).save();
    }

    public void waitUntilEditorContentSame(String otherClassContent,
        String... fileNodes) throws RemoteException {
        waitUntil(SarosConditions.isEditorContentsSame(this, otherClassContent,
            fileNodes));
    }

    public void setTextInJavaEditorWithSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException {
        setTextInEditorWithSave(contentPath,
            getClassNodes(projectName, packageName, className));
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

        // e.typeText("hallo wie geht es dir !%%%");
        // e.pressShortcut(Keystrokes.LF);
        // e.typeText("mir geht es gut!");
        // delegate.sleep(2000);
        //
        // delegate.sleep(2000);

        // editorObject.setTextinEditorWithSave(contents, className + ".java");
    }

    public void waitUntilJavaEditorContentSame(String otherClassContent,
        String projectName, String pkg, String className)
        throws RemoteException {
        waitUntilEditorContentSame(otherClassContent,
            getClassNodes(projectName, pkg, className));
    }

    public void setTextInEditorWithoutSave(String contentPath,
        String... fileNodes) throws RemoteException {
        String contents = getTestFileContents(contentPath);
        String fileName = fileNodes[fileNodes.length - 1];
        precondition(fileNodes);
        getEditor(fileName).setText(contents);
    }

    public void setTextInJavaEditorWithoutSave(String contentPath,
        String projectName, String packageName, String className)
        throws RemoteException {
        setTextInEditorWithoutSave(contentPath,
            getClassNodes(projectName, packageName, className));
    }

    public void typeTextInEditor(String text, String... fileNodes)
        throws RemoteException {
        String fileName = fileNodes[fileNodes.length - 1];
        precondition(fileNodes);
        getEditor(fileName).typeText(text);
        // e.navigateTo(3, 0);
        // e.autoCompleteProposal("main", "main - main method");
        // e.autoCompleteProposal("sys", "sysout - print to standard out");
        // e.typeText("System.currentTimeMillis()");
        // // SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        // // SWTBotEclipseEditor e = getTextEditor(fileName);
        // // e.navigateTo(3, 0);
        // // e.pressShortcut(SWT.CTRL, '.');
        // // e.quickfix("Add unimplemented methods");
        // // e.navigateTo(7, 0);
        // //
        // // e.navigateTo(3, 0);
        // // e.autoCompleteProposal("main", "main - main method");
        // // e.autoCompleteProposal("sys", "sysout - print to standard out");
        // // e.typeText("System.currentTimeMillis()");
        //
        // // e.typeText("thread.start();\n");
        // // e.typeText("thread.join();");
        // // SWTBotPreferences.KEYBOARD_LAYOUT = "DE_DE";
        // // e.quickfix("Add throws declaration");
        // // e.pressShortcut(SWT.NONE, (char) 27);
        // // e.pressShortcut(SWT.NmainONE, '\n');
        // //
        // // e.pressShortcut(SWT.CTRL, 's');
        // //
        // // e.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');
        // // e.pressShortcut(SWT.NONE, 'j');
    }

    public void typeTextInJavaEditor(String text, String projectName,
        String packageName, String className) throws RemoteException {
        typeTextInEditor(text,
            getClassNodes(projectName, packageName, className));
    }

    public boolean isFileDirty(String... fileNodes) throws RemoteException {
        String fileName = fileNodes[fileNodes.length - 1];
        precondition(fileNodes);
        return getEditor(fileName).isDirty();
    }

    public boolean isClassDirty(String projectName, String pkg,
        String className, final String idOfEditor) throws RemoteException {
        if (!isJavaEditorOpen(className))
            peVC.openClass(projectName, pkg, className);
        if (!isJavaEditorActive(className))
            activateJavaEditor(className);
        return getJavaEditor(className).isDirty();
        // return isFileDirty(getClassNodes(projectName, pkg, className));
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

    /**********************************************
     * 
     * infos about debug
     * 
     **********************************************/
    public void setBreakPoint(int line, String projectName, String packageName,
        String className) throws RemoteException {
        precondition(getClassNodes(projectName, packageName, className));
        selectLineInJavaEditor(line, className);
        menuPart.clickMenuWithTexts("Run", "Toggle Breakpoint");
    }

    /**********************************************
     * 
     * inner functions
     * 
     **********************************************/

    private void precondition(String... fileNodes) throws RemoteException {
        String fileName = fileNodes[fileNodes.length - 1];
        if (!isEditorOpen(fileName))
            peVC.openFile(fileNodes);
        if (!isEditorActive(fileName))
            activateEditor(fileName);
    }

    /**
     * @return all filenames on the editors which are opened currently
     */
    public List<String> getTitlesOfAllOpenedEditors() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotEditor editor : bot.editors())
            list.add(editor.getTitle());
        return list;
    }
}
