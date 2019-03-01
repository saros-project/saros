package saros.ui.wizards.pages;

import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;
import saros.SarosConstants;
import saros.SarosPluginContext;
import saros.net.xmpp.JID;
import saros.net.xmpp.discovery.DiscoveryManager;
import saros.net.xmpp.discovery.DiscoveryManagerListener;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.util.selection.retriever.SelectionRetrieverFactory;
import saros.ui.widgets.viewer.roster.FilteredContactSelectionComposite;
import saros.ui.widgets.viewer.roster.events.ContactSelectionChangedEvent;
import saros.ui.widgets.viewer.roster.events.ContactSelectionListener;

/**
 * Allows the user to select a {@link JID} from the {@link Roster}.
 *
 * @author bkahlert
 */
public class ContactSelectionWizardPage extends WizardPage {
  private static final Logger LOG = Logger.getLogger(ContactSelectionWizardPage.class);

  protected static final String TITLE = Messages.ContactSelectionWizardPage_title;
  protected static final String DESCRIPTION = Messages.ContactSelectionWizardPage_description;

  protected static final String NO_CONTACT_SELECTED_ERROR_MESSAGE =
      Messages.ContactSelectionWizardPage_error_no_contact_selected;
  protected static final String OFFLINE_CONTACT_SELECTED_ERROR_MESSAGE =
      Messages.ContactSelectionWizardPage_error_offline_contact_selected;
  protected static final String CONTACTS_WITHOUT_SAROS_SUPPORT_WARNING_MESSAGE =
      Messages.ContactSelectionWizardPage_warn_contact_without_saros_support_selected;

  protected FilteredContactSelectionComposite contactSelectionComposite;

  /** Flag indicating if this wizard page can be completed even if no contact is selected */
  protected final boolean allowEmptyContactSelection;

  /** This flag is true as soon as the user selected contacts without problems. */
  protected boolean selectionWasValid = false;

  @Inject protected DiscoveryManager discoveryManager;

  /**
   * This {@link ContactSelectionListener} changes the {@link WizardPage}'s state according to the
   * selected {@link JID}s.
   */
  protected final ContactSelectionListener contactSelectionListener =
      new ContactSelectionListener() {

        @Override
        public void contactSelectionChanged(ContactSelectionChangedEvent event) {
          updatePageCompletion();
        }
      };

  /** This listener update the page completion if someone's presence changed. */
  protected final DiscoveryManagerListener discoveryManagerListener =
      new DiscoveryManagerListener() {
        @Override
        public void featureSupportUpdated(final JID jid, String feature, boolean isSupported) {

          if (!SarosConstants.XMPP_FEATURE_NAMESPACE.equals(feature)) return;

          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {
                @Override
                public void run() {
                  if (ContactSelectionWizardPage.this.getControl().isDisposed()) return;

                  updatePageCompletion();
                }
              });
        }
      };

  public ContactSelectionWizardPage() {
    this(false);
  }

  public ContactSelectionWizardPage(boolean allowEmptyContactSelection) {
    super(ContactSelectionWizardPage.class.getName());
    setTitle(TITLE);
    setDescription(DESCRIPTION);

    SarosPluginContext.initComponent(this);
    discoveryManager.addDiscoveryManagerListener(discoveryManagerListener);
    this.allowEmptyContactSelection = allowEmptyContactSelection;
  }

  @Override
  public void dispose() {
    discoveryManager.removeDiscoveryManagerListener(discoveryManagerListener);
    super.dispose();
  }

  @Override
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    setControl(composite);

    composite.setLayout(new GridLayout(1, false));

    contactSelectionComposite =
        new FilteredContactSelectionComposite(composite, SWT.BORDER | SWT.V_SCROLL);

    /*
     * preset the contact(s) e.g from the Saros view when invoking 'Work
     * Together on...' context menu
     */
    contactSelectionComposite.setSelectedContacts(
        SelectionRetrieverFactory.getSelectionRetriever(JID.class).getOverallSelection());

    contactSelectionComposite.addContactSelectionListener(contactSelectionListener);

    contactSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    updatePageCompletion();
  }

  protected void updatePageCompletion() {

    List<JID> selectedContacts = getSelectedContacts();

    List<JID> selectedContactsWithSarosSupport = getSelectedContactsWithSarosSupport();

    if (allowEmptyContactSelection && selectedContacts.isEmpty()) {
      setPageComplete(true);
      setErrorMessage(null);
    } else if (selectedContacts.size() == 0) {
      if (selectionWasValid) setErrorMessage(NO_CONTACT_SELECTED_ERROR_MESSAGE);
      setPageComplete(false);
    } else if (!contactSelectionComposite.areAllSelectedOnline()) {
      setErrorMessage(OFFLINE_CONTACT_SELECTED_ERROR_MESSAGE);
      setPageComplete(false);
    } else {
      selectionWasValid = true;
      setErrorMessage(null);

      if (selectedContacts.size() > selectedContactsWithSarosSupport.size()) {
        setMessage(CONTACTS_WITHOUT_SAROS_SUPPORT_WARNING_MESSAGE, IMessageProvider.WARNING);
      } else {
        setMessage(DESCRIPTION);
      }

      setPageComplete(true);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    if (!visible) return;

    contactSelectionComposite.setFocus();
  }

  // WizardPage Results

  public List<JID> getSelectedContacts() {
    if (contactSelectionComposite == null || contactSelectionComposite.isDisposed()) return null;

    return contactSelectionComposite.getSelectedContacts();
  }

  public List<JID> getSelectedContactsWithSarosSupport() {
    if (contactSelectionComposite == null || contactSelectionComposite.isDisposed()) return null;

    return contactSelectionComposite.getSelectedContactsWithSarosSupport();
  }
}
