package de.fu_berlin.inf.dpp.whiteboard.sxe.util;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.SetRecord;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A specialized list to ensure a proper set record list with increasing versions by overriding
 * add() methods and throwing an IllegalArgumentException;
 *
 * <p>It also overrides the contains() method to start searching at the and to break if version
 * reached or smaller than the provided one.
 */
public class SetRecordList extends LinkedList<SetRecord> {

  private static final long serialVersionUID = 1L;

  @Override
  public boolean contains(Object o) {
    if (!(o instanceof SetRecord)) return false;

    ListIterator<SetRecord> it = listIterator(size());
    SetRecord r = (SetRecord) o;
    SetRecord previous;

    while (it.hasPrevious()) {
      previous = it.previous();
      if (previous.getVersion() == r.getVersion()) return r.equals(previous);
      if (previous.getVersion() < r.getVersion()) break;
    }

    return false;
  }

  @Override
  public boolean add(SetRecord r) {
    if (!isEmpty())
      if (r.getVersion() <= getLast().getVersion())
        throw new IllegalArgumentException("Cannot add a set-record with version<=last");
    return super.add(r);
  }

  @Override
  public void add(int i, SetRecord r) {
    if (i != size()) throw new IllegalArgumentException("Can only append set-records to the end");
    add(r);
  }

  @Override
  public void addFirst(SetRecord r) {
    if (!isEmpty()) throw new IllegalArgumentException("Can only append set-records to the end");
    add(r);
  }

  // @Override
  // public boolean addAll(Collection<? extends SetRecord> arg0) {
  // return false;
  // }
  //
  // @Override
  // public boolean addAll(int arg0, Collection<? extends SetRecord> arg1) {
  // return false;
  // }

}
