package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

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
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.sarosFinder.remoteComponents.EclipseComponentImp;

public class STFBotEditorImp extends EclipseComponentImp implements
    STFBotEditor {

    private static transient STFBotEditorImp self;

    private String title;
    private String id;
    private SWTBotEclipseEditor editor;

    /* error messages */
    private static String ERROR_MESSAGE_FOR_INVALID_FILENAME = "the passed fileName has no suffix, you should pass a fileName like e.g myFile.xml or if you want to open a java editor, please use the method isJavaEditorOpen";
    private static String ERROR_MESSAGE_FOR_INVALID_CLASSNAME = "You need to only pass the className without sufix like e.g MyClass";

    /**
     * {@link STFBotEditorImp} is a singleton, but inheritance is possible.
     */
    public static STFBotEditorImp getInstance() {
        if (self != null)
            return self;
        self = new STFBotEditorImp();
        return self;
    }

    public void setTitle(String title) {
        // if (this.title == null || !this.title.equals(title)) {
        this.title = title;
        this.editor = bot.editorByTitle(title).toTextEditor();
        // }
    }

    public void setId(String id) {
        if (this.id == null || !this.id.equals(id)) {
            this.id = id;
            this.editor = bot.editorById(id).toTextEditor();
        }
    }

    /***********************************************************************
     * 
     * exported functions
     * 
     ***********************************************************************/

    /**********************************************
     * 
     * actions
     * 
     **********************************************/
    public void activate() throws RemoteException {
        editor.setFocus();
        editor.show();
        editor.setFocus();
    }

    public void closeAndSave() throws RemoteException {
        editor.save();
        editor.close();
    }

    public void closeWithoutSave() throws RemoteException {
        editor.close();
        if (bot().isShellOpen(SHELL_SAVE_RESOURCE)
            && bot().shell(SHELL_SAVE_RESOURCE).isActive())
            confirmShellSaveSource(NO);

    }

    public void confirmShellSaveSource(String buttonType)
        throws RemoteException {
        bot().waitUntilShellOpen(SHELL_SAVE_ALL_FILES_NOW);
        bot().shell(SHELL_SAVE_ALL_FILES_NOW).activate();
        bot().shell(SHELL_SAVE_ALL_FILES_NOW).confirm(buttonType);
    }

    public void setTextAndSave(String contentPath) throws RemoteException {
        String contents = getFileContentNoGUI(contentPath);
        editor.setText(contents);
        editor.save();
    }

    public void setTextWithoutSave(String contentPath) throws RemoteException {
        String contents = getFileContentNoGUI(contentPath);
        editor.setText(contents);
    }

    public void typeText(String text) throws RemoteException {
        editor.setFocus();
        editor.typeText(text);
    }

    public void navigateTo(int line, int column) throws RemoteException {

        editor.setFocus();
        editor.navigateTo(line, column);
    }

    public void selectCurrentLine() throws RemoteException {
        editor.selectCurrentLine();
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        workbench.sleep(500);
    }

    public void selectLine(int line) throws RemoteException {
        editor.selectLine(line);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        workbench.sleep(1000);

    }

    public void selectRange(int line, int column, int length)
        throws RemoteException {
        editor.selectRange(line, column, length);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        workbench.sleep(800);
    }

    public void pressShortcut(String... keys) throws RemoteException {
        editor.setFocus();
        for (String key : keys) {
            try {
                editor.pressShortcut(KeyStroke.getInstance(key));
            } catch (ParseException e) {
                throw new RemoteException("Could not parse \"" + key + "\"", e);
            }
        }
    }

    public void pressShortCut(int modificationKeys, char c)
        throws RemoteException {
        editor.pressShortcut(modificationKeys, c);
    }

    public void pressShortCutDelete() throws RemoteException {

        pressShortcut(IKeyLookup.DELETE_NAME);
    }

    public void pressShortCutEnter() throws RemoteException {
        pressShortcut(IKeyLookup.LF_NAME);
    }

    public void pressShortCutSave() throws RemoteException {

        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.COMMAND, 's');
        else
            editor.pressShortcut(SWT.CTRL, 's');
    }

    public void pressShortRunAsJavaApplication() throws RemoteException {

        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.ALT | SWT.COMMAND, 'x');
        else
            editor.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');
        workbench.sleep(1000);
        editor.pressShortcut(SWT.NONE, 'j');
    }

    public void pressShortCutNextAnnotation() throws RemoteException {

        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.COMMAND, '.');
        else
            editor.pressShortcut(SWT.CTRL, '.');
        workbench.sleep(20);
    }

    public void pressShortCutQuickAssignToLocalVariable()
        throws RemoteException {

        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.COMMAND, '2');
        else
            editor.pressShortcut(SWT.CTRL, '2');
        workbench.sleep(1000);
        editor.pressShortcut(SWT.NONE, 'l');

    }

    public void autoCompleteProposal(String insertText, String proposalText)
        throws RemoteException {
        editor.autoCompleteProposal(checkInputText(insertText), proposalText);
    }

    public void quickfix(String quickFixName) throws RemoteException {
        editor.quickfix(quickFixName);
    }

    public void quickfix(int index) throws RemoteException {
        editor.quickfix(index);
    }

    public void setBreakPoint(int line) throws RemoteException {

        selectLine(line);
        stfMenu.clickMenuWithTexts("Run", "Toggle Breakpoint");
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String getText() throws RemoteException {
        return editor.getText();
    }

    public String getTextOnCurrentLine() throws RemoteException {
        return editor.getTextOnCurrentLine();
    }

    public String getTextOnLine(int line) throws RemoteException {
        return editor.getTextOnLine(line);
    }

    public int getCursorLine() throws RemoteException {
        return editor.cursorPosition().line;
    }

    public int getCursorColumn() throws RemoteException {
        return editor.cursorPosition().column;
    }

    public RGB getLineBackground(int line) throws RemoteException {
        return editor.getLineBackground(line);
    }

    public boolean isFileDirty() throws RemoteException {
        return editor.isDirty();
    }

    public String getSelection() throws RemoteException {

        return editor.getSelection();
    }

    public List<String> getAutoCompleteProposals(String insertText)
        throws RemoteException {

        return editor.getAutoCompleteProposals(insertText);
    }

    /**********************************************
     * 
     * waits until
     * 
     **********************************************/

    public void waitUntilIsActive() throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return isActive();
            }

            public String getFailureMessage() {
                return "The editor is not open.";
            }
        });
    }

    public boolean isActive() throws RemoteException {
        return editor.isActive();
    }

    public void waitUntilContentSame(final String otherClassContent)
        throws RemoteException {
        waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getText().equals(otherClassContent);
            }

            public String getFailureMessage() {
                return "The both contents are not" + " same.";
            }
        });

    }

    /**********************************************
     * 
     * inner functions
     * 
     **********************************************/

    /**
     * @return all filenames on the editors which are opened currently
     */
    public List<String> getTitlesOfAllOpenedEditors() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotEditor editor : bot.editors())
            list.add(editor.getTitle());
        return list;
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

    public void waitUntilFileContentSame(String otherClassContent,
        String... fileNodes) throws RemoteException {
        waitUntil(SarosConditions.isFileContentsSame(this, otherClassContent,
            fileNodes));
    }

}
