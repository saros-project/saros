package de.fu_berlin.inf.dpp.util;

import java.util.Objects;

/** @deprecated Will be replaced with Apache Pair implementation */
@Deprecated
public final class Pair<K, V> {

  /** The key part of this pair. Can be <code>null</code>. */
  private final K key;

  /** The value part of this pair. Can be <code>null</code>. */
  private final V value;

  public static <K, V> Pair<K, V> of(final K key, final V value) {
    return new Pair<>(key, value);
  }

  private Pair(final K key, final V value) {
    this.key = key;
    this.value = value;
  }

  public K getLeft() {
    return getKey();
  }

  public V getRight() {
    return getValue();
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
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
