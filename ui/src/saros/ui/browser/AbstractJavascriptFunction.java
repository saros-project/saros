package saros.ui.browser;

// Adopted from https://github.com/ag-se/swt-browser-improved
public abstract class AbstractJavascriptFunction {

  protected IBrowser browser;
  private final String name;

  /** @param name the name of the Javascript function that calls into Java */
  public AbstractJavascriptFunction(String name) {
    this.name = name;
  }

  public void setBrowser(IBrowser browser) {
    this.browser = browser;
  }

  /**
   * This method is to be overridden to supply the Java code
   *
   * @param arguments an array of Object, these are the parameters provided in Javascript
   * @return the value to return back to Javascript
   */
  public abstract Object function(Object[] arguments);

  public String getName() {
    return name;
  }
}
