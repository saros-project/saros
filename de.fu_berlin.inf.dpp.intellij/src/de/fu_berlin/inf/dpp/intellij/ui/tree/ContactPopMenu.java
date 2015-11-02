package de.fu_berlin.inf.dpp.intellij.ui.tree;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Contact pop-up menu for selecting a project to share. Opened when
 * right-clicking on a contact.
 */
class ContactPopMenu extends JPopupMenu {

    private static final Logger LOG = Logger.getLogger(ContactPopMenu.class);

    @Inject
    private IWorkspace workspace;

    @Inject
    private Project project;

    private final ContactTreeRootNode.ContactInfo contactInfo;

    public ContactPopMenu(ContactTreeRootNode.ContactInfo contactInfo) {
        this.contactInfo = contactInfo;

        if (workspace == null && project == null) {
            SarosPluginContext.initComponent(this);
        }

        JMenu menuShareProject = new JMenu("Work together on...");
        menuShareProject.setIcon(IconManager.SESSIONS_ICON);

        if (project == null) {
            return;
        }

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        if (moduleManager != null) {
            for (Module module : moduleManager.getModules()) {

                if (project.getName().equalsIgnoreCase(module.getName())) {
                    continue;
                }

                JMenuItem moduleItem = new JMenuItem(module.getName());
                moduleItem.addActionListener(new ShareDirectoryAction(new File(
                    module.getProject().getBasePath() + "/" + module
                        .getName())));

                menuShareProject.add(moduleItem);
            }

        } else {
            File dir = new File(project.getBasePath());
            for (File myDir : dir.listFiles()) {
                if (myDir.getName().startsWith(".") || myDir.isFile()) {
                    continue;
                }

                JMenuItem directoryItem = new JMenuItem(myDir.getName());
                directoryItem
                    .addActionListener(new ShareDirectoryAction(myDir));
                menuShareProject.add(directoryItem);
            }
        }

        add(menuShareProject);
    }

    /**
     * Action that is executed, when a project is selected for sharing.
     */
    private class ShareDirectoryAction implements ActionListener {
        private final File dir;

        private ShareDirectoryAction(File dir) {
            this.dir = dir;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                List<IResource> resources;

                IProject proj = workspace.getProject(dir.getName());
                proj.refreshLocal();

                resources = Arrays.asList((IResource) proj);

                JID user = new JID(contactInfo.getRosterEntry().getUser());
                List<JID> contacts = Arrays.asList(user);

                CollaborationUtils.startSession(resources, contacts);
            } catch (IOException exception) {
                LOG.error("could not share directory due to", exception);
            }
        }
    }
}
