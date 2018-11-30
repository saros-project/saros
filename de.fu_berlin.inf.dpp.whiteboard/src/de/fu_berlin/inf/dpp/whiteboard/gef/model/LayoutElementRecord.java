package de.fu_berlin.inf.dpp.whiteboard.gef.model;

import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.RecordType;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.DocumentRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.ElementRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.IRecord;
import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SetRecord;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Base class for records that understand GEF's layout mechanic. Thus it allows to convert a {@link
 * Rectangle} to a list of records.
 *
 * @author jurke
 */
public abstract class LayoutElementRecord extends ElementRecord {

  public static final String PROPERTY_LAYOUT = "NodeLayout";

  public LayoutElementRecord(DocumentRecord documentRecord) {
    super(documentRecord);
  }

  /**
   * This method will return <code>AttributeRecord</code>s that define the layout rectangle of this
   * element or if the attribute exists a <code>SetRecord</code>s to update the layout respectively.
   *
   * <p>Note, omits <code>SetRecord</code>s that are not changed by the new layout.
   *
   * @param layout
   * @return the list of records to change the layout to the provided one
   */
  public final List<IRecord> getChangeLayoutRecords(Rectangle layout) {
    return removeTrivalRecords(createLayoutRecords(layout, false));
  }

  /**
   * @param layout
   * @param onlyCreateNewRecords whether not to return a SetRecord if the record exists already but
   *     a new attribute record
   * @return the list of records corresponding to the provided one
   */
  public abstract List<IRecord> createLayoutRecords(Rectangle layout, boolean onlyCreateNewRecords);

  public Rectangle getLayout() {
    Point loc = getLocation();
    Dimension dim = getSize();
    if (loc == null | dim == null) return null;
    else return new Rectangle(loc, dim);
  }

  public abstract Point getLocation();

  public abstract Dimension getSize();

  public boolean isComposite() {
    return false;
  }

  public List<ElementRecord> getChildrenArray() {
    return new ArrayList<ElementRecord>(getChildElements());
  }

  /**
   * Utility method to remove <code>SetRecord</code> that do not change anything. Thus implementing
   * subclasses do not have to check this for every attribute.
   *
   * @param records
   */
  protected static List<IRecord> removeTrivalRecords(List<IRecord> records) {
    Iterator<IRecord> it = records.iterator();
    IRecord tmp;

    while (it.hasNext()) {
      tmp = it.next();
      if (tmp.getRecordType() == RecordType.SET) {
        if (!((SetRecord) tmp).changesTargetState()) {
          // i.e. width, height
          it.remove();
        }
      }
    }
    return records;
  }
}
