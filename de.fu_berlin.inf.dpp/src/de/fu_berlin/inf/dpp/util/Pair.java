/**
 * GASP-Reference Implementation for the Software Laboratory 2007.
 * 
 * http://projects.mi.fu-berlin.de/w/bin/view/SE/ProgrammierPraktikum2006
 * 
 * Copyright (C) 2007 Christopher Oezbek, Jan Sebastian Siwy, Olufemi
 * Rosanwo, Nicolai Kamenzky
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package de.fu_berlin.inf.dpp.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

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

    public static <P extends Comparable<P>, V> Comparator<Pair<P, V>> pCompare() {
        return new Comparator<Pair<P, V>>() {
            public int compare(Pair<P, V> arg0, Pair<P, V> arg1) {
                return arg0.p.compareTo(arg1.p);
            }
        };
    }

    public static <P, V> Comparator<Pair<P, V>> pCompare(
        final Comparator<P> comp) {
        return new Comparator<Pair<P, V>>() {
            public int compare(Pair<P, V> arg0, Pair<P, V> arg1) {
                return comp.compare(arg0.p, arg1.p);
            }
        };
    }

    public Pair<V, P> flip() {
        return new Pair<V, P>(v, p);
    }

    public static <P, V> List<Pair<V, P>> flipList(List<Pair<P, V>> list) {
        LinkedList<Pair<V, P>> result = new LinkedList<Pair<V, P>>();
        for (Pair<P, V> pair : list)
            result.add(pair.flip());
        return result;
    }

    /**
     * Given a list of pairs, will return a list where pairs containing the same
     * P will be merged together to form a pair with this P and a list of all
     * the Vs associated with this P.
     * 
     * This operation is garantueed to be stable (the ordering of pairs with the
     * same P is unaltered).
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
            public int compare(Pair<P, V> arg0, Pair<P, V> arg1) {
                return arg0.v.compareTo(arg1.v);
            }
        };
    }

    public static <P, V> Comparator<Pair<P, V>> vCompare(
        final Comparator<V> vComp) {
        return new Comparator<Pair<P, V>>() {
            public int compare(Pair<P, V> arg0, Pair<P, V> arg1) {
                return vComp.compare(arg0.v, arg1.v);
            }
        };
    }

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
        return new StringBuffer().append('<').append(p).append(',').append(v)
            .append('>').toString();
    }

    @Override
    public int hashCode() {
        return (this.p == null ? 0 : this.p.hashCode())
            | (this.v == null ? 0 : this.v.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair))
            return false;

        Pair<?, ?> other = (Pair<?, ?>) o;
        return ObjectUtils.equals(this.p, other.p)
            && ObjectUtils.equals(this.v, other.v);
    }
}
