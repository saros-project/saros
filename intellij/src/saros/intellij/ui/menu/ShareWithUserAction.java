package saros.intellij.ui.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IResource;
import saros.intellij.context.SharedIDEContext;
import saros.intellij.filesystem.Filesystem;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.NotificationPanel;
import saros.net.xmpp.JID;

/**
 * An Action that starts a session when triggered.
 *
 * <p>Calls {@link CollaborationUtils#startSession(List, List)} with the selected module as
 * parameter.
 *
 * <p>This class assumes that the project is allowed to be shared (at the moment only completely
 * shared projects are implemented) and that the call to {@link
 * ShareWithUserAction#getModuleForVirtualFile(VirtualFile, Project)} is supported for this IDE
 * type.
 */
public class ShareWithUserAction extends AnAction {

  private static final Logger LOG = Logger.getLogger(ShareWithUserAction.class);

  private final JID userJID;
  private final String title;

  ShareWithUserAction(JID user, int userCount) {
    super(
        "_" + userCount + ": " + user.getRAW(),
        MessageFormat.format(Messages.ShareWithUserAction_description, user.getRAW()),
        IconManager.CONTACT_ONLINE_ICON);
    userJID = user;
    title = user.getName();
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = event.getProject();
    if (project == null) {
      throw new IllegalStateException(
          "Unable to start session - could not determine project for highlighted resource.");
    }

    VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
    if (virtualFile == null) {
      throw new IllegalStateException(
          "Unable to start session - could not determine virtual file for highlighted resource.");
    }

    IResource module;

    try {
      // We allow only completely shared projects, so no need to check
      // for partially shared ones.
      module = getModuleForVirtualFile(virtualFile, event.getProject());

    } catch (IllegalArgumentException e) {
      LOG.error("Tried to share illegal module", e);

      NotificationPanel.showError(
          MessageFormat.format(Messages.ShareWithUserAction_illegal_module_message, e.getMessage()),
          Messages.ShareWithUserAction_illegal_module_title);

      return;
    }

    List<IResource> resources = Arrays.asList(module);

    List<JID> contacts = Arrays.asList(userJID);

    SharedIDEContext.preregisterProject(project);
    CollaborationUtils.startSession(resources, contacts);
  }

  private IResource getModuleForVirtualFile(VirtualFile virtualFile, Project project) {
    ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);

    Module module = Filesystem.runReadAction(() -> projectFileIndex.getModuleForFile(virtualFile));

    if (module == null) {
      // FIXME: Find way to select moduleName for non-module based IDEAs (Webstorm)
      throw new UnsupportedOperationException();
    }

    return new IntelliJProjectImpl(module);
  }

  @Override
  public String toString() {
    return super.toString() + " " + title;
  }
}
