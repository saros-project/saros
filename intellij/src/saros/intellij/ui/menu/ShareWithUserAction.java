package saros.intellij.ui.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IReferencePoint;
import saros.intellij.context.SharedIDEContext;
import saros.intellij.filesystem.IntellijReferencePoint;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.NotificationPanel;
import saros.net.xmpp.JID;

/**
 * An Action that starts a session when triggered.
 *
 * <p>Calls {@link CollaborationUtils#startSession(Set, List)} with the selected directory as
 * parameter.
 */
public class ShareWithUserAction extends AnAction {

  private static final Logger log = Logger.getLogger(ShareWithUserAction.class);

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
          "Unable to start session - could not determine project for highlighted directory.");
    }

    VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
    if (virtualFile == null) {
      throw new IllegalStateException(
          "Unable to start session - could not determine virtual file for highlighted directory.");
    }

    IReferencePoint referencePoint;

    try {
      referencePoint = new IntellijReferencePoint(project, virtualFile);

    } catch (IllegalArgumentException e) {
      log.error("Failed to create reference point for " + virtualFile + " in " + project, e);

      NotificationPanel.showError(
          MessageFormat.format(
              Messages.ShareWithUserAction_failed_to_share_directory_message, e.getMessage()),
          Messages.ShareWithUserAction_failed_to_share_directory_title);

      return;
    }

    Set<IReferencePoint> referencePoints = new HashSet<>();
    referencePoints.add(referencePoint);

    List<JID> contacts = Collections.singletonList(userJID);

    SharedIDEContext.preregisterProject(project);
    CollaborationUtils.startSession(referencePoints, contacts);
  }

  @Override
  public String toString() {
    return super.toString() + " " + title;
  }
}
