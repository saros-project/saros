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
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.intellij.ui.util.SafeDialogUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        if(moduleManager == null){

            NotificationPanel.showError("The local module manager " +
                "could not be found. This most likely means that you are not " +
                "using IntelliJ IDEA or are using an unsupported version.\n" +
                "If you are using a supported version of IntelliJ IDEA," +
                "please contact the Saros development team. You can reach us " +
                "by  writing to our mailing list " +
                "(saros-devel@googlegroups.com) or by using our contact form " +
                "(https://www.saros-project.org/contact/Website%20feedback).",
                "Unsupported IDE");

            return;
        }

        for (Module module : moduleManager.getModules()) {
            String moduleName = module.getName();

            if (project.getName().equalsIgnoreCase(moduleName)) {
                continue;
            }

            JMenuItem moduleItem = new JMenuItem(moduleName);
            moduleItem.addActionListener(
                new ShareDirectoryAction(moduleName));

            menuShareProject.add(moduleItem);
        }

        add(menuShareProject);
    }

    /**
     * Action that is executed, when a project is selected for sharing.
     */
    private class ShareDirectoryAction implements ActionListener {
        private final String moduleName;

        private ShareDirectoryAction(String moduleName) {
            this.moduleName = moduleName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            List<IResource> resources;

            IProject module;

            try {
                module = workspace.getProject(moduleName);
            } catch(IllegalArgumentException exception) {
                LOG.debug("No session is started as an invalid module was "
                    + "chosen", exception);

                SafeDialogUtils.showError("The chosen module can not be " +
                    "shared through Saros. This is probably due to the " +
                    "module not meeting the current restrictions. Modules " +
                    "should have exactly one content root that is located in " +
                    "the project root directory. Please select a  valid " +
                    "module.\n"+
                    "If the chosen module meets the given restrictions, " +
                    "please contact the Saros development team.You can reach " +
                    "us by writing to our mailing list " +
                    "(saros-devel@googlegroups.com) or by using our contact " +
                    "form " +
                    "(https://www.saros-project.org/contact/Website%20feedback).",
                    "Invalid module chosen!");

                return;
            }

            if (module == null) {
                LOG.error("The IProject object for the module " + moduleName
                    + " could not be created. This most likely means that the"
                    + " local IntelliJ instance does not know any module with"
                    + " the given name.");

                NotificationPanel.showError("Saros could not find " +
                    "the chosen module " + moduleName + ". Please make sure " +
                    "that the module is correctly configured in the current " +
                    "project and exists on disk.\n" +
                    "If there seems to be no problem with the module, please " +
                    "contact the Saros development team. You can reach us by " +
                    "writing to our mailing list " +
                    "(saros-devel@googlegroups.com) or by using our contact " +
                    "form " +
                    "(https://www.saros-project.org/contact/Website%20feedback).",
                    "Error - Project sharing aborted");

                return;
            }

            resources = Arrays.asList((IResource) module);

            JID user = new JID(contactInfo.getRosterEntry().getUser());
            List<JID> contacts = Arrays.asList(user);

            CollaborationUtils.startSession(resources, contacts);
        }
    }
}
