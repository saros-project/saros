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

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterGroupElement;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewerUtils;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.ContactSelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.FilterContactsChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.FilteredContactSelectionListener;
import de.fu_berlin.inf.dpp.ui.wizards.AddContactWizard;

/**
 * This {@link Composite} extends {@link ContactSelectionComposite} and displays
 * additional controls.
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
public class FilteredContactSelectionComposite extends
    ContactSelectionComposite {

    protected boolean filterNonSarosContacts;

    protected Button filterNonSarosContactsButton;

    protected ViewerFilter nonSarosContactsFilter = new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement,
            Object element) {
            /*
             * Groups are only displayed if at they contain at least one
             * selected element (e.g. contact with Saros support).
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

    public FilteredContactSelectionComposite(Composite parent, int style,
        boolean filterNonSarosContacts) {
        super(parent, style);

        createControls();
        setFilterNonSarosContacts(filterNonSarosContacts);
    }

    /**
     * Creates additional controls
     */
    protected void createControls() {
        Composite controlComposite = new Composite(this, SWT.NONE);
        controlComposite.setLayoutData(LayoutUtils.createFillHGrabGridData());
        controlComposite.setLayout(new GridLayout(2, false));

        filterNonSarosContactsButton = new Button(controlComposite, SWT.CHECK);
        filterNonSarosContactsButton.setLayoutData(new GridData(SWT.BEGINNING,
            SWT.CENTER, false, false));

        filterNonSarosContactsButton
            .setText(Messages.FilteredContactSelectionComposite_filter_non_saros_contacts_button_text);

        filterNonSarosContactsButton
            .addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setFilterNonSarosContacts(filterNonSarosContactsButton
                        .getSelection());
                }
            });

        Button addContactButton = new Button(controlComposite, SWT.PUSH);
        addContactButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true,
            false));
        addContactButton
            .setText(Messages.FilteredContactSelectionComposite_add_contact_button_text);
        addContactButton.setImage(ImageManager.ELCL_CONTACT_ADD);
        addContactButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AddContactWizard addContactWizard = WizardUtils
                    .openAddContactWizard();

                if (addContactWizard == null)
                    return;

                List<JID> selectedContacts = getSelectedContacts();
                selectedContacts.add(addContactWizard.getContact());
                setSelectedContacts(selectedContacts);
            }
        });
    }

    /**
     * Defines whether non Saros contacts should be displayed or not
     * 
     * @param filter
     *            <code>true</code> if non Saros contacts should not be
     *            displayed
     */
    public void setFilterNonSarosContacts(boolean filter) {
        if (filterNonSarosContacts == filter)
            return;

        filterNonSarosContacts = filter;

        if (filterNonSarosContactsButton != null
            && !filterNonSarosContactsButton.isDisposed()
            && filterNonSarosContactsButton.getSelection() != filter) {
            filterNonSarosContactsButton.setSelection(filter);
        }

        if (filter) {
            getViewer().addFilter(nonSarosContactsFilter);
            ViewerUtils.expandAll(getViewer());
        } else {
            getViewer().removeFilter(nonSarosContactsFilter);
            ViewerUtils.expandAll(getViewer());
        }

        notifyContactSelectionListener(filter);
    }

    /**
     * Notify all {@link FilteredContactSelectionListener}s about a changed
     * {@link FilteredContactSelectionComposite#filterNonSarosContacts} option.
     * 
     * @param filterNonSarosContacts
     */
    public void notifyContactSelectionListener(boolean filterNonSarosContacts) {
        FilterContactsChangedEvent event = new FilterContactsChangedEvent(
            filterNonSarosContacts);

        for (ContactSelectionListener contactSelectionListener : contactSelectionListeners) {
            if (contactSelectionListener instanceof FilteredContactSelectionListener)
                ((FilteredContactSelectionListener) contactSelectionListener)
                    .filterNonSarosContactsChanged(event);
        }
    }
}
