package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedObjects;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.EclipseObject;

/**
 * This class contains basic API to find widgets based on the GUI component
 * "editor" in SWTBot and to perform the operations on a editor, which is only
 * used by rmi server side and not exported.
 * 
 * @author lchen
 */
public class EditorObject extends EclipseObject {

    /**
     * Sets the text into the given editor and saves the change.
     * 
     * @param text
     *            the text to set.
     * @param fileName
     *            the filename on the editor tab
     */
    public void setTextInEditorWithSave(String text, String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        // e.setFocus();
        // e.pressShortcut(Keystrokes.LF);
        e.setText(text);
        e.save();
    }

    /**
     * Sets the text into the given editor without saving the change.
     * 
     * @param text
     *            the text to set.
     * @param fileName
     *            the filename on the editor tab
     */
    public void setTextinEditorWithoutSave(String text, String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.setText(text);
    }

    /**
     * TODO: This function doesn't work exactly. It should be happen that the
     * text isn't typed in the right editor, When your saros-instances are fresh
     * started.
     * 
     * @param text
     *            the text to set.
     * @param fileName
     *            the filename on the editor tab
     */
    public void typeTextInEditor(String text, final String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.navigateTo(3, 0);
        e.autoCompleteProposal("main", "main - main method");
        e.autoCompleteProposal("sys", "sysout - print to standard out");
        e.typeText("System.currentTimeMillis()");
        // SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        // SWTBotEclipseEditor e = getTextEditor(fileName);
        // e.navigateTo(3, 0);
        // e.pressShortcut(SWT.CTRL, '.');
        // e.quickfix("Add unimplemented methods");
        // e.navigateTo(7, 0);
        //
        // e.navigateTo(3, 0);
        // e.autoCompleteProposal("main", "main - main method");
        // e.autoCompleteProposal("sys", "sysout - print to standard out");
        // e.typeText("System.currentTimeMillis()");

        // e.typeText("thread.start();\n");
        // e.typeText("thread.join();");
        // SWTBotPreferences.KEYBOARD_LAYOUT = "DE_DE";
        // e.quickfix("Add throws declaration");
        // e.pressShortcut(SWT.NONE, (char) 27);
        // e.pressShortcut(SWT.NmainONE, '\n');
        //
        // e.pressShortcut(SWT.CTRL, 's');
        //
        // e.pressShortcut(SWT.ALT | SWT.SHIFT, 'x');
        // e.pressShortcut(SWT.NONE, 'j');
    }

    /**
     * 
     * @param fileName
     *            the filename on the editor tab
     * @return an extended version of the editor bot which provides methods for
     *         text editors.
     */
    public SWTBotEclipseEditor getTextEditor(String fileName) {
        SWTBotEditor editor = bot.editorByTitle(fileName);
        return editor.toTextEditor();
    }

    /**
     * 
     * @param fileName
     *            the filename on the editor tab
     * @return the editor with the specified title
     * @see SWTWorkbenchBot#editorByTitle(String)
     */
    public SWTBotEditor getEditor(String fileName) {
        return bot.editorByTitle(fileName);
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
     * @param line
     *            the line number to select, 0 based.
     * @param fileName
     *            the filename on the editor tab
     */
    public void selectLineInEditor(int line, String fileName) {
        getTextEditor(fileName).selectLine(line);
    }

    /**
     * Closes the given editor. The editor must belong to this workbench page.
     * <p>
     * Any unsaved changes are discard, if the editor has unsaved content.
     * </p>
     * 
     * @param fileName
     *            the filename on the editor tab
     */
    public void closeEditorWithoutSave(String fileName) {
        if (isEditorOpen(fileName)) {
            getTextEditor(fileName).close();
        }
    }

    /**
     * Closes the given editor. The editor must belong to this workbench page.
     * <p>
     * Saves the editor, if the editor has unsaved content.
     * </p>
     * 
     * @param fileName
     *            the filename on the editor tab
     */
    public void closeEditorWithSave(String fileName) {
        if (isEditorOpen(fileName)) {
            getTextEditor(fileName).saveAndClose();
        }
    }

    /**
     * 
     * @param fileName
     *            the filename on the editor tab
     * @return <tt>true</tt>, if the given editor is open.
     */
    public boolean isEditorOpen(String fileName) {
        return getTitlesOfAllOpenedEditors().contains(fileName);
    }

    /**
     * Activates the tabItem.
     * 
     * @param fileName
     *            the filename on the editor tab
     */
    public void activateEditor(String fileName) {
        try {
            bot.cTabItem(fileName).activate();
        } catch (TimeoutException e) {
            log.warn("The tab" + fileName + " does not activate '", e);
        }

    }

    /**
     * 
     * @param fileName
     *            the filename on the editor tab
     * @return <tt>true</tt>, if there is a active editor and it's name is same
     *         as the given fileName.
     */
    public boolean isEditorActive(String fileName) {
        try {
            return bot.activeEditor().getTitle().equals(fileName);
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    /**
     * 
     * Waits until the given editor is active.
     * 
     * @param fileName
     *            the filename on the editor tab
     */
    public void waitUntilEditorIsActive(String fileName) {
        waitUntil(SarosConditions.isEditorActive(this, fileName));
    }
}
