package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.remoteFinder.remoteWidgets;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

public class RemoteBotEditor extends AbstractRmoteWidget implements
    IRemoteBotEditor {

    private static transient RemoteBotEditor self;

    private SWTBotEclipseEditor widget;

    /**
     * {@link RemoteBotEditor} is a singleton, but inheritance is possible.
     */
    public static RemoteBotEditor getInstance() {
        if (self != null)
            return self;
        self = new RemoteBotEditor();
        return self;
    }

    public void setWidget(SWTBotEclipseEditor editor) {
        this.widget = editor;
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

    public void show() throws RemoteException {
        widget.show();
    }

    public void setFocus() throws RemoteException {
        widget.setFocus();
    }

    public void closeWithSave() throws RemoteException {
        widget.save();
        widget.close();
    }

    public void save() throws RemoteException {
        widget.save();
    }

    public void closeWithoutSave() throws RemoteException {
        widget.close();
        if (stfBot.isShellOpen(SHELL_SAVE_RESOURCE)
            && stfBot.shell(SHELL_SAVE_RESOURCE).isActive())
            stfBot.shell(SHELL_SAVE_RESOURCE).confirm(NO);
    }

    public void setTexWithSave(String contentPath) throws RemoteException {
        String contents = getFileContentNoGUI(contentPath);
        widget.setText(contents);
        widget.save();
    }

    public void setTextWithoutSave(String contentPath) throws RemoteException {
        String contents = getFileContentNoGUI(contentPath);
        widget.setText(contents);
    }

    public void typeText(String text) throws RemoteException {
        widget.setFocus();
        widget.typeText(text);
    }

    public void navigateTo(int line, int column) throws RemoteException {
        widget.setFocus();
        widget.navigateTo(line, column);
    }

    public void selectCurrentLine() throws RemoteException {
        widget.selectCurrentLine();
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        stfBot.sleep(500);
    }

    public void selectLine(int line) throws RemoteException {
        widget.selectLine(line);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        stfBot.sleep(1000);

    }

    public void selectRange(int line, int column, int length)
        throws RemoteException {
        widget.selectRange(line, column, length);
        // It's is necessary to sleep a litte time so that the following
        // operation like quickfix will be successfully performed.
        stfBot.sleep(800);
    }

    public void pressShortcut(String... keys) throws RemoteException {
        widget.setFocus();
        for (String key : keys) {
            try {
                widget.pressShortcut(KeyStroke.getInstance(key));
            } catch (ParseException e) {
                throw new RemoteException("Could not parse \"" + key + "\"", e);
            }
        }
    }

    public void pressShortCut(int modificationKeys, char c)
        throws RemoteException {
        widget.pressShortcut(modificationKeys, c);
    }

    public void pressShortCutDelete() throws RemoteException {
        pressShortcut(IKeyLookup.DELETE_NAME);
    }

    public void pressShortCutEnter() throws RemoteException {
        pressShortcut(IKeyLookup.LF_NAME);
    }

    public void pressShortCutSave() throws RemoteException {
        if (getOS() == TypeOfOS.MAC)
            widget.pressShortcut(SWT.COMMAND, 's');
        else
            widget.pressShortcut(SWT.CTRL, 's');
    }

    public void pressShortRunAsJavaApplication() throws RemoteException {
        if (getOS() == TypeOfOS.MAC)
            widget.pressShortcut(SWT.ALT | SWT.COMMAND, 'x');
        else
            widget.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');
        stfBot.sleep(1000);
        widget.pressShortcut(SWT.NONE, 'j');
    }

    public void pressShortCutNextAnnotation() throws RemoteException {
        if (getOS() == TypeOfOS.MAC)
            widget.pressShortcut(SWT.COMMAND, '.');
        else
            widget.pressShortcut(SWT.CTRL, '.');

        stfBot.sleep(20);
    }

    public void pressShortCutQuickAssignToLocalVariable()
        throws RemoteException {
        if (getOS() == TypeOfOS.MAC)
            widget.pressShortcut(SWT.COMMAND, '2');
        else
            widget.pressShortcut(SWT.CTRL, '2');
        stfBot.sleep(1000);
        widget.pressShortcut(SWT.NONE, 'l');

    }

    public void autoCompleteProposal(String insertText, String proposalText)
        throws RemoteException {
        widget.autoCompleteProposal(checkInputText(insertText), proposalText);
    }

    public void quickfix(String quickFixName) throws RemoteException {
        widget.quickfix(quickFixName);
    }

    public void quickfix(int index) throws RemoteException {
        widget.quickfix(index);
    }

    /**********************************************
     * 
     * states
     * 
     **********************************************/

    public String getText() throws RemoteException {
        return widget.getText();
    }

    public String getTextOnCurrentLine() throws RemoteException {
        return widget.getTextOnCurrentLine();
    }

    public String getTextOnLine(int line) throws RemoteException {
        return widget.getTextOnLine(line);
    }

    public int getCursorLine() throws RemoteException {
        return widget.cursorPosition().line;
    }

    public int getCursorColumn() throws RemoteException {
        return widget.cursorPosition().column;
    }

    public RGB getLineBackground(int line) throws RemoteException {
        return widget.getLineBackground(line);
    }

    public boolean isDirty() throws RemoteException {
        return widget.isDirty();
    }

    public String getSelection() throws RemoteException {

        return widget.getSelection();
    }

    public List<String> getAutoCompleteProposals(String insertText)
        throws RemoteException {
        return widget.getAutoCompleteProposals(insertText);
    }

    public boolean isActive() throws RemoteException {
        return widget.isActive();
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
