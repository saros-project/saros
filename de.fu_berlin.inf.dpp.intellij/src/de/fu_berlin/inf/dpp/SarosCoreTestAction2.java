package de.fu_berlin.inf.dpp;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import de.fu_berlin.inf.dpp.net.stun.IStunService;
import de.fu_berlin.inf.dpp.net.stun.internal.StunServiceImpl;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPAccess;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPAccessImpl;
import de.fu_berlin.inf.dpp.net.upnp.internal.UPnPServiceImpl;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import java.util.Collection;

/**
 * Created by fzieris
 */
public class SarosCoreTestAction2 extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);

        final String jabberID = Messages
            .showInputDialog(project, "Your Jabber-ID (e.g. 'dev1_alice_stf')",
                "What are your credentials?", Messages.getQuestionIcon());
        final String password = Messages
            .showInputDialog(project, "Password (e.g. 'dev')",
                "What are your credentials?", Messages.getQuestionIcon());

        IUPnPAccess upnpAccess = new UPnPAccessImpl();
        IUPnPService upnp = new UPnPServiceImpl(upnpAccess);

        IStunService stun = new StunServiceImpl();
        XMPPConnectionService service = new XMPPConnectionService(upnp, stun);

        try {
            service.connect(
                new ConnectionConfiguration("saros-con.imp.fu-berlin.de"),
                jabberID, password);

            String values = "<html><body>Your roster entries are: <ul>";

            final Roster roster = service.getRoster();
            final Collection<RosterEntry> entries = roster.getEntries();
            for (RosterEntry entry : entries) {
                values += "<li>" + entry.getUser() + "</li>";
            }

            service.disconnect();

            values += "</ul>Test successful, connection was closed.</body></html>";

            Messages.showMessageDialog(project, values, "XMPP Connection Test",
                Messages.getInformationIcon());
        } catch (XMPPException ex) {
            Messages.showMessageDialog(project, "Something went wrong",
                "What a pitty ...", Messages.getErrorIcon());
        }

    }
}
