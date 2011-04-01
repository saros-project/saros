package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * The icon of this action is used as visual indicator for the user if the
 * {@link StopManager} blocked the project.
 * 
 * Pressing the button has no effect for users, unless eclipse ist started with
 * assertions enabled. Then it is possible to manually unblock the project for
 * debugging purposes.
 */
@Component(module = "action")
public class StoppedAction extends Action {

    private static final Logger log = Logger.getLogger(StoppedAction.class);

    @Inject
    protected StopManager stopManager;
    protected ObservableValue<Boolean> isBlockedObservable;

    public StoppedAction() {
        setText("Stop Running Process");
        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ELCL_SAROS_SESSION_STOP_PROCESS
                    .getImageData();
            }
        });

        SarosPluginContext.initComponent(this);
        this.isBlockedObservable = stopManager.getBlockedObservable();

        isBlockedObservable.addAndNotify(new ValueChangeListener<Boolean>() {
            public void setValue(Boolean newValue) {
                setEnabled(newValue);
                setToolTipText(newValue ? "Project is stopped." : null);
            }
        });
    }

    @Override
    public void run() {
        // Attention: Assertion with side effect ahead.
        boolean isDebug = false;
        assert (isDebug = true) == true;
        if (isDebug) {
            log.warn("Manually unblocking project.");
            stopManager.lockProject(false);
        }
    }
}
