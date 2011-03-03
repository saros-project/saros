package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.model.ITreeElement;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterCheckStateProvider;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BaseBuddySelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BuddySelectionChangedEvent;
import de.fu_berlin.inf.dpp.util.ArrayUtils;

/**
 * This {@link Composite} extends {@link BuddyDisplayComposite} and allows to
 * check (via check boxes) {@link RosterEntry}s.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link StructuredViewer}</dd>
 * <dd>SWT.CHECK is used by default</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author bkahlert
 * 
 */
public class BaseBuddySelectionComposite extends BuddyDisplayComposite {

    RosterCheckStateProvider checkStateProvider;

    protected List<BaseBuddySelectionListener> buddySelectionListeners = new ArrayList<BaseBuddySelectionListener>();

    protected ICheckStateListener checkStateListener = new ICheckStateListener() {
        public void checkStateChanged(CheckStateChangedEvent event) {
            // Update the check state
            if (checkStateProvider != null) {
                checkStateProvider.setChecked(event.getElement(),
                    event.getChecked());
            }

            // Fire selection event
            JID jid = (JID) Platform.getAdapterManager().getAdapter(
                event.getElement(), JID.class);
            if (jid != null)
                notifyBuddySelectionChanged(jid, event.getChecked());
        }
    };

    public BaseBuddySelectionComposite(Composite parent, int style) {
        super(parent, style | SWT.CHECK);

        ((CheckboxTreeViewer) this.viewer)
            .addCheckStateListener(checkStateListener);

        this.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (viewer != null) {
                    ((CheckboxTreeViewer) viewer)
                        .removeCheckStateListener(checkStateListener);
                }
            }
        });
    }

    @Override
    public void createViewer(int style) {
        /*
         * The normal CheckboxTreeViewer does not preserve the checkbox states.
         * We therefore use a workaround class.
         */
        this.viewer = new CheckboxTreeViewer(new Tree(this, style));
    }

    @Override
    protected void configureViewer() {
        super.configureViewer();
        this.checkStateProvider = new RosterCheckStateProvider();
        ((CheckboxTreeViewer) this.viewer)
            .setCheckStateProvider(this.checkStateProvider);
    }

    /**
     * Sets the currently selected {@link JID}s.
     * 
     * @param buddies
     */
    public void setSelectedBuddies(List<JID> buddies) {
        CheckboxTreeViewer treeViewer = (CheckboxTreeViewer) this.viewer;

        List<RosterEntryElement> allElements = collectAllRosterEntryElement(treeViewer);
        List<RosterEntryElement> checkedElements = ArrayUtils
            .getAdaptableObjects(treeViewer.getCheckedElements(),
                RosterEntryElement.class);
        List<RosterEntryElement> elementsToCheck = new ArrayList<RosterEntryElement>();
        for (JID buddy : buddies) {
            elementsToCheck.add(new RosterEntryElement(this.saros.getRoster(),
                buddy));
        }

        Map<RosterEntryElement, Boolean> checkStatesChanges = calculateCheckStateDiff(
            allElements, checkedElements, elementsToCheck);

        /*
         * Update the check state in the RosterCheckStateProvider
         */
        for (RosterEntryElement rosterEntryElement : checkStatesChanges
            .keySet()) {
            boolean checked = checkStatesChanges.get(rosterEntryElement);
            this.checkStateProvider.setChecked(rosterEntryElement, checked);
        }

        /*
         * Refresh the viewer in order to reflect the new check states.
         */
        treeViewer.refresh();

        /*
         * Fire events
         */
        for (RosterEntryElement rosterEntryElement : checkStatesChanges
            .keySet()) {
            boolean checked = checkStatesChanges.get(rosterEntryElement);
            this.notifyBuddySelectionChanged(
                (JID) rosterEntryElement.getAdapter(JID.class), checked);
        }
    }

    /**
     * Calculates from a given set of {@link RosterEntryElement}s which
     * {@link RosterEntryElement}s change their check state.
     * 
     * @param allRosterEntryElements
     * @param checkedRosterEntryElement
     *            {@link RosterEntryElement}s which are already checked
     * @param rosterEntryElementToCheck
     *            {@link RosterEntryElement}s which have to be exclusively
     *            checked
     * @return {@link Map} of {@link RosterEntryElement} that must change their
     *         check state
     */
    protected Map<RosterEntryElement, Boolean> calculateCheckStateDiff(
        List<RosterEntryElement> allRosterEntryElements,
        List<RosterEntryElement> checkedRosterEntryElement,
        List<RosterEntryElement> rosterEntryElementToCheck) {

        Map<RosterEntryElement, Boolean> checkStatesChanges = new HashMap<RosterEntryElement, Boolean>();
        for (RosterEntryElement rosterEntryElement : allRosterEntryElements) {
            if (rosterEntryElementToCheck.contains(rosterEntryElement)
                && !checkedRosterEntryElement.contains(rosterEntryElement)) {
                checkStatesChanges.put(rosterEntryElement, true);
            } else if (!rosterEntryElementToCheck.contains(rosterEntryElement)
                && checkedRosterEntryElement.contains(rosterEntryElement)) {
                checkStatesChanges.put(rosterEntryElement, false);
            }
        }

        return checkStatesChanges;
    }

    /**
     * Returns the currently selected {@link JID}s.
     * 
     * @return
     */
    public List<JID> getSelectedBuddies() {
        List<JID> buddies = new ArrayList<JID>();
        for (Object element : ((CheckboxTreeViewer) this.viewer)
            .getCheckedElements()) {
            JID buddy = (JID) ((ITreeElement) element).getAdapter(JID.class);
            if (buddy != null)
                buddies.add(buddy);
        }
        return buddies;
    }

    /**
     * Returns the currently selected {@link JID}s that support Saros.
     * 
     * @return
     */
    public List<JID> getSelectedBuddiesWithSarosSupport() {
        List<JID> buddies = new ArrayList<JID>();
        for (Object element : ((CheckboxTreeViewer) this.viewer)
            .getCheckedElements()) {
            JID buddy = (JID) ((ITreeElement) element).getAdapter(JID.class);
            boolean isSarosSupported = element instanceof RosterEntryElement
                && ((RosterEntryElement) element).isSarosSupported();

            if (buddy != null && isSarosSupported)
                buddies.add(buddy);
        }
        return buddies;
    }

    /**
     * Adds a {@link BaseBuddySelectionListener}
     * 
     * @param buddySelectionListener
     */
    public void addBuddySelectionListener(
        BaseBuddySelectionListener buddySelectionListener) {
        this.buddySelectionListeners.add(buddySelectionListener);
    }

    /**
     * Removes a {@link BaseBuddySelectionListener}
     * 
     * @param buddySelectionListener
     */
    public void removeBuddySelectionListener(
        BaseBuddySelectionListener buddySelectionListener) {
        this.buddySelectionListeners.remove(buddySelectionListener);
    }

    /**
     * Notify all {@link BaseBuddySelectionListener}s about a changed selection.
     * 
     * @param jid
     *            of the buddy who's selection changed
     * @param isSelected
     *            new selection state
     */
    public void notifyBuddySelectionChanged(JID jid, boolean isSelected) {
        BuddySelectionChangedEvent event = new BuddySelectionChangedEvent(jid,
            isSelected);
        for (BaseBuddySelectionListener buddySelectionListener : this.buddySelectionListeners) {
            buddySelectionListener.buddySelectionChanged(event);
        }
    }

    @Override
    public void setLayout(Layout layout) {
        // ignore
    }
}
