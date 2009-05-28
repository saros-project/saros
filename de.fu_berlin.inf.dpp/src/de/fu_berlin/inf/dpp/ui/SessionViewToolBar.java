package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.ui.actions.FollowModeAction;
import de.fu_berlin.inf.dpp.ui.actions.LeaveSessionAction;
import de.fu_berlin.inf.dpp.ui.actions.OpenInviteInterface;
import de.fu_berlin.inf.dpp.ui.actions.RemoveAllDriverRoleAction;
import de.fu_berlin.inf.dpp.ui.actions.RemoveDriverRoleAction;

/**
 * This is the ToolBar used by the SessionView
 */
@Component(module = "ui")
public class SessionViewToolBar implements Disposable {

    protected Saros saros;

    protected ViewPart sessionView;

    protected RemoveAllDriverRoleAction removeAllDriverRoleAction;

    protected PreferenceUtils preferences;

    public SessionViewToolBar(Saros saros, ViewPart sessionView,
        ConsistencyAction consistencyAction,
        OpenInviteInterface openInvitationInterfaceAction,
        RemoveDriverRoleAction removeDriverRoleAction,
        RemoveAllDriverRoleAction removeAllDriverRoleAction,
        FollowModeAction followModeAction,
        LeaveSessionAction leaveSessionAction, PreferenceUtils preferences) {

        this.saros = saros;
        this.sessionView = sessionView;
        this.removeAllDriverRoleAction = removeAllDriverRoleAction;
        this.preferences = preferences;

        /**
         * Register for our preference store, so we can be notified if the
         * Multi-Driver setting changes
         */
        saros.getPreferenceStore().addPropertyChangeListener(
            multiDriverPrefsListener);

        // Create Toolbar
        IActionBars bars = sessionView.getViewSite().getActionBars();
        IToolBarManager toolBar = bars.getToolBarManager();

        toolBar.add(consistencyAction);
        toolBar.add(openInvitationInterfaceAction);
        if (preferences.isMultiDriverEnabled()) {
            toolBar.add(removeAllDriverRoleAction);
        }
        toolBar.add(followModeAction);
        toolBar.add(leaveSessionAction);

    }

    protected IPropertyChangeListener multiDriverPrefsListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(PreferenceConstants.MULTI_DRIVER)) {
                updateMultiDriverActions();
            }
        }

        private void updateMultiDriverActions() {
            IViewSite site = sessionView.getViewSite();

            // Check if the site exists (may be null when disposed or starting)
            if (site == null)
                return;

            IActionBars bars = site.getActionBars();
            IToolBarManager toolBar = bars.getToolBarManager();

            if (preferences.isMultiDriverEnabled()) {
                toolBar.insertBefore(FollowModeAction.ACTION_ID,
                    removeAllDriverRoleAction);
            } else {
                toolBar.remove(RemoveAllDriverRoleAction.ACTION_ID);
            }
            toolBar.update(false);
        }
    };

    public void dispose() {
        saros.getPreferenceStore().removePropertyChangeListener(
            multiDriverPrefsListener);
    }
}
