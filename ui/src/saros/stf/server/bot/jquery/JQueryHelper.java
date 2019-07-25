package saros.stf.server.bot.jquery;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.NameSelector;
import java.util.ArrayList;
import java.util.List;

/**
 * The helper class is used in order to execute jquery commands in the HTML GUI browser during
 * testing.
 */
public class JQueryHelper {
  private final IJQueryBrowser browser;

  public JQueryHelper(IJQueryBrowser browser) {
    this.browser = browser;
  }

  /**
   * Clicks on an element defined by a given selector.
   *
   * @param selector selector that defines the clickable element
   */
  public void clickOnSelection(ISelector selector) {
    this.browser.run(String.format("%s[0].click()", selector.getStatement()));
  }

  /**
   * Returns the text of an element defined by a given selector.
   *
   * @param selector selector that defines the element containing text
   * @return the textual content of the selection
   */
  public String getTextOfSelection(ISelector selector) {
    return (String)
        this.browser.syncRun(String.format("return %s.text()", selector.getStatement()));
  }

  /**
   * Sets the text of an element defined by a given selector.
   *
   * @param selector selector that defines the element which text has to be set
   * @param text new text which is set
   */
  public void setTextOfSelection(ISelector selector, String text) {
    this.browser.syncRun(String.format("%s.text('%s')", selector.getStatement(), text));
  }

  /**
   * Returns a field value of the current view.
   *
   * @param selector selector that defines the field
   * @return field value defined by the selector
   */
  public Object getFieldValue(ISelector selector) {
    final String name = this.getSelectorName(selector);
    return this.browser.syncRun(String.format("return view.getFieldValue('%s')", name));
  }

  /**
   * Sets the value of a field of the current view.
   *
   * @param selector selector that defindes the field
   * @param value new value of the field defined by the selector
   */
  public void setFieldValue(ISelector selector, Object value) {
    final String name = this.getSelectorName(selector);
    String serializedValue;

    if (value instanceof String) serializedValue = String.format("'%s'", (String) value);
    else serializedValue = value.toString();

    this.browser.syncRun(
        String.format("return view.getFieldValue('%s', %s)", name, serializedValue));
  }

  /**
   * Verifies whether an element defined by a selector exists.
   *
   * @param selector selector whose existence has to be verified
   * @return <code>true</code> if the element defined by the selector exists
   */
  public boolean selectionExists(ISelector selector) {
    Boolean result =
        (Boolean)
            this.browser.syncRun(String.format("return %s.length > 0;", selector.getStatement()));
    return result != null && result;
  }

  /**
   * Returns the text content of multiple elements defined by a selector as list.
   *
   * @param selector selector that defines multiple elements
   * @return the text content of the defined elements
   */
  public List<String> getListItemsText(ISelector selector) {
    String code =
        "var strings = [];"
            + selector.getStatement()
            + ".each(function (i) { "
            + "strings[i] = $(this).text().trim(); }); return strings; ";

    return this.getListItems(code);
  }

  /**
   * Returns the value of multiple elements defined by a selector as list.
   *
   * @param selector selector that defines multiple elements
   * @return the values of the defined elements
   */
  public List<String> getListItemsValue(ISelector selector) {
    String code =
        "var strings = [];"
            + selector.getStatement()
            + ".each(function (i) { "
            + "strings[i] = $(this).val(); }); return strings; ";

    return this.getListItems(code);
  }

  /**
   * Returns a list of all options of a select element defined by a selector.
   *
   * @param selector selector that defines a select element
   * @return a list of all options
   */
  public List<String> getSelectOptions(ISelector selector) {
    String code =
        "var options = []; "
            + "$('"
            + selector
            + " option').each(function (i) { "
            + "options[i] = $(this).val(); }); "
            + "return options; ";

    return this.getListItems(code);
  }

  /**
   * returns a list of strings which is defined by a javascript code snippet that returns a list.
   *
   * @param code javascript code that returns a list of strings
   * @return a list that contains the content of the javascript list created by the code
   */
  private List<String> getListItems(String code) {

    Object[] objects = (Object[]) this.browser.syncRun(code);

    List<String> strings = new ArrayList<String>();
    for (Object o : objects) {
      strings.add(o.toString());
    }

    return strings;
  }

  /**
   * Returns the name field value of a <code>ISelector</code> which has to be <code>NameSelector
   * </code>. Otherwise the return value is <code>null</code>.
   *
   * @param selector selector whos name value is returned
   * @return the name field value of the selector
   */
  private String getSelectorName(ISelector selector) {
    if (selector instanceof NameSelector) {
      return ((NameSelector) selector).getName();
    }
    return null;
  }
}
