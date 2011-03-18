package de.fu_berlin.inf.dpp.ui.sarosView;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.actions.ChangeXMPPAccountAction;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.ui.actions.IMBeepAction;
import de.fu_berlin.inf.dpp.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.ui.actions.NewContactAction;
import de.fu_berlin.inf.dpp.ui.actions.OpenInviteInterface;
import de.fu_berlin.inf.dpp.ui.actions.RestrictInviteesToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.RestrictToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.StoppedAction;
import de.fu_berlin.inf.dpp.ui.actions.VoIPAction;

/**
 * This is the ToolBar used by the {@link SarosView}
 * 
 * @author patbit
 */
@Component(module = "ui")
public class SarosViewToolbar implements Disposable {

    protected Saros saros;

    protected ViewPart sarosView;

    protected RestrictInviteesToReadOnlyAccessAction removeAllDriverRoleAction;

    protected PreferenceUtils preferences;

    /*
     * TODO Actions are created by PicoContainer for now. See TODO in SarosView
     */
    public SarosViewToolbar(Saros saros, ViewPart sarosView,
        StoppedAction stoppedAction, ConsistencyAction consistencyAction,
        OpenInviteInterface openInvitationInterfaceAction,
        RestrictToReadOnlyAccessAction removeDriverRoleAction,
        RestrictInviteesToReadOnlyAccessAction removeAllDriverRoleAction,
        FollowModeAction followModeAction, VoIPAction voipAction,
        LeaveSessionAction leaveSessionAction,
        NewContactAction newContactAction,
        ChangeXMPPAccountAction changeXMPPAccountAction,
        IMBeepAction iMBeepAction, PreferenceUtils preferences) {

        this.saros = saros;
        this.sarosView = sarosView;
        this.removeAllDriverRoleAction = removeAllDriverRoleAction;
        this.preferences = preferences;

        // Create Toolbar
        IActionBars bars = sarosView.getViewSite().getActionBars();
        IToolBarManager toolBar = bars.getToolBarManager();

        toolBar.add(changeXMPPAccountAction);
        toolBar.add(newContactAction);
        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBar.add(stoppedAction);
        toolBar.add(consistencyAction);
        toolBar.add(openInvitationInterfaceAction);
        toolBar.add(followModeAction);
        toolBar.add(leaveSessionAction);
        toolBar.add(iMBeepAction);
    }

    public void dispose() {
        // empty
    }

}
