package saros.ui;

import saros.ui.pages.AbstractBrowserPage;
import saros.ui.pages.AddContactPage;
import saros.ui.pages.MainPage;
import saros.ui.pages.ShareProjectPage;

/**
 * This enum make the connection between conceptual View and their technical realization (the page).
 * It allows UI designers, for example, to move a simple form (such as ADD_CONTACT_VIEW) to a
 * separate browser widget without breaking the tests by just changing the pageClass part of the
 * corresponding Key.
 */
// TODO: Verify whether this enum is needed in production code.
public enum View {
  /** The permanently accessible view of Saros, which provides access to most of its features. */
  MAIN_VIEW(MainPage.class, "main-page", "root"),
  /** The form to add a new contact with */
  ADD_CONTACT_VIEW(AddContactPage.class, "add-contact-page", "add-contact-form"),
  /** The share project dialog */
  SHARE_PROJECT_VIEW(ShareProjectPage.class, "share-project-page", "session-wizard"),
  /** The dummy page for testing all html components */
  BASIC_WIDGET_TEST(MainPage.class, "basic-widget-test", "basic-widget-test-root");

  private final Class<? extends AbstractBrowserPage> pageClass;
  private final String viewName;
  private final String rootId;

  private View(Class<? extends AbstractBrowserPage> pageClass, String viewName, String rootId) {
    this.pageClass = pageClass;
    this.viewName = viewName;
    this.rootId = rootId;
  }

  public Class<? extends AbstractBrowserPage> getPageClass() {
    return pageClass;
  }

  public String getViewName() {
    return viewName;
  }

  public String getRootId() {
    return rootId;
  }
}
