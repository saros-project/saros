package de.fu_berlin.inf.dpp.util;

import java.util.Objects;

/** Immutable pair class that holds a key of type K and a value of type V. */
public final class Pair<K, V> {

  /** The key part of this pair. Can be <code>null</code>. */
  public final K key;

  /** The value part of this pair. Can be <code>null</code>. */
  public final V value;

  public Pair(final K key, final V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public boolean equals(final Object obj) {

    if (!(obj instanceof Pair)) return false;

    Pair<?, ?> other = (Pair<?, ?>) obj;
    return Objects.equals(key, other.key) && Objects.equals(value, other.value);
  }

  @Override
  public String toString() {
    return "Pair [key=" + key + ", value=" + value + "]";
  }
}
