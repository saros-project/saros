package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster;

import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterGroupElement;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BaseBuddySelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BuddySelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.FilterNonSarosBuddiesChangedEvent;
import de.fu_berlin.inf.dpp.ui.wizards.AddBuddyWizard;

/**
 * This {@link Composite} extends {@link BaseBuddySelectionComposite} and
 * displays additional controls.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout and adding
 * sub {@link Control}s correctly.
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
public class BuddySelectionComposite extends BaseBuddySelectionComposite {
    protected boolean filterNonSarosBuddies;
    protected Button filterNonSarosBuddiesButton;
    protected ViewerFilter nonSarosBuddiesFilter = new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element) {
            /*
             * Groups are only displayed if at they contain at least one
             * selected element (e.g. buddy with Saros support).
             */
            if (element instanceof RosterGroupElement) {
                RosterGroupElement rosterGroupElement = (RosterGroupElement) element;
                for (Object child : rosterGroupElement.getChildren()) {
                    /*
                     * Check recursively
                     */
                    boolean childSelected = select(viewer, element, child);
                    if (childSelected)
                        return true;
                }
                return false;
            }

            /*
             * Entries are only displayed if they have Saros support
             */
            if (element instanceof RosterEntryElement) {
                RosterEntryElement rosterEntryElement = (RosterEntryElement) element;
                return rosterEntryElement.isSarosSupported();
            }

            return false;
        }
    };

    public BuddySelectionComposite(Composite parent, int style,
        boolean filterNonSarosBuddies) {
        super(parent, style);

        createControls();
        setFilterNonSarosBuddies(filterNonSarosBuddies);
    }

    /**
     * Creates additional controls
     */
    protected void createControls() {
        Composite controlComposite = new Composite(this, SWT.NONE);
        controlComposite.setLayoutData(LayoutUtils.createFillHGrabGridData());
        controlComposite.setLayout(new GridLayout(2, false));

        filterNonSarosBuddiesButton = new Button(controlComposite, SWT.CHECK);
        filterNonSarosBuddiesButton.setLayoutData(new GridData(SWT.BEGINNING,
            SWT.CENTER, false, false));
        filterNonSarosBuddiesButton
            .setText("Hide buddies without Saros support");
        filterNonSarosBuddiesButton
            .addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setFilterNonSarosBuddies(filterNonSarosBuddiesButton
                        .getSelection());
                }
            });

        Button addBuddyButton = new Button(controlComposite, SWT.PUSH);
        addBuddyButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true,
            false));
        addBuddyButton.setText("Add Buddy...");
        addBuddyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AddBuddyWizard addBuddyWizard = WizardUtils
                    .openAddBuddyWizard();
                if (addBuddyWizard != null) {
                    List<JID> selectedBuddies = getSelectedBuddies();
                    selectedBuddies.add(addBuddyWizard.getBuddy());
                    setSelectedBuddies(selectedBuddies);
                }
            }
        });
    }

    /**
     * Defines whether non Saros buddies should be displayed or not
     * 
     * @param filterNonSarosBuddies
     *            true if nonSaros buddies should not be displayed
     */
    public void setFilterNonSarosBuddies(boolean filterNonSarosBuddies) {
        if (this.filterNonSarosBuddies == filterNonSarosBuddies)
            return;

        this.filterNonSarosBuddies = filterNonSarosBuddies;

        if (this.filterNonSarosBuddiesButton != null
            && !this.filterNonSarosBuddiesButton.isDisposed()
            && this.filterNonSarosBuddiesButton.getSelection() != filterNonSarosBuddies) {
            this.filterNonSarosBuddiesButton
                .setSelection(filterNonSarosBuddies);
        }

        if (filterNonSarosBuddies) {
            viewer.addFilter(nonSarosBuddiesFilter);
            ViewerUtils.expandAll(viewer);
        } else {
            viewer.removeFilter(nonSarosBuddiesFilter);
            ViewerUtils.expandAll(viewer);
        }

        notifyBuddySelectionListener(filterNonSarosBuddies);
    }

    /**
     * Notify all {@link BuddySelectionListener}s about a changed
     * {@link BuddySelectionComposite#filterNonSarosBuddies} option.
     * 
     * @param filterNonSarosBuddies
     */
    public void notifyBuddySelectionListener(boolean filterNonSarosBuddies) {
        FilterNonSarosBuddiesChangedEvent event = new FilterNonSarosBuddiesChangedEvent(
            filterNonSarosBuddies);
        for (BaseBuddySelectionListener buddySelectionListener : this.buddySelectionListeners) {
            if (buddySelectionListener instanceof BuddySelectionListener)
                ((BuddySelectionListener) buddySelectionListener)
                    .filterNonSarosBuddiesChanged(event);
        }
    }
}
