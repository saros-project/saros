package saros.intellij.ui.tree;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Pair;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.NotificationPanel;
import saros.net.xmpp.JID;

/**
 * Contact pop-up menu for selecting a project and module to share. Opened when right-clicking on a
 * contact.
 */
class ContactPopMenu extends JPopupMenu {

  private static final Logger LOG = Logger.getLogger(ContactPopMenu.class);

  private final ContactTreeRootNode.ContactInfo contactInfo;

  ContactPopMenu(ContactTreeRootNode.ContactInfo contactInfo) {
    this.contactInfo = contactInfo;

    JMenu menuShareProject = new JMenu("Work together on...");
    menuShareProject.setIcon(IconManager.SESSIONS_ICON);

    List<String> nonCompliantModules = new ArrayList<>();

    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      Pair<JMenu, List<String>> results = prepareProjectMenu(project);

      if (results == null) {
        return;
      }

      JMenu projectMenu = results.first;
      List<String> projectNonCompliantModules = results.second;

      menuShareProject.add(projectMenu);

      if (!projectNonCompliantModules.isEmpty()) {
        nonCompliantModules.addAll(projectNonCompliantModules);
      }
    }

    if (!nonCompliantModules.isEmpty()) {
      NotificationPanel.showWarning(
          MessageFormat.format(
              Messages.Contact_saros_message_conditional,
              MessageFormat.format(
                  Messages.ContactPopMenu_invalid_module_message_condition, nonCompliantModules)),
          Messages.ContactPopMenu_invalid_module_title);
    }

    add(menuShareProject);
  }

  /**
   * Prepares the JMenu for the given project. The menu contains all sharable modules contained in
   * the project.
   *
   * @param project the project whose menu to prepare
   * @return a <code>Pair</code> containing the prepared JMenu (first value) and a list of
   *     incompatible modules (second value) or <code>null</code> if the ModuleManager could not be
   *     instantiated
   */
  private Pair<JMenu, List<String>> prepareProjectMenu(@NotNull Project project) {
    JMenu projectMenu = new JMenu(project.getName());

    ModuleManager moduleManager = ModuleManager.getInstance(project);

    if (moduleManager == null) {

      NotificationPanel.showError(
          MessageFormat.format(
              Messages.Contact_saros_message_conditional,
              Messages.ContactPopMenu_unsupported_ide_message_condition),
          Messages.ContactPopMenu_unsupported_ide_title);

      return null;
    }

    List<JMenuItem> shownModules = new ArrayList<>();
    List<String> nonCompliantModules = new ArrayList<>();

    for (Module module : moduleManager.getModules()) {
      String moduleName = module.getName();
      String fullModuleName = project.getName() + File.separator + moduleName;

      if (project.getName().equalsIgnoreCase(moduleName)) {
        continue;
      }

      IProject wrappedModule;

      try {
        wrappedModule = new IntelliJProjectImpl(module);

      } catch (IllegalArgumentException exception) {
        LOG.debug(
            "Ignoring module "
                + fullModuleName
                + " as it does not meet the current release restrictions.");

        nonCompliantModules.add(fullModuleName);

        continue;

      } catch (IllegalStateException exception) {
        LOG.warn(
            "Ignoring module "
                + fullModuleName
                + " as an error "
                + "occurred while trying to create an IProject object.",
            exception);

        NotificationPanel.showWarning(
            MessageFormat.format(
                Messages.ContactPopMenu_error_creating_module_object_message,
                fullModuleName,
                exception),
            MessageFormat.format(
                Messages.ContactPopMenu_error_creating_module_object_title, fullModuleName));

        continue;
      }

      JMenuItem moduleItem = new JMenuItem(moduleName);
      moduleItem.addActionListener(new ShareDirectoryAction(moduleName, wrappedModule));

      shownModules.add(moduleItem);
    }

    if (!shownModules.isEmpty()) {
      shownModules.sort(Comparator.comparing(JMenuItem::getText));

      for (JMenuItem moduleItem : shownModules) {
        projectMenu.add(moduleItem);
      }
    } else {
      LOG.debug(
          "No modules shown to user as no modules "
              + (nonCompliantModules.isEmpty()
                  ? ""
                  : "complying with our current release restrictions ")
              + "were found");

      projectMenu.add(
          new JMenuItem(
              "No modules "
                  + (nonCompliantModules.isEmpty()
                      ? ""
                      : "complying with our current release restrictions ")
                  + " found!"));
    }

    return new Pair<>(projectMenu, nonCompliantModules);
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
                + " could not be created. This most likely means that the local Intellij instance "
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

      // TODO set the correct project for the session context
      CollaborationUtils.startSession(resources, contacts);
    }
  }
}
