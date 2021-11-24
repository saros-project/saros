package saros.ui.handlers.popup;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.viewers.ISelection;
import saros.SarosPluginContext;
import saros.communication.SkypeManager;
import saros.net.xmpp.contact.XMPPContact;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.util.selection.SelectionUtils;

/** An action for starting a Skype Audio Session to other contacts. */
public class SkypeHandler {

  public static final String ID = SkypeHandler.class.getName();

  private ISelectionListener selectionListener =
      new ISelectionListener() {

        @Override
        public void selectionChanged(MPart part, Object selection) {
          updateEnablement();
        }
      };

  @Inject private SkypeManager skypeManager;

  private MDirectMenuItem skypeMenuEntry;
  private ESelectionService selectionService;

  public SkypeHandler() {
    SarosPluginContext.initComponent(this);
  }

  @PostConstruct
  public void postConstruct(
      EModelService service, MPart sarosView, ESelectionService selectionService) {
    this.selectionService = selectionService;

    MPopupMenu popupMenu = null;
    for (MMenu menu : sarosView.getMenus()) {
      if (menu instanceof MPopupMenu) {
        popupMenu = (MPopupMenu) menu;
      }
    }

    MUIElement menuItem = service.find(ID, popupMenu);
    if (menuItem instanceof MDirectMenuItem) {
      skypeMenuEntry = (MDirectMenuItem) menuItem;
    }

    selectionService.addSelectionListener(selectionListener);
    updateEnablement();
  }

  private void updateEnablement() {
    if (skypeMenuEntry == null) {
      return;
    }

    skypeMenuEntry.setEnabled(getEnabledState());
  }

  @CanExecute
  public boolean canExecute() {
    return getEnabledState();
  }

  private boolean getEnabledState() {
    final List<XMPPContact> contacts =
        SelectionUtils.getAdaptableObjects(
            (ISelection) selectionService.getSelection(), XMPPContact.class);

    if (contacts.size() != 1) return false;

    final String skypeName = skypeManager.getSkypeName(contacts.get(0));

    return SkypeManager.isSkypeAvailable(false)
        && skypeName != null
        && !SkypeManager.isEchoService(skypeName);
  }

  @Execute
  public void run(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection activeSelection) {

    final List<XMPPContact> participants =
        SelectionUtils.getAdaptableObjects(activeSelection, XMPPContact.class);

    if (participants.size() != 1) return;

    final String skypeName = skypeManager.getSkypeName(participants.get(0));

    if (skypeName == null || SkypeManager.isEchoService(skypeName)) return;

    final String uri = SkypeManager.getAudioCallUri(skypeName);

    if (uri == null) return;

    final URLHyperlink link = new URLHyperlink(new Region(0, 0), uri);

    link.open();
  }

  @PreDestroy
  public void dispose(ESelectionService selectionService) {
    selectionService.removeSelectionListener(selectionListener);
  }
}
