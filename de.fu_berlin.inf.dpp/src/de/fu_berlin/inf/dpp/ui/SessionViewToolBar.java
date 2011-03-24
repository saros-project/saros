package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.ui.actions.RestrictInviteesToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.RestrictToReadOnlyAccessAction;
import de.fu_berlin.inf.dpp.ui.actions.SendFileAction;
import de.fu_berlin.inf.dpp.ui.actions.StoppedAction;
import de.fu_berlin.inf.dpp.ui.actions.VideoSharingAction;
import de.fu_berlin.inf.dpp.ui.actions.VoIPAction;

/**
 * This is the ToolBar used by the SessionView
 */
@Component(module = "ui")
public class SessionViewToolBar implements Disposable {

    protected Saros saros;

    protected ViewPart sessionView;

    protected RestrictInviteesToReadOnlyAccessAction restrictInviteesToReadOnlyAccessAction;

    protected PreferenceUtils preferences;

    public SessionViewToolBar(
        Saros saros,
        ViewPart sessionView,
        StoppedAction stoppedAction,
        ConsistencyAction consistencyAction,
        RestrictToReadOnlyAccessAction restrictToReadOnlyAccess,
        RestrictInviteesToReadOnlyAccessAction restrictInviteesToReadOnlyAccessAction,
        FollowModeAction followModeAction, SendFileAction sendFileAction,
        VoIPAction voipAction, VideoSharingAction videoSharingAction,
        LeaveSessionAction leaveSessionAction, PreferenceUtils preferences) {

        this.saros = saros;
        this.sessionView = sessionView;
        this.restrictInviteesToReadOnlyAccessAction = restrictInviteesToReadOnlyAccessAction;
        this.preferences = preferences;

        // Create Toolbar
        IActionBars bars = sessionView.getViewSite().getActionBars();
        IToolBarManager toolBar = bars.getToolBarManager();

        toolBar.add(videoSharingAction);
        toolBar.add(sendFileAction);
        toolBar.add(voipAction);
        toolBar.add(stoppedAction);
        toolBar.add(consistencyAction);
        toolBar.add(restrictInviteesToReadOnlyAccessAction);
        toolBar.add(followModeAction);
        toolBar.add(leaveSessionAction);

    }

    public void dispose() {
        // empty
    }
}
