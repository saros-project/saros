package de.fu_berlin.inf.dpp.whiteboard.sxe.util;

import de.fu_berlin.inf.dpp.whiteboard.sxe.records.NodeRecord;
import java.util.Collection;
import java.util.Random;
import java.util.TreeSet;

/**
 * This extension of a TreeSet ensures that records with empty primary-weight are initialized to be
 * added at the end.
 *
 * <p>Technically it automatically sets the primary-weight of a record if null.
 *
 * @author jurke
 */
public class NodeSet<T extends NodeRecord> extends TreeSet<T> {

  private static final long serialVersionUID = -3895227390262044221L;

  private static final Random RANDOM = new Random();

  /** Steps between default primary-weights */
  public static final float STEP_TO_NEXT_PRIMARY_WEIGHT = 10f;

  /**
   * This method is used to increase the possibility to have different primary-weight values for
   * every peer, even on concurrent creation.</br>
   *
   * <p>This is useful because if comparing per secondary-weight (the RIDs) only, it's more effort
   * to insert an element in between.
   *
   * @return a random float with two decimal places
   */
  protected static float random2Decimals() {
    return Math.round(RANDOM.nextFloat() * 100f) / 100f;
  }

  /**
   * Adds the record to this set. If no primary weight is specified, it will be added to the end.
   */
  @Override
  public boolean add(T e) {
    if (e.getPrimaryWeight() == null) {
      e.setPrimaryWeight(nextPrimaryWeight());
    }
    return super.add(e);
  }

  /** Note: A node without primary-weight cannot be contained */
  @Override
  public boolean contains(Object o) {
    if (o instanceof NodeRecord) {
      if (((NodeRecord) o).getPrimaryWeight() == null) return false;
      return super.contains(o);
    }
    return false;
  }

  /** @return the next primary-weight to append a child at the end */
  public Float nextPrimaryWeight() {
    if (isEmpty()) return random2Decimals();
    else return last().getPrimaryWeight() + STEP_TO_NEXT_PRIMARY_WEIGHT + random2Decimals();
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    boolean changed = false;
    for (T t : c) {
      changed |= add(t);
    }
    return changed;
  }
}
