package saros.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.ConnectionState;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.WizardUtils;

public class NewContactAction extends Action implements Disposable {

  public static final String ACTION_ID = NewContactAction.class.getName();

  private final IConnectionStateListener connectionListener =
      (state, error) -> setEnabled(state == ConnectionState.CONNECTED);

  @Inject private ConnectionHandler connectionHandler;

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

    connectionHandler.addConnectionStateListener(connectionListener);
    setEnabled(connectionHandler.isConnected());
  }

  @Override
  public void dispose() {
    connectionHandler.removeConnectionStateListener(connectionListener);
  }

  @Override
  public void run() {
    WizardUtils.openAddContactWizard();
  }
}
