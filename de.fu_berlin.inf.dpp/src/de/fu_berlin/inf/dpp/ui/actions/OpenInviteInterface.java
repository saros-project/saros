package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;

/**
 * Rename to OpenInvitationDialogAction
 */
@Component(module = "action")
public class OpenInviteInterface extends Action {

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            setEnabled(newSarosSession.isHost());
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            setEnabled(false);
        }
    };

    protected SarosSessionManager sessionManager;

    public OpenInviteInterface(SarosSessionManager sessionManager) {
        super();
        this.sessionManager = sessionManager;

        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ELCL_PROJECT_SHARE_ADD_BUDDIES
                    .getImageData();
            }
        });
        setToolTipText("Add Buddy(s) to Session");

        sessionManager.addSarosSessionListener(sessionListener);

        // Needed when the Interface is created during a session
        ISarosSession sarosSession = sessionManager.getSarosSession();
        setEnabled((sarosSession != null) && sarosSession.isHost());
    }

    @Override
    public void run() {
        WizardUtils.openShareProjectAddBuddiesWizard();
    }

}
