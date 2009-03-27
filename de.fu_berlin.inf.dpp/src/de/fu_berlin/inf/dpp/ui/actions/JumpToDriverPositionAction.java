package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class JumpToDriverPositionAction extends Action {

    private static final Logger log = Logger
        .getLogger(JumpToDriverPositionAction.class.getName());

    public JumpToDriverPositionAction() {
        setToolTipText("Jump to position of driver.");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/table_edit.png"));

        Saros.getDefault().getSessionManager().addSessionListener(

        new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject sharedProject) {
                updateEnablement();
            }

            @Override
            public void sessionEnded(ISharedProject sharedProject) {
                updateEnablement();
            }
        });
        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                EditorManager.getDefault().openDriverEditor();
            }
        });
    }

    private void updateEnablement() {
        setEnabled(Saros.getDefault().getSessionManager().getSharedProject() != null);
    }
}
