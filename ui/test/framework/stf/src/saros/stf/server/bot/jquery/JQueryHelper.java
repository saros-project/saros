package saros.stf.server.bot.jquery;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.NameSelector;
import java.util.ArrayList;
import java.util.List;

public class JQueryHelper {
  private final IJQueryBrowser browser;

  public JQueryHelper(IJQueryBrowser browser) {
    this.browser = browser;
  }

  public void clickOnSelection(ISelector selector) {
    this.browser.run(String.format("%s[0].click()", selector.getStatement()));
  }

  public String getTextOfSelection(ISelector selector) {
    return (String)
        this.browser.syncRun(String.format("return %s.text()", selector.getStatement()));
  }

  public void setTextOfSelection(ISelector selector, String text) {
    this.browser.syncRun(String.format("%s.text('%s')", selector.getStatement(), text));
  }

  public Object getFieldValue(ISelector selector) {
    final String name = this.getSelectorName(selector);
    return this.browser.syncRun(String.format("return view.getFieldValue('%s')", name));
  }

  public void setFieldValue(ISelector selector, Object value) {
    final String name = this.getSelectorName(selector);
    String serializedValue;

    if (value instanceof String) serializedValue = String.format("'%s'", (String) value);
    else serializedValue = value.toString();

    this.browser.syncRun(
        String.format("return view.getFieldValue('%s', %s)", name, serializedValue));
  }

  public boolean selectionExists(ISelector selector) {
    return (Boolean)
        this.browser.syncRun(String.format("return %s.length > 0;", selector.getStatement()));
  }

  public List<String> getListItemsText(ISelector selector) {
    String code =
        "var strings = [];"
            + selector.getStatement()
            + ".each(function (i) { "
            + "strings[i] = $(this).text().trim(); }); return strings; ";

    return this.getListItems(selector, code);
  }

  private List<String> getListItems(ISelector selector, String code) {

    Object[] objects = (Object[]) this.browser.syncRun(code);

    List<String> strings = new ArrayList<String>();
    for (Object o : objects) {
      strings.add(o.toString());
    }

    return strings;
  }

  public List<String> getListItemsValue(ISelector selector) {
    String code =
        "var strings = [];"
            + selector.getStatement()
            + ".each(function (i) { "
            + "strings[i] = $(this).val(); }); return strings; ";

    return this.getListItems(selector, code);
  }

  public List<String> getSelectOptions(ISelector selector) {
    String code =
        "var options = []; "
            + "$('"
            + selector
            + " option').each(function (i) { "
            + "options[i] = $(this).val(); }); "
            + "return options; ";

    return this.getListItems(selector, code);
  }

  private String getSelectorName(ISelector selector) {
    if (selector instanceof NameSelector) {
      return ((NameSelector) selector).getName();
    }
    return null;
  }
}
