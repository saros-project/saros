package de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.noExportedPages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;

import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.conditions.SarosConditions;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.saros.SarosControler;
import de.fu_berlin.inf.dpp.stf.server.rmiSwtbot.eclipse.workbench.EclipseObject;

public class EditorObject extends EclipseObject {

    public EditorObject(SarosControler rmiBot) {
        super(rmiBot);
    }

    public void setTextInEditorWithSave(String contents, String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.setFocus();
        e.pressShortcut(Keystrokes.LF);
        e.setText(contents);
        e.save();
    }

    public void setTextinEditorWithoutSave(String contents, String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.setText(contents);
    }

    /**
     * TODO: This function doesn't work exactly. It would be happen that die
     * text contents isn't typed in the right editor, When your saros-instances
     * are fresh started.
     * 
     * @param contents
     * @param fileName
     */
    public void typeTextInEditor(String contents, final String fileName) {

        // SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
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

    public SWTBotEclipseEditor getTextEditor(String fileName) {
        SWTBotEditor editor;
        editor = bot.editorByTitle(fileName);
        SWTBotEclipseEditor e = editor.toTextEditor();
        return e;
    }

    public List<String> getEditorTitles() {
        ArrayList<String> list = new ArrayList<String>();
        for (SWTBotEditor editor : bot.editors())
            list.add(editor.getTitle());
        return list;
    }

    public void selectLineInEditor(int line, String fileName) {
        getTextEditor(fileName).selectLine(line);
    }

    public void closeEditorWithText(String text) {
        if (isEditorOpen(text)) {
            bot.editorByTitle(text).close();
        }
    }

    public boolean isEditorOpen(String name) {
        return getEditorTitles().contains(name);
    }

    public void activateEditor(String name) {
        try {
            bot.cTabItem(name).activate();
        } catch (WidgetNotFoundException e) {
            log.warn("tableItem not found '", e);
        }
        // waitUntilEditorActive(name);
    }

    public boolean isEditorActive(String name) {
        return bot.activeEditor().getTitle().equals(name);
    }

    public void waitUntilEditorActive(String name) {
        waitUntil(SarosConditions.isEditorActive(bot, name));
    }
}
