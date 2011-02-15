//package de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.conditions;
//
//import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
//
//import de.fu_berlin.inf.dpp.stf.server.rmiSarosSWTBot.finder.remoteWidgets.STFBotEditor;
//
//public class IsEditorActive extends DefaultCondition {
//
//    private STFBotEditor editorPart;
//    private String fileName;
//
//    IsEditorActive(STFBotEditor editor, String fileName) {
//        this.fileName = fileName;
//        this.editorPart = editor;
//    }
//
//    public String getFailureMessage() {
//        return "STFBotEditor " + fileName + " is not active.";
//    }
//
//    public boolean test() throws Exception {
//        return editorPart.isEditorActive(fileName);
//    }
// }
