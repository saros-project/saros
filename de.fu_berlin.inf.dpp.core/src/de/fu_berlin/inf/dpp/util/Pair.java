/**
 * GASP-Reference Implementation for the Software Laboratory 2007.
 *
 * <p>http://www.inf.fu-berlin.de/w/SE/ProgrammierPraktikum2006
 *
 * <p>Copyright (C) 2007 Christopher Oezbek, Jan Sebastian Siwy, Olufemi Rosanwo, Nicolai Kamenzky
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package de.fu_berlin.inf.dpp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class that holds a value of Type P and one of Type V.
 *
 * @param <P>
 * @param <V>
 */
public class Pair<P, V> {

  public P p;

  public V v;

  public Pair(P p, V v) {
    this.p = p;
    this.v = v;
  }

  /** Returns a comparator for pairs based on the Ps in the Pairs only. */
  public static <P extends Comparable<P>, V> Comparator<Pair<P, V>> pCompare() {
    return new Comparator<Pair<P, V>>() {
      @Override
      public int compare(Pair<P, V> arg0, Pair<P, V> arg1) {
        return arg0.p.compareTo(arg1.p);
      }
    };
  }

  public static <P, V> Comparator<Pair<P, V>> pCompare(final Comparator<P> comp) {
    return new Comparator<Pair<P, V>>() {
      @Override
      public int compare(Pair<P, V> arg0, Pair<P, V> arg1) {
        return comp.compare(arg0.p, arg1.p);
      }
    };
  }

  public Pair<V, P> flip() {
    return new Pair<V, P>(v, p);
  }

  /** Will return a list that has P and V switched. */
  public static <P, V> List<Pair<V, P>> flipList(List<Pair<P, V>> list) {
    LinkedList<Pair<V, P>> result = new LinkedList<Pair<V, P>>();
    for (Pair<P, V> pair : list) result.add(pair.flip());
    return result;
  }

  /**
   * Given a list of pairs, will return a list where pairs containing the same P will be merged
   * together to form a pair with this P and a list of all the Vs associated with this P.
   *
   * <p>This operation is guaranteed to be stable (the ordering of pairs with the same P is
   * unaltered).
   *
   * @param <P>
   * @param <V>
   * @param list
   * @return
   */
  public static <P extends Comparable<P>, V> List<Pair<P, List<V>>> disjointPartition(
      List<Pair<P, V>> list) {

    List<Pair<P, List<V>>> result = new LinkedList<Pair<P, List<V>>>();

    Comparator<Pair<P, V>> c = Pair.pCompare();
    Collections.sort(list, Collections.reverseOrder(c));

    Iterator<Pair<P, V>> i = list.iterator();

    List<V> vs;

    if (i.hasNext()) {
      Pair<P, V> first = i.next();
      P last = first.p;
      vs = new LinkedList<V>();
      vs.add(first.v);

      while (i.hasNext()) {
        Pair<P, V> next = i.next();
        if (last.compareTo(next.p) == 0) {
          vs.add(next.v);
        } else {
          result.add(new Pair<P, List<V>>(last, vs));
          vs = new LinkedList<V>();
          last = next.p;
          vs.add(next.v);
        }
      }
      result.add(new Pair<P, List<V>>(last, vs));
    }
    return result;
  }

  public static <P, V extends Comparable<V>> Comparator<Pair<P, V>> vCompare() {
    return new Comparator<Pair<P, V>>() {
      @Override
      public int compare(Pair<P, V> arg0, Pair<P, V> arg1) {
        return arg0.v.compareTo(arg1.v);
      }
    };
  }

  public static <P, V> Comparator<Pair<P, V>> vCompare(final Comparator<V> vComp) {
    return new Comparator<Pair<P, V>>() {
      @Override
      public int compare(Pair<P, V> arg0, Pair<P, V> arg1) {
        return vComp.compare(arg0.v, arg1.v);
      }
    };
  }

  /**
   * Given a list of Ps and a list of Vs will return a list of Pairs. If the list are of different
   * length null values used to fill.
   */
  public static <P, V> List<Pair<P, V>> zip(List<P> ps, List<V> vs) {

    List<Pair<P, V>> result = new LinkedList<Pair<P, V>>();

    Iterator<P> pI = ps.iterator();
    Iterator<V> vI = vs.iterator();

    while (pI.hasNext()) {
      V nextV = (vI.hasNext() ? vI.next() : null);
      result.add(new Pair<P, V>(pI.next(), nextV));
    }
    while (vI.hasNext()) {
      result.add(new Pair<P, V>(null, vI.next()));
    }
    return result;
  }

  /** From a list of pairs, returns a list of the P elements in the list. */
  public static <P, V> List<P> pList(List<Pair<P, V>> list) {

    List<P> result = new LinkedList<P>();

    for (Pair<P, V> pair : list) {
      result.add(pair.p);
    }
    return result;
  }

  public static <P, V> List<V> vList(List<Pair<P, V>> list) {

    List<V> result = new LinkedList<V>();

    for (Pair<P, V> pair : list) {
      result.add(pair.v);
    }
    return result;
  }

  @Override
  public String toString() {
    return new StringBuffer().append('<').append(p).append(',').append(v).append('>').toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((p == null) ? 0 : p.hashCode());
    result = prime * result + ((v == null) ? 0 : v.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof Pair<?, ?>)) return false;
    Pair<?, ?> other = (Pair<?, ?>) obj;
    if (p == null) {
      if (other.p != null) return false;
    } else if (!p.equals(other.p)) return false;
    if (v == null) {
      if (other.v != null) return false;
    } else if (!v.equals(other.v)) return false;
    return true;
  }

  public static <P, V> List<Pair<P, V>> map(Collection<V> vs, Function<V, P> function) {
    List<Pair<P, V>> result = new ArrayList<Pair<P, V>>(vs.size());
    for (V v : vs) {
      result.add(new Pair<P, V>(function.apply(v), v));
    }
    return result;
  }

  public static <P extends Comparable<P>, V> List<Pair<P, List<V>>> partition(
      Collection<V> input, Function<V, P> function) {

    return disjointPartition(map(input, function));
  }

  public static <P, V> List<V> expand(
      Collection<P> input, Function<P, ? extends Collection<V>> function) {
    List<V> result = new ArrayList<V>(input.size());
    for (P p : input) {
      result.addAll(function.apply(p));
    }
    return result;
  }

  public static <P, V> Collection<Pair<P, V>> cross(P p, Collection<V> vs) {
    List<Pair<P, V>> result = new ArrayList<Pair<P, V>>(vs.size());
    for (V v : vs) {
      result.add(new Pair<P, V>(p, v));
    }
    return result;
  }

  public static <P, V> Collection<Pair<V, P>> cross(Collection<V> vs, P p) {
    List<Pair<V, P>> result = new ArrayList<Pair<V, P>>(vs.size());
    for (V v : vs) {
      result.add(new Pair<V, P>(v, p));
    }
    return result;
  }
}
