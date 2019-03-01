package de.fu_berlin.inf.dpp.feedback;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum FeedbackInterval {
  EVERY(1),
  EVERY_THIRD(3),
  EVERY_FIFTH(5),
  EVERY_TENTH(10),
  EVERY_FIFTEENTH(15);

  public static final FeedbackInterval DEFAULT = EVERY_FIFTH;

  private static final Map<Integer, FeedbackInterval> lookup =
      new HashMap<Integer, FeedbackInterval>();

  static {
    for (FeedbackInterval f : EnumSet.allOf(FeedbackInterval.class)) {
      lookup.put(f.getInterval(), f);
    }
  }

  private int interval;

  private FeedbackInterval(int i) {
    this.interval = i;
  }

  public int getInterval() {
    return interval;
  }

  public int getIndex() {
    return this.ordinal();
  }

  @Override
  public String toString() {
    return this.name().toLowerCase().replace('_', ' ').concat(" session"); // $NON-NLS-1$
  }

  /**
   * Returns the enum for the given interval.
   *
   * @param interval
   * @return the enum for the interval, can't be null
   */
  public static FeedbackInterval getFromInterval(int interval) {
    FeedbackInterval[] values = FeedbackInterval.values();
    // make sure the given interval isn't too small or too big
    if (interval < values[0].interval) interval = values[0].interval;
    if (interval > values[values.length - 1].interval)
      interval = values[values.length - 1].interval;

    FeedbackInterval i = lookup.get(interval);
    // if the interval didn't exist in the map, return the default
    if (i == null) {
      return EVERY_FIFTH;
    }
    return i;
  }

  /**
   * Returns the enum for the given index.
   *
   * @param index
   * @return the enum for the index
   */
  public static FeedbackInterval getFromIndex(int index) {
    // make sure the index exists
    if (index < 0) index = 0;
    if (index > FeedbackInterval.values().length - 1) index = FeedbackInterval.values().length - 1;

    return FeedbackInterval.values()[index];
  }

  /**
   * Returns an array of Strings that contains the enum names in a user friendly text
   * representation.
   *
   * @return String array of enum names
   */
  public static String[] toStringArray() {
    FeedbackInterval[] intervals = FeedbackInterval.values();
    String[] strings = new String[intervals.length];
    for (int i = 0; i < intervals.length; i++) {
      strings[i] = intervals[i].toString();
    }
    return strings;
  }
}
