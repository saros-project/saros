package saros.whiteboard.ui.browser;

/**
 * Describes the functionality of the browser used to display the HTMLWhiteboard, this allows for an
 * IDE-independent implementation of the Whiteboard.
 *
 * <p>Any browser displaying the whiteboard must implement this interface.
 */
public interface IWhiteboardBrowser {

  /**
   * Runs the script in browser <b>asynchronously</b> and ignores returned values.
   *
   * <p>this function can be called from the UI thread.
   *
   * @param script String that will be called as Javascript code in browser
   */
  void asyncRun(String script);

  /**
   * Creates a function that is callable from Javascript from within the browser.
   *
   * @param functionName the name of the function that will be globally registered in the Javascript
   *     runtime environment
   * @param runnable the java code to be executed when the function is called from Javascript
   */
  void createBrowserFunction(String functionName, BrowserRunnable runnable);
}
