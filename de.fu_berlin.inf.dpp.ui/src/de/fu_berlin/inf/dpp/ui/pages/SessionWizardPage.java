package de.fu_berlin.inf.dpp.ui.pages;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.ui.renderer.ProjectListRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

/**
 * Represents the Saros Session invitation Wizard. This wizard is used to start a Session by sharing
 * Projects with multiple contacts.
 */
public class SessionWizardPage extends AbstractBrowserPage {

  public static final String HTML_DOC_NAME = "start-session-wizard.html";

  /**
   * Created by PicoContainer
   *
   * @see HTMLUIContextFactory
   */
  public SessionWizardPage(StateRenderer stateRenderer, ProjectListRenderer projectListRenderer) {
    super(HTML_DOC_NAME, HTMLUIStrings.TITLE_START_SESSION_WIZARD);
    this.addRenderer(stateRenderer, projectListRenderer);
  }
}
