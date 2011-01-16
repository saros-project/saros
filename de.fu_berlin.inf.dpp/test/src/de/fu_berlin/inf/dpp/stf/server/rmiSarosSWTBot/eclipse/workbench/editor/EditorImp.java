package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.workbench.editor;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.eclipse.EclipsePart;

public class EditorImp extends EclipsePart implements
    Editor {

    private static transient EditorImp self;

    /* error messages */
    private static String ERROR_MESSAGE_FOR_INVALID_FILENAME = "the passed fileName has no suffix, you should pass a fileName like e.g myFile.xml or if you want to open a java editor, please use the method isJavaEditorOpen";
    private static String ERROR_MESSAGE_FOR_INVALID_CLASSNAME = "You need to only pass the className without sufix like e.g MyClass";

    /* Title of shells */
    private static String SHELL_SAVE_RESOURCE = "Save Resource";

    /**
     * {@link EditorImp} is a singleton, but inheritance is possible.
     */
    public static EditorImp getInstance() {
        if (self != null)
            return self;
        self = new EditorImp();
        return self;
    }

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * operations about opening a editor
     * 
     **********************************************/

    public boolean isEditorOpen(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        return getTitlesOfAllOpenedEditors().contains(fileName);
    }

    public void waitUntilEditorOpen(final String fileName)
        throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
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
        assert !className.contains(".") : ERROR_MESSAGE_FOR_INVALID_CLASSNAME;
        return isEditorOpen(className + SUFIX_JAVA);
    }

    public void waitUntilJavaEditorOpen(String className)
        throws RemoteException {
        assert !className.contains(".") : ERROR_MESSAGE_FOR_INVALID_CLASSNAME;
        waitUntilEditorOpen(className + SUFIX_JAVA);
    }

    /**********************************************
     * 
     * operations about activating a editor
     * 
     **********************************************/

    public void activateEditor(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        try {
            getEditor(fileName).setFocus();
        } catch (TimeoutException e) {
            log.warn("The tab of the editor with the title " + fileName
                + " can't be activated.", e);
        }
    }

    public void waitUntilEditorActive(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        waitUntil(SarosConditions.isEditorActive(this, fileName));
    }

    public boolean isEditorActive(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        try {
            return bot.activeEditor().getTitle().equals(fileName);
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    public void activateJavaEditor(String className) throws RemoteException {
        assert !className.contains(".") : ERROR_MESSAGE_FOR_INVALID_CLASSNAME;
        try {
            getJavaEditor(className).setFocus();
        } catch (TimeoutException e) {
            log.warn("The tab of the editor with the title " + className
                + SUFIX_JAVA + " can't be activated.", e);
        }
    }

    public void waitUntilJavaEditorActive(String className)
        throws RemoteException {
        assert !className.contains(".") : ERROR_MESSAGE_FOR_INVALID_CLASSNAME;
        waitUntilEditorActive(className + SUFIX_JAVA);
    }

    public boolean isJavaEditorActive(String className) throws RemoteException {
        assert !className.contains(".") : ERROR_MESSAGE_FOR_INVALID_CLASSNAME;
        if (!isJavaEditorOpen(className))
            return false;
        return isEditorActive(className + SUFIX_JAVA);
    }

    /**********************************************
     * 
     * operations about closing a editor
     * 
     **********************************************/
    public void closeEditorWithSave(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        if (isEditorOpen(fileName)) {
            activateEditor(fileName);
            getEditor(fileName).save();
            getEditor(fileName).close();
        }
    }

    public void closeEditorWithoutSave(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        if (isEditorOpen(fileName)) {
            activateEditor(fileName);
            getEditor(fileName).close();
            if (shellC.isShellActive("Save Resource"))
                confirmWindowSaveSource(YES);
        }
    }

    public void waitUntilEditorClosed(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        waitUntil(SarosConditions.isEditorClosed(this, fileName));
    }

    public void closeJavaEditorWithSave(String className)
        throws RemoteException {
        assert !className.contains(".") : ERROR_MESSAGE_FOR_INVALID_CLASSNAME;
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
        assert !className.contains(".") : ERROR_MESSAGE_FOR_INVALID_CLASSNAME;
        closeEditorWithoutSave(className + SUFIX_JAVA);
    }

    public void waitUntilJavaEditorClosed(String className)
        throws RemoteException {
        assert !className.contains(".") : ERROR_MESSAGE_FOR_INVALID_CLASSNAME;
        waitUntilEditorClosed(className + SUFIX_JAVA);
    }

    public void confirmWindowSaveSource(String buttonType)
        throws RemoteException {
        shellC.waitUntilShellOpen(SHELL_SAVE_RESOURCE);
        shellC.activateShellWithText(SHELL_SAVE_RESOURCE);
        shellC.confirmShell(SHELL_SAVE_RESOURCE, buttonType);
    }

    // public void closeAllOpenedEditors() throws RemoteException {
    // bot.closeAllEditors();
    // }

    /**********************************************
     * 
     * get contents of a editor
     * 
     **********************************************/

    public String getTextOfEditor(String... fileNodes) throws RemoteException {
        String fileName = fileNodes[fileNodes.length - 1];
        precondition(fileNodes);
        return getEditor(fileName).getText();
    }

    public String getTextOfJavaEditor(String projectName, String pkg,
        String className) throws RemoteException {
        precondition(getClassNodes(projectName, pkg, className));
        return getJavaEditor(className).getText();
    }

    public String getTextOnCurrentLine(String fileName) throws RemoteException {
        return getEditor(fileName).getTextOnCurrentLine();
    }

    public String getTextOnLine(String fileName, int line)
        throws RemoteException {
        return getEditor(fileName).getTextOnLine(line);
    }

    public String getJavaTextOnLine(String projectName, String pkg,
        String className, int line) throws RemoteException {
        precondition(getClassNodes(projectName, pkg, className));
        return getJavaEditor(className).getTextOnLine(line);
    }

    public int getCursorLine(String fileName) throws RemoteException {
        return getEditor(fileName).cursorPosition().line;
    }

    public int getCursorColumn(String fileName) throws RemoteException {
        return getEditor(fileName).cursorPosition().column;
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

    public void waitUntilFileContentSame(String otherClassContent,
        String... fileNodes) throws RemoteException {
        waitUntil(SarosConditions.isFileContentsSame(this, otherClassContent,
            fileNodes));
    }

    public void waitUntilClassContentsSame(final String projectName,
        final String pkg, final String className, final String otherClassContent)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getClassContent(projectName, pkg, className).equals(
                    otherClassContent);
            }

            public String getFailureMessage() {
                return "The both contents are not" + " same.";
            }
        });
    }

    public String getClassContent(String projectName, String pkg,
        String className) throws RemoteException, IOException, CoreException {
        IPath path = new Path(getClassPath(projectName, pkg, className));
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);
        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return ConvertStreamToString(file.getContents());
    }

    public String getFileContent(String... nodes) throws RemoteException,
        IOException, CoreException {
        IPath path = new Path(getPath(nodes));
        log.info("Checking existence of file \"" + path + "\"");
        final IFile file = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        log.info("Checking full path: \"" + file.getFullPath().toOSString()
            + "\"");
        return ConvertStreamToString(file.getContents());
    }

    /**********************************************
     * 
     * modify contents of a editor
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
        workbenchC.activateEclipseShell();
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.setFocus();
        editor.typeText(text);

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
        typeTextInEditor(checkInputText(text),
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
            pEV.openClass(projectName, pkg, className);
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

    public void navigateInEditor(String fileName, int line, int column)
        throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.setFocus();
        editor.navigateTo(line, column);
    }

    public void selectCurrentLine(String fileName) throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.selectCurrentLine();
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        workbenchC.sleep(500);
    }

    public void selectLine(String fileName, int line) throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.selectLine(line);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        workbenchC.sleep(1000);

    }

    public void selectRange(String fileName, int line, int column, int length)
        throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.selectRange(line, column, length);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        workbenchC.sleep(800);
    }

    public String getSelection(String fileName) throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        return editor.getSelection();
    }

    /**********************************************
     * 
     * modify contents of a editor with keyboard
     * 
     **********************************************/
    public void pressShortcut(String fileName, String... keys)
        throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.setFocus();
        for (String key : keys) {
            try {
                editor.pressShortcut(KeyStroke.getInstance(key));
            } catch (ParseException e) {
                throw new RemoteException("Could not parse \"" + key + "\"", e);
            }
        }
    }

    public void pressShortCut(String fileName, int modificationKeys, char c)
        throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.pressShortcut(modificationKeys, c);
    }

    public void pressShortCutDelete(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        pressShortcut(fileName, IKeyLookup.DELETE_NAME);
    }

    public void pressShortCutEnter(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        pressShortcut(fileName, IKeyLookup.LF_NAME);
    }

    public void pressShortCutSave(String fileName) throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        SWTBotEclipseEditor editor = getEditor(fileName);
        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.COMMAND, 's');
        else
            editor.pressShortcut(SWT.CTRL, 's');
    }

    public void pressShortRunAsJavaApplication(String fileName)
        throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.ALT | SWT.COMMAND, 'x');
        else
            editor.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');
        workbenchC.sleep(1000);
        editor.pressShortcut(SWT.NONE, 'j');
    }

    public void pressShortCutNextAnnotation(String fileName)
        throws RemoteException {
        assert fileName.contains(".") : ERROR_MESSAGE_FOR_INVALID_FILENAME;
        SWTBotEclipseEditor editor = getEditor(fileName);
        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.COMMAND, '.');
        else
            editor.pressShortcut(SWT.CTRL, '.');
        workbenchC.sleep(20);
    }

    public void pressShortCutQuickAssignToLocalVariable(String fileName)
        throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.COMMAND, '2');
        else
            editor.pressShortcut(SWT.CTRL, '2');
        workbenchC.sleep(1000);
        editor.pressShortcut(SWT.NONE, 'l');

    }

    public void autoCompleteProposal(String fileName, String insertText,
        String proposalText) throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.autoCompleteProposal(checkInputText(insertText), proposalText);
    }

    public List<String> getAutoCompleteProposals(String fileName,
        String insertText) throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        return editor.getAutoCompleteProposals(insertText);
    }

    public void quickfix(String fileName, String quickFixName)
        throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.quickfix(quickFixName);
    }

    public void quickfix(String fileName, int index) throws RemoteException {
        SWTBotEclipseEditor editor = getEditor(fileName);
        editor.quickfix(index);
    }

    /**********************************************
     * 
     * infos about debug
     * 
     **********************************************/
    public void setBreakPoint(int line, String projectName, String packageName,
        String className) throws RemoteException {
        precondition(getClassNodes(projectName, packageName, className));
        selectLine(className + SUFIX_JAVA, line);
        basic.clickMenuWithTexts("Run", "Toggle Breakpoint");
    }

    /**********************************************
     * 
     * inner functions
     * 
     **********************************************/

    private void precondition(String... fileNodes) throws RemoteException {
        String fileName = fileNodes[fileNodes.length - 1];
        if (!isEditorOpen(fileName)) {
            pEV.openFile(fileNodes);
        }
        if (!isEditorActive(fileName)) {
            activateEditor(fileName);
        }
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

    /**
     * 
     * @param fileName
     *            the filename on the editor tab
     * @return an editor specified by the given fileName which provides methods
     *         for text editors.
     * 
     */
    public SWTBotEclipseEditor getEditor(String fileName) {
        SWTBotEditor editor = bot.editorByTitle(fileName);
        return editor.toTextEditor();
    }

    /**
     * 
     * @param className
     *            the name of the java file without the suffix ".java".
     * @return an editor specified by the given className which provides methods
     *         for text editors.
     * 
     */
    public SWTBotEclipseEditor getJavaEditor(String className) {
        return getEditor(className + SUFIX_JAVA);
    }

}
