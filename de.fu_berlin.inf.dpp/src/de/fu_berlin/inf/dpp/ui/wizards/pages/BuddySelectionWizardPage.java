package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManagerListener;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.BuddySelectionComposite;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BuddySelectionChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BuddySelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.FilterNonSarosBuddiesChangedEvent;

/**
 * Allows the user to select a {@link JID} from the {@link Roster}.
 * 
 * @author bkahlert
 */
public class BuddySelectionWizardPage extends WizardPage {
    private static final Logger LOG = Logger
        .getLogger(BuddySelectionWizardPage.class);

    protected static final String TITLE = Messages.BuddySelectionWizardPage_title;
    protected static final String DESCRIPTION = Messages.BuddySelectionWizardPage_description;

    protected static final String NO_BUDDY_SELECTED_ERROR_MESSAGE = Messages.BuddySelectionWizardPage_error_select_one_buddy;
    protected static final String OFFLINE_BUDDY_SELECTED_ERROR_MESSAGE = Messages.BuddySelectionWizardPage_error_selected_offline;
    protected static final String BUDDIES_WITHOUT_SAROS_SUPPORT_WARNING_MESSAGE = Messages.BuddySelectionWizardPage_warn_only_saros_buddies;

    protected BuddySelectionComposite buddySelectionComposite;

    /**
     * Flag indicating if this wizard page can be completed even if no contact
     * is selected
     */
    protected final boolean allowEmptyContactSelection;

    /**
     * This flag is true as soon as the user selected buddies without problems.
     */
    protected boolean selectionWasValid = false;

    @Inject
    protected IPreferenceStore preferenceStore;

    @Inject
    protected DiscoveryManager discoveryManager;

    /**
     * This {@link BuddySelectionListener} changes the {@link WizardPage}'s
     * state according to the selected {@link JID}s.
     */
    protected BuddySelectionListener buddySelectionListener = new BuddySelectionListener() {
        @Override
        public void buddySelectionChanged(BuddySelectionChangedEvent event) {
            updatePageCompletion();
        }

        @Override
        public void filterNonSarosBuddiesChanged(
            FilterNonSarosBuddiesChangedEvent event) {

            preferenceStore.setValue(
                PreferenceConstants.BUDDYSELECTION_FILTERNONSAROSBUDDIES,
                event.isFilterNonSarosBuddies());
        }
    };

    /**
     * This listener update the page completion if someone's presence changed.
     */
    protected DiscoveryManagerListener discoveryManagerListener = new DiscoveryManagerListener() {
        @Override
        public void featureSupportUpdated(final JID jid, String feature,
            boolean isSupported) {

            if (!Saros.NAMESPACE.equals(feature))
                return;

            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    if (BuddySelectionWizardPage.this.getControl().isDisposed())
                        return;

                    updatePageCompletion();
                }
            });
        }
    };

    public BuddySelectionWizardPage() {
        this(false);
    }

    public BuddySelectionWizardPage(boolean allowEmptyContactSelection) {
        super(BuddySelectionWizardPage.class.getName());
        setTitle(TITLE);
        setDescription(DESCRIPTION);

        SarosPluginContext.initComponent(this);
        discoveryManager.addDiscoveryManagerListener(discoveryManagerListener);
        this.allowEmptyContactSelection = allowEmptyContactSelection;
    }

    @Override
    public void dispose() {
        discoveryManager
            .removeDiscoveryManagerListener(discoveryManagerListener);
        super.dispose();
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(1, false));

        boolean initialFilterConfig = preferenceStore
            .getBoolean(PreferenceConstants.BUDDYSELECTION_FILTERNONSAROSBUDDIES);

        buddySelectionComposite = new BuddySelectionComposite(composite,
            SWT.BORDER | SWT.V_SCROLL, initialFilterConfig);

        /*
         * preset the contact(s) e.g from the Saros view when invoking 'Work
         * Together on...' context menu
         */
        buddySelectionComposite.setSelectedBuddies(SelectionRetrieverFactory
            .getSelectionRetriever(JID.class).getOverallSelection());

        buddySelectionComposite
            .addBuddySelectionListener(buddySelectionListener);

        buddySelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
            true, true));

        updatePageCompletion();
    }

    protected void updatePageCompletion() {

        List<JID> selectedBuddies = getSelectedBuddies();

        List<JID> selectedBuddiesWithSarosSupport = getSelectedBuddiesWithSarosSupport();

        if (allowEmptyContactSelection && selectedBuddies.isEmpty()) {
            setPageComplete(true);
            setErrorMessage(null);
        } else if (selectedBuddies.size() == 0) {
            if (selectionWasValid)
                setErrorMessage(NO_BUDDY_SELECTED_ERROR_MESSAGE);
            setPageComplete(false);
        } else if (!buddySelectionComposite.areAllSelectedOnline()) {
            setErrorMessage(OFFLINE_BUDDY_SELECTED_ERROR_MESSAGE);
            setPageComplete(false);
        } else {
            selectionWasValid = true;
            setErrorMessage(null);

            if (selectedBuddies.size() > selectedBuddiesWithSarosSupport.size()) {
                setMessage(BUDDIES_WITHOUT_SAROS_SUPPORT_WARNING_MESSAGE,
                    IMessageProvider.WARNING);
            } else {
                setMessage(DESCRIPTION);
            }

            setPageComplete(true);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (!visible)
            return;

        buddySelectionComposite.setFocus();
    }

    // WizardPage Results

    public List<JID> getSelectedBuddies() {
        if (buddySelectionComposite == null
            || buddySelectionComposite.isDisposed())
            return null;

        return buddySelectionComposite.getSelectedBuddies();
    }

    public List<JID> getSelectedBuddiesWithSarosSupport() {
        if (buddySelectionComposite == null
            || buddySelectionComposite.isDisposed())
            return null;

        return buddySelectionComposite.getSelectedBuddiesWithSarosSupport();
    }
}
