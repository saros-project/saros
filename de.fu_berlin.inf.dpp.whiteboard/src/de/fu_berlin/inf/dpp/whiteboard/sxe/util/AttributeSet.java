package de.fu_berlin.inf.dpp.whiteboard.sxe.util;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.AttributeRecord;
import java.util.Collection;
import java.util.HashMap;

/**
 * A list of AttributeRecords that wraps a HashMap to accelerate the access of an AttributeRecord by
 * its RID.
 *
 * @author jurke
 */
public class AttributeSet extends NodeSet<AttributeRecord> {

  private static final long serialVersionUID = 4185312480101104224L;

  protected HashMap<String, AttributeRecord> map = new HashMap<String, AttributeRecord>();

  @Override
  public boolean add(AttributeRecord e) {
    map.put(e.getName(), e);
    return super.add(e);
  }

  @Override
  public boolean addAll(Collection<? extends AttributeRecord> c) {
    for (AttributeRecord r : c) {
      map.put(r.getName(), r);
    }
    return super.addAll(c);
  }

  @Override
  public void clear() {
    map.clear();
    super.clear();
  }

  @Override
  public boolean remove(Object o) {
    if (!(o instanceof AttributeRecord)) return false;
    AttributeRecord toRemove = (AttributeRecord) o;

    if (super.remove(toRemove)) {
      map.remove(toRemove.getName());
      return true;
    }
    return false;
  }

  public AttributeRecord remove(String key) {
    AttributeRecord toRemove = map.remove(key);
    if (toRemove == null) return null;
    super.remove(toRemove);
    return toRemove;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    for (Object r : c) {
      if (r instanceof AttributeRecord) map.remove(((AttributeRecord) r).getName());
    }
    return super.removeAll(c);
  }

  public AttributeRecord get(String key) {
    return map.get(key);
  }
}
