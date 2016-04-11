/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJFolderImpl;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJProjectImpl;
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
        // running yet when this class is instanciated.
        // To make the dependency injection work,
        // SarosPluginContext.initComponent has to be called here.
        if (sessionManager == null && connectionService == null) {
            SarosPluginContext.initComponent(this);
        }

        if (e == null || sessionManager.getSarosSession() != null) {
            return new AnAction[0];
        }

        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project ideaProject = e.getData(CommonDataKeys.PROJECT);
        if (virtualFile == null || ideaProject == null) {
            return new AnAction[0];
        }

        if (!virtualFile.isDirectory()) {
            return new AnAction[0];
        }

        Roster roster = connectionService.getRoster();
        if (roster == null)
            return new AnAction[0];

        IntelliJProjectImpl project = null;
        try {
            project = getProjectFromVirtFile(virtualFile, ideaProject);
        } catch (UnsupportedOperationException e1) {
            return new AnAction[0];
        }

        IntelliJFolderImpl resFolder = new IntelliJFolderImpl(project,
            new File(virtualFile.getPath()));

        //Holger: This disables partial sharing for the moment, until the need arises
        if (!isCompleteProject(project, resFolder)) {
            return new AnAction[0];
        }

        List<AnAction> list = new ArrayList<AnAction>();
        for (RosterEntry rosterEntry : roster.getEntries()) {
            Presence presence = roster.getPresence(rosterEntry.getUser());

            if (presence.getType() == Presence.Type.available) {
                list.add(
                    new ShareWithUserAction(new JID(rosterEntry.getUser())));
            }
        }

        return list.toArray(new AnAction[] {});
    }

    static IntelliJProjectImpl getProjectFromVirtFile(VirtualFile virtFile,
        Project project) {
        Module module = ProjectFileIndex.SERVICE.getInstance(project)
            .getModuleForFile(virtFile);
        String moduleName = null;
        if (module != null) {
            moduleName = module.getName();
        } else {
            //FIXME: Find way to select moduleName for non-module based IDEAs
            //(Webstorm)
            throw new UnsupportedOperationException();
        }
        return new IntelliJProjectImpl(project, moduleName);
    }

    /**
     * Checks whether a given folder is the project (module) root folder, to allow
     * only complete modules to be shared.
     *
     * @param project
     * @param resFolder
     * @return
     */
    private boolean isCompleteProject(IntelliJProjectImpl project,
        IntelliJFolderImpl resFolder) {
        return resFolder.getFullPath().equals(project.getFullPath());
    }
}
