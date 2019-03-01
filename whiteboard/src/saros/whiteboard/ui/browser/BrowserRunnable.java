package saros.whiteboard.ui.browser;

/**
 * Contains the code that will be run when a browser function is called.
 *
 * <p>is used to overcome the problem that the {@link Runnable} interface doesn't accept parameters
 * in its {@link Runnable#run()} method.
 */
public interface BrowserRunnable {
  /**
   * runs the java code when the JavaScript function is called.
   *
   * @param arguments the parameters which have been given in browser while calling the function
   * @return result of the executed code
   */
  Object run(Object[] arguments);
}
