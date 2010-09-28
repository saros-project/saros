package de.fu_berlin.inf.dpp.stf.swtbot.helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import de.fu_berlin.inf.dpp.stf.swtbot.RmiSWTWorkbenchBot;
import de.fu_berlin.inf.dpp.stf.swtbot.SarosSWTWorkbenchBot;

public class EditorObject {
    private static final transient Logger log = Logger
        .getLogger(EditorObject.class);
    private RmiSWTWorkbenchBot rmiBot;
    private WaitUntilObject wUntil;
    private static SarosSWTWorkbenchBot bot = new SarosSWTWorkbenchBot();

    public EditorObject(RmiSWTWorkbenchBot rmiBot) {
        this.rmiBot = rmiBot;
        this.wUntil = rmiBot.wUntilObject;
    }

    public void setTextinEditor(String contents, String fileName) {
        SWTBotEclipseEditor e = getTextEditor(fileName);
        e.setText(contents);
        e.save();
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

        // try {
        // return delegate.editorByTitle(name) != null;
        // } catch (WidgetNotFoundException e) {
        // log.warn("Editor '" + name + "' doesn't exist!");
        // return false;
        // }
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

}
