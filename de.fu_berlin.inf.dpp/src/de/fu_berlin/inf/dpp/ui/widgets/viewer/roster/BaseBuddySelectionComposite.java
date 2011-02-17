package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster;

import java.util.ArrayList;
import java.util.List;

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
import de.fu_berlin.inf.dpp.ui.model.CheckStateProvider;
import de.fu_berlin.inf.dpp.ui.model.ITreeElement;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.model.workaround.CheckStatePreservingCheckboxTreeViewer;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BaseBuddySelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BuddySelectionChangedEvent;

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

    protected List<BaseBuddySelectionListener> buddySelectionListeners = new ArrayList<BaseBuddySelectionListener>();

    protected ICheckStateListener checkStateListener = new ICheckStateListener() {
        public void checkStateChanged(CheckStateChangedEvent event) {
            JID jid = (JID) ((ITreeElement) event.getElement())
                .getAdapter(JID.class);
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
        this.viewer = new CheckStatePreservingCheckboxTreeViewer(new Tree(this,
            style));
    }

    @Override
    protected void configureViewer() {
        super.configureViewer();
        ((CheckboxTreeViewer) this.viewer)
            .setCheckStateProvider(new CheckStateProvider());
    }

    /**
     * Sets the currently selected {@link JID}s.
     * 
     * @param buddies
     */
    public void setSelectedBuddies(List<JID> buddies) {
        List<RosterEntryElement> rosterEntryElements = new ArrayList<RosterEntryElement>();
        for (JID buddy : buddies) {
            rosterEntryElements.add(new RosterEntryElement(this.saros
                .getRoster(), buddy));
        }
        ((CheckboxTreeViewer) this.viewer)
            .setCheckedElements(rosterEntryElements.toArray());
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
