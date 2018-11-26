package de.fu_berlin.inf.dpp.ui.pages;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.HTMLUIStrings;
import de.fu_berlin.inf.dpp.ui.renderer.AccountRenderer;
import de.fu_berlin.inf.dpp.ui.renderer.StateRenderer;

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
