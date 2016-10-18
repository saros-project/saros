package de.fu_berlin.inf.dpp.intellij.ui.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Saros action group for the pop-up menu when right-clicking on a module.
 */
public class SarosFileShareGroup extends ActionGroup {
    private static final Logger LOG = Logger
        .getLogger(SarosFileShareGroup.class);

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private XMPPConnectionService connectionService;

    @Override
    public void actionPerformed(AnActionEvent e) {
        //do nothing when menu pops-up
    }

    @NotNull
    @Override
    public AnAction[] getChildren(
        @Nullable
        AnActionEvent e) {
        // This has to be initialized here, because doing it in the
        // constructor would be too early. The lifecycle is not
        // running yet when this class is instantiated.
        // To make the dependency injection work,
        // SarosPluginContext.initComponent has to be called here.
        if (sessionManager == null && connectionService == null) {
            SarosPluginContext.initComponent(this);
        }

        if (e == null || sessionManager.getSarosSession() != null) {
            return new AnAction[0];
        }

        if (!isSharableResource(e)) {
            return new AnAction[0];
        }

        Roster roster = connectionService.getRoster();
        if (roster == null) {
            return new AnAction[0];
        }

        List<AnAction> list = new ArrayList<>();
        for (RosterEntry rosterEntry : roster.getEntries()) {
            Presence presence = roster.getPresence(rosterEntry.getUser());
            if (presence.getType() == Presence.Type.available) {
                list.add(
                    new ShareWithUserAction(new JID(rosterEntry.getUser())));
            }
        }

        return list.toArray(new AnAction[0]);
    }

    private boolean isSharableResource(AnActionEvent e) {
        // Don't allow to share any file or folder other than a module
        if (e.getDataContext().getData(DataKeys.MODULE_CONTEXT.getName())
            == null) {
            return false;
        }

        Project project = e.getData(DataKeys.PROJECT);
        Module module = e.getData(DataKeys.MODULE);

        if (project == null || module == null)
            return false;

        // Only allow modules to be shared, that are directly in the project folder

        /*
            FIXME module.getModuleFile() always returns  null for new created modules
            Only switching to another Application Window will resolve the issue
         */

        VirtualFile moduleFile = module.getModuleFile();

        if (moduleFile == null)
            return false;

        // FIXME do not play with the filesystem here, there should be a better way
        return project.getName()
            .equals(moduleFile.getParent().getParent().getName());

    }
}
