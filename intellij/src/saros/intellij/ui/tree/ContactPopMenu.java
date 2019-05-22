package saros.intellij.ui.tree;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import saros.SarosPluginContext;
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.NotificationPanel;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;

/**
 * Contact pop-up menu for selecting a project to share. Opened when right-clicking on a contact.
 */
class ContactPopMenu extends JPopupMenu {

  private static final Logger LOG = Logger.getLogger(ContactPopMenu.class);

  @Inject private IWorkspace workspace;

  @Inject private Project project;

  private final ContactTreeRootNode.ContactInfo contactInfo;

  ContactPopMenu(ContactTreeRootNode.ContactInfo contactInfo) {
    this.contactInfo = contactInfo;

    if (workspace == null || project == null) {
      SarosPluginContext.initComponent(this);

      if (workspace == null || project == null) {
        LOG.error("PicoContainer injection failed. Objects still not present after injection.");

        return;
      }
    }

    JMenu menuShareProject = new JMenu("Work together on...");
    menuShareProject.setIcon(IconManager.SESSIONS_ICON);

    ModuleManager moduleManager = ModuleManager.getInstance(project);

    if (moduleManager == null) {

      NotificationPanel.showError(
          MessageFormat.format(
              Messages.Contact_saros_message_conditional,
              Messages.ContactPopMenu_unsupported_ide_message_condition),
          Messages.ContactPopMenu_unsupported_ide_title);

      return;
    }

    List<JMenuItem> shownModules = new LinkedList<>();
    List<String> nonCompliantModules = new LinkedList<>();

    for (Module module : moduleManager.getModules()) {
      String moduleName = module.getName();

      if (project.getName().equalsIgnoreCase(moduleName)) {
        continue;
      }

      IProject wrappedModule;

      try {
        wrappedModule = workspace.getProject(moduleName);

      } catch (IllegalArgumentException exception) {
        LOG.debug(
            "Ignoring module "
                + moduleName
                + " as it does not meet the current release restrictions.");

        nonCompliantModules.add(moduleName);

        continue;

      } catch (IllegalStateException exception) {
        LOG.warn(
            "Ignoring module "
                + moduleName
                + " as an error "
                + "occurred while trying to create an IProject object.",
            exception);

        NotificationPanel.showWarning(
            MessageFormat.format(
                Messages.ContactPopMenu_error_creating_module_object_message,
                moduleName,
                exception),
            MessageFormat.format(
                Messages.ContactPopMenu_error_creating_module_object_title, moduleName));

        continue;
      }

      JMenuItem moduleItem = new JMenuItem(moduleName);
      moduleItem.addActionListener(new ShareDirectoryAction(moduleName, wrappedModule));

      shownModules.add(moduleItem);
    }

    if (!nonCompliantModules.isEmpty()) {
      NotificationPanel.showWarning(
          MessageFormat.format(
              Messages.Contact_saros_message_conditional,
              MessageFormat.format(
                  Messages.ContactPopMenu_invalid_module_message_condition, nonCompliantModules)),
          Messages.ContactPopMenu_invalid_module_title);
    }

    if (!shownModules.isEmpty()) {
      shownModules.sort(Comparator.comparing(JMenuItem::getText));

      for (JMenuItem moduleItem : shownModules) {
        menuShareProject.add(moduleItem);
      }
    } else {
      LOG.debug(
          "No modules shown to user as no modules "
              + (nonCompliantModules.isEmpty()
                  ? ""
                  : "complying with our current release restrictions ")
              + "were found");

      menuShareProject.add(
          new JMenuItem(
              "No modules "
                  + (nonCompliantModules.isEmpty()
                      ? ""
                      : "complying with our current release restrictions ")
                  + " found!"));
    }

    add(menuShareProject);
  }

  /** Action that is executed, when a project is selected for sharing. */
  private class ShareDirectoryAction implements ActionListener {
    private final String moduleName;
    private final IProject module;

    private ShareDirectoryAction(String moduleName, IProject module) {
      this.moduleName = moduleName;
      this.module = module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (module == null || !module.exists()) {
        LOG.error(
            "The IProject object for the module "
                + moduleName
                + " could not be created. This most likely means that the local IntelliJ instance "
                + "does not know any module with the given name.");

        NotificationPanel.showError(
            MessageFormat.format(
                Messages.Contact_saros_message_conditional,
                MessageFormat.format(
                    Messages.ContactPopMenu_module_not_found_message_condition, moduleName)),
            Messages.ContactPopMenu_module_not_found_title);

        return;
      }

      List<IResource> resources = new ArrayList<>();
      resources.add(module);

      JID user = new JID(contactInfo.getRosterEntry().getUser());
      List<JID> contacts = new ArrayList<>();
      contacts.add(user);

      CollaborationUtils.startSession(resources, contacts);
    }
  }
}
