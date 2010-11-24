package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
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

    protected StopManager stopManager;
    protected ObservableValue<Boolean> isBlockedObservable;

    public StoppedAction(StopManager stopManager) {
        // TODO A small stop traffic sign would look better. IMHO.
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_ELCL_STOP));
        this.stopManager = stopManager;
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
