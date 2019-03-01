package saros.ui.pages;

import java.util.ArrayList;
import java.util.List;
import saros.ui.renderer.Renderer;

/**
 * Abstract implementation of {@link IBrowserPage} which offers convenience methods for registering
 * browser functions and renderer.
 */
public abstract class AbstractBrowserPage implements IBrowserPage {

  /** Common HTML document location */
  public static final String PATH = "html/dist/";

  private String relativePageLocation;

  /** The title is shown to the user in the dialog. */
  private String pageTitle;

  private final List<Renderer> renderers = new ArrayList<Renderer>();

  /**
   * Creates a new BrowserPage that encapsulates the location and title of the HTML page as well as
   * the needed browsers functions and renderer.
   *
   * @param htmlDocName the file name of the HTML document without any path addition
   * @param pageTitle the title that will be shown in the dialog
   */
  public AbstractBrowserPage(String htmlDocName, String pageTitle) {
    this.relativePageLocation = PATH + htmlDocName;
    this.pageTitle = pageTitle;
  }

  @Override
  public String getRelativePath() {
    return relativePageLocation;
  }

  @Override
  public String getTitle() {
    return pageTitle;
  }

  @Override
  public List<Renderer> getRenderers() {
    return renderers;
  }

  protected void addRenderer(Renderer... renderers) {
    for (Renderer renderer : renderers) {
      this.renderers.add(renderer);
    }
  }
}
