package de.fu_berlin.inf.dpp.ui.widgets.viewer.roster;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterEntryElement;
import de.fu_berlin.inf.dpp.ui.model.roster.RosterGroupElement;
import de.fu_berlin.inf.dpp.ui.util.LayoutUtils;
import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.wizards.AddContactWizard;
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

/**
 * This {@link Composite} extends {@link ContactSelectionComposite} and displays additional
 * controls.
 *
 * <p>This composite does <strong>NOT</strong> handle setting the layout and adding sub {@link
 * Control}s correctly.
 *
 * <dl>
 *   <dt><b>Styles:</b>
 *   <dd>NONE and those supported by {@link StructuredViewer}
 *   <dd>SWT.CHECK is used by default
 *   <dt><b>Events:</b>
 *   <dd>(none)
 * </dl>
 *
 * @author bkahlert
 */
public final class FilteredContactSelectionComposite extends ContactSelectionComposite {

  private final ViewerFilter sarosSupportFilter =
      new ViewerFilter() {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
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
              if (childSelected) return true;
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

  public FilteredContactSelectionComposite(Composite parent, int style) {
    super(parent, style);
    createControls();
    getViewer().addFilter(sarosSupportFilter);
  }

  private void createControls() {
    Composite controlComposite = new Composite(this, SWT.NONE);
    controlComposite.setLayoutData(LayoutUtils.createFillHGrabGridData());
    controlComposite.setLayout(new GridLayout(2, false));

    Button addContactButton = new Button(controlComposite, SWT.PUSH);
    addContactButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));

    addContactButton.setText(Messages.FilteredContactSelectionComposite_add_contact_button_text);

    addContactButton.setImage(ImageManager.ELCL_CONTACT_ADD);
    addContactButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            AddContactWizard addContactWizard = WizardUtils.openAddContactWizard();

            if (addContactWizard == null) return;

            List<JID> selectedContacts = getSelectedContacts();
            selectedContacts.add(addContactWizard.getContact());
            setSelectedContacts(selectedContacts);
          }
        });
  }
}
