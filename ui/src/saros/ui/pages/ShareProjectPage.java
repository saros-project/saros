package saros.ui.pages;

import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.ui.renderer.ProjectListRenderer;
import saros.ui.renderer.StateRenderer;

/**
 * Represents the Saros Session invitation Wizard. This wizard is used to start a Session by sharing
 * Projects with multiple contacts.
 */
public class ShareProjectPage extends AbstractBrowserPage {

  public static final String HTML_DOC_NAME = "share-project-page.html";

  /**
   * Created by PicoContainer
   *
   * @see HTMLUIContextFactory
   */
  public ShareProjectPage(StateRenderer stateRenderer, ProjectListRenderer projectListRenderer) {
    super(HTML_DOC_NAME, HTMLUIStrings.TITLE_SHARE_PROJECT_PAGE);
    this.addRenderer(stateRenderer, projectListRenderer);
  }
}
