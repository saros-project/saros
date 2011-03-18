package de.fu_berlin.inf.dpp.ui.widgets.session;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.ui.sarosView.SessionViewTableViewer;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.BuddyDisplayComposite;

public class RosterSessionComposite extends Composite {

    private SessionComposite sessionComposite;
    private BuddyDisplayComposite buddyDisplayComposite;

    public RosterSessionComposite(Composite parent, int style) {
        super(parent, style);

        SarosPluginContext.initComponent(this);
        this.setBackgroundMode(SWT.INHERIT_DEFAULT);
        final GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginTop = 0;
        this.setLayout(gridLayout);

        /*
         * Session
         */

        Group sessionGroup = new Group(this, SWT.SHADOW_OUT);
        sessionGroup
            .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        sessionGroup.setLayout(new FillLayout());
        sessionGroup.setText("Session");
        sessionGroup.setBackgroundMode(SWT.INHERIT_DEFAULT);
        this.sessionComposite = new SessionComposite(sessionGroup, SWT.NONE);

        /*
         * Roster
         */

        Group rosterGroup = new Group(this, SWT.SHADOW_OUT);
        rosterGroup.setLayout(LayoutUtils.createGridLayout());
        rosterGroup.setBackgroundMode(SWT.INHERIT_DEFAULT);
        rosterGroup.setText("Buddies");
        rosterGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.buddyDisplayComposite = new BuddyDisplayComposite(rosterGroup,
            SWT.MULTI | SWT.NO_SCROLL | SWT.BORDER);
        this.buddyDisplayComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.FILL, true, true));
    }

    public SessionViewTableViewer getSessionViewer() {
        return sessionComposite.getSessionViewer();
    }

    public Viewer getBuddyViewer() {
        return this.buddyDisplayComposite.getViewer();
    }

}
