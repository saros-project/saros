package saros.ui.renderer;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import java.util.ArrayList;
import java.util.List;
import saros.ui.model.State;

/**
 * Implementations of this interface can transfer their current state to a list of browsers by
 * calling Javascript functions.
 *
 * <p>As the responsible browser instances may change, all renderers manage a reference to the
 * current browsers and provide methods for its replacement. @JTourBusStop 3, Extending the HTML
 * GUI, Calling Javascript from Java:
 *
 * <p>Each renderer class is used to transfer parts of the application state to the each browser for
 * rendering.
 *
 * <p>They may store state themselves, as {@link StateRenderer} does.
 *
 * <p>There are two ways to get the state from the Saros:
 *
 * <p>1. use a listener if supported. See {@link StateRenderer} for an example.
 *
 * <p>2. query the state from the core directly the {@link AccountRenderer} does that.
 *
 * <p>For the management of GUI state create custom GUI model class in the model package, like
 * {@link State}. Those classes should be converted to JSON strings with the GSON library in the
 * renderer classes.
 */
public abstract class Renderer {

  private List<IJQueryBrowser> browserList = new ArrayList<IJQueryBrowser>();

  /**
   * Renders the current state managed by the renderer in the given browser.
   *
   * @param browser the browser to be rendered
   */
  public abstract void render(IJQueryBrowser browser);

  /** Renders the current state managed by the renderer for each browser. */
  public synchronized void render() {
    for (IJQueryBrowser browser : browserList) {
      this.render(browser);
    }
  }

  /**
   * Adds the given browser to the renderer
   *
   * @param browser the browser to be added
   */
  public synchronized void addBrowser(IJQueryBrowser browser) {
    this.browserList.add(browser);
    render(browser);
  }

  /**
   * Removes the given browser from the renderer. This method must be called every time the browser
   * is disposed.
   *
   * @param browser the browser to be removed
   */
  public synchronized void removeBrowser(IJQueryBrowser browser) {
    browserList.remove(browser);
  }
}
