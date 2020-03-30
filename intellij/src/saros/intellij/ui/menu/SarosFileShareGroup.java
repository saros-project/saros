package saros.intellij.ui.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.SarosPluginContext;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;

/** Saros action group for the pop-up menu when right-clicking on a module. */
public class SarosFileShareGroup extends ActionGroup {
  private static final Logger log = Logger.getLogger(SarosFileShareGroup.class);

  @Inject private ISarosSessionManager sessionManager;

  @Inject private XMPPContactsService contactsService;

  @Override
  public void actionPerformed(AnActionEvent e) {
    // do nothing when menu pops-up
  }

  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    // This has to be initialized here, because doing it in the
    // constructor would be too early. The lifecycle is not
    // running yet when this class is instantiated.
    // To make the dependency injection work,
    // SarosPluginContext.initComponent has to be called here.
    if (sessionManager == null && contactsService == null) {
      SarosPluginContext.initComponent(this);
    }

    if (e == null || sessionManager.getSession() != null) {
      return new AnAction[0];
    }

    if (!isSharableResource(e)) {
      return new AnAction[0];
    }

    int userCount = 1;
    List<AnAction> list = new ArrayList<>();
    for (XMPPContact contact : contactsService.getAllContacts()) {
      if (contact.getStatus().isOnline()) {
        list.add(new ShareWithUserAction(contact.getBareJid(), userCount));
        userCount++;
      }
    }

    return list.toArray(new AnAction[0]);
  }

  private boolean isSharableResource(AnActionEvent e) {
    // FIXME also returns null when a module with multiple roots is selected
    // Don't allow to share any file or folder other than a module
    if (e.getDataContext().getData(LangDataKeys.MODULE_CONTEXT.getName()) == null) {
      return false;
    }

    Project project = e.getData(LangDataKeys.PROJECT);
    Module module = e.getData(LangDataKeys.MODULE);

    if (project == null || module == null || project.getName().equalsIgnoreCase(module.getName())) {

      return false;
    }

    String moduleName = module.getName();

    try {
      new IntelliJProjectImpl(module);

    } catch (IllegalArgumentException exception) {
      if (log.isTraceEnabled()) {
        log.trace(
            "Ignoring module "
                + moduleName
                + " as it does not meet the current release restrictions.");
      }

      return false;
    }

    return true;
  }
}
