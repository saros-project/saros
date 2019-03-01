package saros.whiteboard.ui.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import saros.whiteboard.sxe.SXEController;
import saros.whiteboard.sxe.constants.RecordType;
import saros.whiteboard.sxe.exceptions.MissingRecordException;
import saros.whiteboard.sxe.records.AttributeRecord;
import saros.whiteboard.sxe.records.ElementRecord;
import saros.whiteboard.sxe.records.IRecord;

public class BrowserActionExecuter {

  private static final Logger LOG = Logger.getLogger(BrowserActionExecuter.class);

  private final SXEController sxe;

  public BrowserActionExecuter(SXEController sxe) {
    this.sxe = sxe;
  }

  public void execute(BrowserAction action) {
    sxe.executeAndCommit(getChangeRecords(action));
  }

  private List<IRecord> getChangeRecords(BrowserAction action) {
    String type = action.getType();
    if (type.equals(RecordType.NEW.toString())) {
      return create(action);
    } else if (type.equals(RecordType.SET.toString())) {
      return set(action);
    } else if (type.equals(RecordType.REMOVE.toString())) {
      return remove(action);
    } else {
      LOG.error("undefined action type received: " + type);
      return Collections.emptyList();
    }
  }

  private List<IRecord> create(BrowserAction action) {
    List<IRecord> records = new ArrayList<IRecord>();
    // since the library fabric.js (used in the html whiteboard) does not
    // support parent hierarchies this function will always add the new
    // element to the root element
    ElementRecord parent = sxe.getDocumentRecord().getRoot();
    ElementRecord newElement = parent.createNewElementRecord(null, action.getElementType());
    newElement.setRid(action.getID());
    records.add(newElement);
    records.addAll(setAttributes(newElement, action.getAttributes()));
    return records;
  }

  private List<IRecord> setAttributes(ElementRecord element, List<Map<String, String>> attributes) {
    List<IRecord> records = new ArrayList<IRecord>();
    for (Map<String, String> attribute : attributes) {
      IRecord record =
          element.createNewOrSetAttributeRecord(
              null, attribute.get("name"), attribute.get("chdata"), false);
      if (record instanceof AttributeRecord)
        ((AttributeRecord) record).setRid(attribute.get("rid"));
      records.add(record);
    }
    return records;
  }

  private List<IRecord> set(BrowserAction action) {
    ElementRecord element = getElement(action.getID());
    if (element == null) return Collections.emptyList();
    return setAttributes(element, action.getAttributes());
  }

  private List<IRecord> remove(BrowserAction action) {
    ElementRecord element = getElement(action.getID());
    if (element == null) return Collections.emptyList();

    List<IRecord> records = new ArrayList<IRecord>();
    records.add(element.getRemoveRecord());
    return records;
  }

  private ElementRecord getElement(String id) {
    try {
      return sxe.getDocumentRecord().getElementRecordById(id);
    } catch (MissingRecordException e) {
      LOG.error(
          "trying to access an element that does NOT" + " exist, the changes will be ignored.", e);
      return null;
    }
  }
}
