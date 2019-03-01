package saros.ui.pages;

import saros.HTMLUIContextFactory;
import saros.HTMLUIStrings;
import saros.ui.renderer.AccountRenderer;
import saros.ui.renderer.StateRenderer;

/** Represents the Saros main view. */
public class MainPage extends AbstractBrowserPage {

  public static final String HTML_DOC_NAME = "main-page.html";

  /**
   * Created by PicoContainer
   *
   * @see HTMLUIContextFactory
   */
  public MainPage(AccountRenderer accountRenderer, StateRenderer stateRenderer) {
    super(HTML_DOC_NAME, HTMLUIStrings.TITLE_MAIN_PAGE);
    this.addRenderer(stateRenderer, accountRenderer);
  }
}
