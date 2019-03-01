package saros.whiteboard.ui.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Instances of this class are created only by GSON and only when a command string is received from
 * the browser. <br>
 * GSON will create an instance and assign the parsed values from the string to their corresponding
 * properties in the instance. This instance is later given to {@link
 * BrowserActionExecuter#execute(BrowserAction)} to be processed.
 */
public class BrowserAction {

  private BrowserAction() {
    // should not be manually instantiated
  }

  private String type;
  private String elementType;
  private String id;
  private ArrayList<Map<String, String>> attributes;

  public String getType() {
    return type;
  }

  public String getElementType() {
    return elementType;
  }

  public String getID() {
    return id;
  }

  public List<Map<String, String>> getAttributes() {
    return attributes;
  }
}
