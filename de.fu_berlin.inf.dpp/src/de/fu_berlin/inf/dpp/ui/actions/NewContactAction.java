package de.fu_berlin.inf.dpp.ui.actions;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Inject;

public class NewContactAction extends Action implements Disposable {

  public static final String ACTION_ID = NewContactAction.class.getName();

  @Inject private XMPPConnectionService sarosNet;

  private final IConnectionListener connectionListener =
      new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection, ConnectionState state) {
          setEnabled(sarosNet.isConnected());
        }
      };

  public NewContactAction() {
    setId(ACTION_ID);
    setToolTipText(Messages.NewContactAction_tooltip);
    setImageDescriptor(
        new ImageDescriptor() {
          @Override
          public ImageData getImageData() {
            return ImageManager.ELCL_CONTACT_ADD.getImageData();
          }
        });

    SarosPluginContext.initComponent(this);

    sarosNet.addListener(connectionListener);
    setEnabled(sarosNet.isConnected());
  }

  @Override
  public void dispose() {
    sarosNet.removeListener(connectionListener);
  }

  @Override
  public void run() {
    WizardUtils.openAddContactWizard();
  }
}
