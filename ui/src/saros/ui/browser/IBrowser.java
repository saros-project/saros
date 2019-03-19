package saros.ui.browser;

public interface IBrowser {

  /*
   * Try to set the focus on the browser widget
   *
   * @return a boolean that is false if it was not possible to set the focus
   */
  public boolean setFocus();

  /*
   * Change the size of the browser
   *
   * @param width the new width of the browser widget
   *
   * @param height the new height of the browser widget
   */
  public void setSize(final int width, final int height);

  /*
   * Returns the currently URL which is currently loaded by the browser
   *
   * @return a string that represents the currently loaded URL
   */
  public String getUrl();

  /*
   * Load a specified URL and block until the page is loaded or the timeout is
   * exceeded
   *
   * @param url the URL which has be loaded
   *
   * @param timeout the timeout that specified the maximal load time
   *
   * @return a boolean that is false if the URL cannot be loaded (within the
   * timeout)
   */
  public boolean loadUrl(final String url, final int timeout);

  /*
   * Load a specified HTML content into the browser
   *
   * @param html the HTML that has to be loaded
   */
  public void loadHtml(final String html);

  /*
   * Execute a JavaScript code that returns an object.
   *
   * @param jsCode the JavaScript code that has to be executed
   *
   * @return an object that represents an arbitrary return value of jsCode
   */
  public Object evaluate(final String jsCode);

  /*
   * Execute a JavaScript code without a return value.
   *
   * @param jsCode the JavaScript code that has to be executed
   */
  public boolean execute(final String jsCode);

  /*
   * Add the functionality a JavascriptFunction class to the browser
   *
   * @param jsFunction the function that has to be injected into the browser
   */
  public void addBrowserFunction(final AbstractJavascriptFunction jsFunction);

  /*
   * Add runnable that is executed after the browser widget is disposed
   *
   * @param run a runnable that is executed after the browser widget is
   * disposed
   */
  public void runOnDisposal(Runnable run);
}
