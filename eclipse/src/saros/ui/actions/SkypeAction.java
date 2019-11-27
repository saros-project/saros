package saros.ui.actions;

import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import saros.SarosPluginContext;
import saros.communication.SkypeManager;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.selection.SelectionUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;

/** An action for starting a Skype Audio Session to other contacts. */
public class SkypeAction extends Action implements Disposable {

  public static final String ACTION_ID = SkypeAction.class.getName();

  private ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  @Inject private SkypeManager skypeManager;

  public SkypeAction() {
    super(Messages.SkypeAction_title);

    SarosPluginContext.initComponent(this);

    setId(ACTION_ID);
    setToolTipText(Messages.SkypeAction_tooltip);
    setImageDescriptor(
        new ImageDescriptor() {
          @Override
          public ImageData getImageData() {
            return ImageManager.ELCL_CONTACT_SKYPE_CALL.getImageData();
          }
        });

    SelectionUtils.getSelectionService().addSelectionListener(selectionListener);
    updateEnablement();
  }

  private void updateEnablement() {

    setEnabled(false);

    final List<JID> contacts =
        SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    if (contacts.size() != 1) return;

    final String skypeName = skypeManager.getSkypeName(contacts.get(0));

    setEnabled(
        SkypeManager.isSkypeAvailable(false)
            && skypeName != null
            && !SkypeManager.isEchoService(skypeName));
  }

  @Override
  public void run() {

    final List<JID> participants =
        SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    if (participants.size() != 1) return;

    final String skypeName = skypeManager.getSkypeName(participants.get(0));

    if (skypeName == null || SkypeManager.isEchoService(skypeName)) return;

    final String uri = SkypeManager.getAudioCallUri(skypeName);

    if (uri == null) return;

    final URLHyperlink link = new URLHyperlink(new Region(0, 0), uri);

    link.open();
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }
}
