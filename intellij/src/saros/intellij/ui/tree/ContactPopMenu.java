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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IProject;
import saros.intellij.context.SharedIDEContext;
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

  private static final Logger log = Logger.getLogger(ContactPopMenu.class);

  private final ContactTreeRootNode.ContactInfo contactInfo;

  ContactPopMenu(ContactTreeRootNode.ContactInfo contactInfo) {
    this.contactInfo = contactInfo;

    JMenu menuShareProject = new JMenu(Messages.ContactPopMenu_root_popup_text);
    menuShareProject.setIcon(IconManager.SESSIONS_ICON);

    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      menuShareProject.add(createProjectMenu(project));
    }

    add(menuShareProject);
  }

  /**
   * Prepares the JMenu for the given project. The menu contains entries for all modules that belong
   * to the project. The entries are grouped by whether they are shareable or not. Non-shareable
   * entries are disabled and have a tooltip explaining why/that they are not sharable.
   *
   * <p>If the module information for the given project can not be read, a disabled menu entry is
   * returned instead. The entry has a tooltip explaining that there was an issue reading the module
   * information.
   *
   * @param project the project whose menu to prepare
   * @return the <code>JMenu</code> for the project or a disabled <code>JMenuEntry</code> containing
   *     only the project name if the module information could not be obtained for the project
   */
  @NotNull
  private JMenuItem createProjectMenu(@NotNull Project project) {

    Pair<List<JMenuItem>, List<JMenuItem>> moduleItems = createModuleEntries(project);

    if (moduleItems == null) {
      JMenuItem errorMessageItem = new JMenuItem(project.getName());
      errorMessageItem.setToolTipText(Messages.ContactPopMenu_menu_entry_error_processing_project);
      errorMessageItem.setEnabled(false);

      return errorMessageItem;
    }

    JMenu projectMenu = new JMenu(project.getName());

    List<JMenuItem> shownModules = moduleItems.first;
    List<JMenuItem> nonCompliantModules = moduleItems.second;

    if (!shownModules.isEmpty()) {
      shownModules.sort(Comparator.comparing(JMenuItem::getText));

      for (JMenuItem moduleItem : shownModules) {
        projectMenu.add(moduleItem);
      }

    } else {
      log.debug(
          "No modules shown to user as no modules "
              + (nonCompliantModules.isEmpty()
                  ? ""
                  : "complying with our current release restrictions ")
              + "were found");

      JMenuItem noModulesFoundMenuItem =
          new JMenuItem(
              nonCompliantModules.isEmpty()
                  ? Messages.ContactPopMenu_menu_entry_no_modules_found
                  : Messages.ContactPopMenu_menu_entry_no_valid_modules_found);

      noModulesFoundMenuItem.setEnabled(false);

      projectMenu.add(noModulesFoundMenuItem);
    }

    // Show non-compliant modules as non-sharable
    if (!nonCompliantModules.isEmpty()) {
      projectMenu.addSeparator();

      nonCompliantModules.sort(Comparator.comparing(JMenuItem::getText));

      for (JMenuItem nonCompliantModuleItem : nonCompliantModules) {
        projectMenu.add(nonCompliantModuleItem);
      }
    }

    return projectMenu;
  }

  /**
   * Creates the menu entries for the modules contained in the project, excluding the project
   * module. Returns the created entries grouped by whether the described module is shareable or
   * not.
   *
   * <p>Shareable module entries trigger the session negotiation when interacted with. Non-shareable
   * module entries are disabled and carry a tooltip explaining why the module can not be shared.
   *
   * @param project the project for whose modules to create menu entries
   * @return a <code>Pair</code> containing the entries for sharable modules (first element) and the
   *     entries for non-shareable modules (second element) or <code>null</code> if the module
   *     information for the project can not be read
   */
  @Nullable
  private Pair<List<JMenuItem>, List<JMenuItem>> createModuleEntries(Project project) {
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
    List<JMenuItem> nonCompliantModules = new ArrayList<>();

    for (Module module : moduleManager.getModules()) {
      String moduleName = module.getName();
      String fullModuleName = project.getName() + File.separator + moduleName;

      try {
        IProject wrappedModule = new IntelliJProjectImpl(module);

        JMenuItem moduleItem = new JMenuItem(moduleName);
        moduleItem.setToolTipText(Messages.ContactPopMenu_menu_tooltip_share_module);
        moduleItem.addActionListener(new ShareDirectoryAction(project, moduleName, wrappedModule));

        shownModules.add(moduleItem);

      } catch (IllegalArgumentException exception) {
        log.debug(
            "Ignoring module "
                + fullModuleName
                + " as it does not meet the current release restrictions.");

        JMenuItem invalidModuleEntry = new JMenuItem(moduleName);
        invalidModuleEntry.setEnabled(false);
        invalidModuleEntry.setToolTipText(Messages.ContactPopMenu_menu_tooltip_invalid_module);

        nonCompliantModules.add(invalidModuleEntry);
      }
    }

    return new Pair<>(shownModules, nonCompliantModules);
  }

  /** Action that is executed, when a project is selected for sharing. */
  private class ShareDirectoryAction implements ActionListener {
    private final Project project;
    private final String moduleName;
    private final IProject module;

    private ShareDirectoryAction(
        @NotNull Project project, @NotNull String moduleName, @Nullable IProject module) {

      this.project = project;
      this.moduleName = moduleName;
      this.module = module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (module == null || !module.exists()) {
        log.error(
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

      Set<IProject> projects = Collections.singleton(module);
      List<JID> contacts = Collections.singletonList(contactInfo.getJid());

      SharedIDEContext.preregisterProject(project);
      CollaborationUtils.startSession(projects, contacts);
    }
  }
}
