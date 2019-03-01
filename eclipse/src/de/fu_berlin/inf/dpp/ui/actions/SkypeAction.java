package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.SkypeManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

/**
 * A action for skyping other JIDs.
 *
 * @author rdjemili
 */
@Component(module = "net")
public class SkypeAction extends Action implements Disposable {

  public static final String ACTION_ID = SkypeAction.class.getName();

  private static final Logger LOG = Logger.getLogger(SkypeAction.class);

  protected IPropertyChangeListener propertyChangeListener =
      new IPropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
          if (event.getProperty().equals(PreferenceConstants.SKYPE_USERNAME)) {
            updateEnablement();
          }
        }
      };

  protected ISelectionListener selectionListener =
      new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
          updateEnablement();
        }
      };

  @Inject protected SkypeManager skypeManager;

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

  public void updateEnablement() {
    try {
      List<JID> contacts =
          SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();
      setEnabled(
          contacts.size() == 1
              && skypeManager.getSkypeURLNonBlock(contacts.get(0).getBareJID()) != null);
    } catch (NullPointerException e) {
      this.setEnabled(false);
    } catch (Exception e) {
      if (!PlatformUI.getWorkbench().isClosing())
        LOG.error("Unexpected error while updating enablement", e); // $NON-NLS-1$
    }
  }

  @Override
  public void run() {

    final List<JID> participants =
        SelectionRetrieverFactory.getSelectionRetriever(JID.class).getSelection();

    if (participants.size() != 1) return;

    ThreadUtils.runSafeAsync(
        "SkypeAction",
        LOG,
        new Runnable() { //$NON-NLS-1$
          @Override
          public void run() {
            SWTUtils.runSafeSWTSync(
                LOG,
                new Runnable() {
                  @Override
                  public void run() {
                    setEnabled(false);
                  }
                });
            final String skypeURL = skypeManager.getSkypeURL(participants.get(0).getBareJID());
            if (skypeURL != null) {
              SWTUtils.runSafeSWTSync(
                  LOG,
                  new Runnable() {
                    @Override
                    public void run() {
                      URLHyperlink link = new URLHyperlink(new Region(0, 0), skypeURL);
                      link.open();
                    }
                  });
            }
          }
        });
  }

  @Override
  public void dispose() {
    SelectionUtils.getSelectionService().removeSelectionListener(selectionListener);
  }
}
