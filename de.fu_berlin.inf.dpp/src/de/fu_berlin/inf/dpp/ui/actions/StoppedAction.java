package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.util.ObservableValue;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.ValueChangeListener;

/**
 * The icon of this action is used as visual indicator for the user if the
 * {@link StopManager} blocked the session.
 * 
 * Pressing the button has no effect for users, unless eclipse is started with
 * assertions enabled. Then it is possible to manually unblock the session for
 * debugging purposes.
 */
@Component(module = "action")
public class StoppedAction extends Action implements Disposable {

    private static final Logger log = Logger.getLogger(StoppedAction.class);

    protected ObservableValue<Boolean> isBlockedObservable;

    @Inject
    protected ISarosSessionManager sessionManager;

    /**
     * Register to the StopManager of the new session.
     */
    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            isBlockedObservable = newSarosSession.getStopManager()
                .getBlockedObservable();
            isBlockedObservable
                .addAndNotify(new ValueChangeListener<Boolean>() {
                    public void setValue(Boolean newValue) {
                        setUnblockEnabled(newValue);
                    }
                });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            isBlockedObservable = null;
            setUnblockEnabled(false);
        }
    };

    public StoppedAction() {
        setEnabled(false);
        setText(Messages.StoppedAction_title);
        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ELCL_SAROS_SESSION_STOP_PROCESS
                    .getImageData();
            }
        });

        SarosPluginContext.initComponent(this);
        sessionManager.addSarosSessionListener(sessionListener);
    }

    void setUnblockEnabled(final boolean blocked) {
        Utils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                setEnabled(blocked);
                setToolTipText(blocked ? Messages.StoppedAction_tooltip : null);
            }
        });
    }

    @Override
    public void run() {
        // The Action should be disabled when there is no session.
        assert sessionManager.getSarosSession() != null;

        // Attention: Assertion with side effect ahead.
        boolean isDebug = false;
        assert (isDebug = true) == true;
        if (isDebug && sessionManager.getSarosSession() != null) {
            log.warn("Manually unblocking session."); //$NON-NLS-1$
            sessionManager.getSarosSession().getStopManager()
                .lockSession(false);
        }
    }

    @Override
    public void dispose() {
        sessionManager.removeSarosSessionListener(sessionListener);
    }
}
