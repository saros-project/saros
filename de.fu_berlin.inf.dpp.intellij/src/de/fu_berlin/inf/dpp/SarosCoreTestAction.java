package de.fu_berlin.inf.dpp;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smackx.ChatState;

/**
 * @author fzieris
 */
public class SarosCoreTestAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {

        String values = "<html><body><b>Found the following connection states in the Saros Core module:</b><ul>";

        for (ConnectionState val : ConnectionState.values()) {
            values += "<li>" + val.toString() + "</li>";
        }

        values += "</ul>";

        values += "<b>And to test Smack's chat states:</b><ul>";
        for (ChatState val : ChatState.values()) {
            values += "<li>" + val.toString() + "</li>";
        }
        values += "</ul>";

        values += "<b>And a String from the Smack patches:</b> "
            + StreamError.NAMESPACE;

        values += "</body></html>";

        Project project = e.getData(PlatformDataKeys.PROJECT);

        Messages.showMessageDialog(project, values, "Saros Core Test",
            Messages.getInformationIcon());
    }
}
