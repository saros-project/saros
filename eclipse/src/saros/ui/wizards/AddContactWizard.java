package saros.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;
import java.util.function.BiPredicate;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import saros.SarosPluginContext;
import saros.exceptions.OperationCanceledException;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.DialogUtils;
import saros.ui.util.SWTUtils;
import saros.ui.wizards.pages.AddContactWizardPage;

/** Wizard for adding a new contact to the Contact list of the currently connected user. */
public class AddContactWizard extends Wizard {
  private static final Logger log = Logger.getLogger(AddContactWizard.class);

  public static final String TITLE = Messages.AddContactWizard_title;

  @Inject private static XMPPContactsService contactsService;

  protected final AddContactWizardPage addContactWizardPage = new AddContactWizardPage();

  /**
   * Caches the {@link JID} reference in case the {@link WizardPage}s are already disposed but a
   * user still needs access.
   */
  protected JID cachedContact;

  public AddContactWizard() {
    SarosPluginContext.initComponent(this);
    setWindowTitle(TITLE);
    setDefaultPageImageDescriptor(ImageManager.getImageDescriptor(ImageManager.WIZBAN_ADD_CONTACT));
    setNeedsProgressMonitor(true);
  }

  @Override
  public void addPages() {
    addPage(addContactWizardPage);
  }

  @Override
  public boolean performFinish() {
    final JID jid = addContactWizardPage.getContact();
    final String nickname = addContactWizardPage.getNickname();

    if (addContactWizardPage.isContactAlreadyAdded()) {
      log.debug("contact " + jid.toString() + " already added");
      return true;
    }

    try {
      getContainer()
          .run(
              true,
              false,
              monitor -> {
                monitor.beginTask("Adding contact " + jid + "...", IProgressMonitor.UNKNOWN);

                try {
                  addContact(jid, nickname);
                  cachedContact = jid;
                } catch (CancellationException e1) {
                  throw new InterruptedException();
                } catch (OperationCanceledException e1) {
                  throw new InvocationTargetException(e1);
                } finally {
                  monitor.done();
                }
              });
    } catch (InvocationTargetException e) {
      log.warn(e.getCause().getMessage(), e.getCause());
      addContactWizardPage.setErrorMessage(e.getMessage());
      // Leave the wizard open
      return false;
    } catch (InterruptedException e) {
      log.error("uninterruptible context was interrupted", e);
    }

    // Close the wizard
    return true;
  }

  /**
   * Returns {@JID} of the newly added contact
   *
   * @return
   */
  public JID getContact() {
    return cachedContact;
  }

  /**
   * Adds given contact to the Contact List.
   *
   * @param jid
   * @param nickname
   * @throws OperationCanceledException
   */
  private void addContact(JID jid, String nickname) throws OperationCanceledException {
    BiPredicate<String, String> questionDialogHandler =
        (title, message) -> {
          try {
            return SWTUtils.runSWTSync(
                () -> DialogUtils.openQuestionMessageDialog(null, title, message));
          } catch (Exception e) {
            log.debug("Error opening questionMessageDialog", e);
            return false;
          }
        };

    contactsService.addContact(jid, nickname, questionDialogHandler);
  }
}
