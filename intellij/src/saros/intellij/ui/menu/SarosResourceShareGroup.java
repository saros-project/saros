package saros.intellij.ui.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.SarosPluginContext;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;

/** Saros action group for the pop-up menu when right-clicking on a resource. */
public class SarosResourceShareGroup extends ActionGroup {
  @Inject private ISarosSessionManager sessionManager;

  @Inject private XMPPContactsService contactsService;

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
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

    List<XMPPContact> contacts = new ArrayList<>(contactsService.getAllContacts());
    contacts.sort(Comparator.comparing(contact -> contact.getBareJid().toString()));

    int userCount = 1;
    List<AnAction> list = new ArrayList<>();
    for (XMPPContact contact : contacts) {
      if (contact.getStatus().isOnline()) {
        list.add(new ShareWithUserAction(contact.getBareJid(), userCount));
        userCount++;
      }
    }

    return list.toArray(new AnAction[0]);
  }

  /**
   * Returns whether the selected element is a sharable resource.
   *
   * <p>Any directory is sharable.
   *
   * @param e the action event
   * @return whether the selected element is a sharable resource
   */
  // TODO exclude excluded resources
  private boolean isSharableResource(AnActionEvent e) {
    VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

    return virtualFile != null && virtualFile.isDirectory();
  }
}
