package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class STFBotEditorImp extends AbstractRmoteWidget implements
    STFBotEditor {

    private static transient STFBotEditorImp self;

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

    public void setTitle(SWTBotEclipseEditor editor) {
        this.editor = editor;
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

    public void closeWithSave() throws RemoteException {
        editor.save();
        editor.close();
    }

    public void save() throws RemoteException {
        editor.save();
    }

    public void closeWithoutSave() throws RemoteException {
        editor.close();
        if (stfBot.isShellOpen(SHELL_SAVE_RESOURCE)
            && stfBot.shell(SHELL_SAVE_RESOURCE).isActive())
            stfBot.shell(SHELL_SAVE_RESOURCE).confirm(NO);
    }

    public void setTexWithSave(String contentPath) throws RemoteException {
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
        stfBot.sleep(500);
    }

    public void selectLine(int line) throws RemoteException {
        editor.selectLine(line);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        stfBot.sleep(1000);

    }

    public void selectRange(int line, int column, int length)
        throws RemoteException {
        editor.selectRange(line, column, length);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        stfBot.sleep(800);
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
        stfBot.sleep(1000);
        editor.pressShortcut(SWT.NONE, 'j');
    }

    public void pressShortCutNextAnnotation() throws RemoteException {
        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.COMMAND, '.');
        else
            editor.pressShortcut(SWT.CTRL, '.');

        stfBot.sleep(20);
    }

    public void pressShortCutQuickAssignToLocalVariable()
        throws RemoteException {
        if (getOS() == TypeOfOS.MAC)
            editor.pressShortcut(SWT.COMMAND, '2');
        else
            editor.pressShortcut(SWT.CTRL, '2');
        stfBot.sleep(1000);
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

    public boolean isDirty() throws RemoteException {
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
        stfBot.waitUntil(new DefaultCondition() {
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

    public void waitUntilIsTextSame(final String otherText)
        throws RemoteException {
        stfBot.waitUntil(new DefaultCondition() {
            public boolean test() throws Exception {
                return getText().equals(otherText);
            }

            public String getFailureMessage() {
                return "The both contents are not" + " same.";
            }
        });

    }

}
