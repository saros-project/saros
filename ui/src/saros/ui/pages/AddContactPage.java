package saros.ui.pages;

import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;

/**
 * Represents the Saros Session invitation Wizard. This wizard is used to start a Session by sharing
 * Projects with multiple contacts.
 */
public class AddContactPage extends AbstractBrowserPage {

  public static final String HTML_DOC_NAME = "add-contact-page.html";

  /**
   * Created by PicoContainer
   *
   * @see HTMLUIContextFactory
   */
  public AddContactPage() {
    super(HTML_DOC_NAME, HTMLUIStrings.TITLE_ADD_CONTACT_PAGE);
  }
}
