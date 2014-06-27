/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.util;

import de.fu_berlin.inf.dpp.util.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Map which will automatically insert a value for a given key if the Map does
 * not already contain one.
 * <p/>
 * This Map is backed by a HashMap and inherits all characteristics by it.
 */
public class AutoHashMap<K, V> implements Map<K, V> {

    /**
     * The internal Map used for storing Key,Value pairs in
     */
    protected Map<K, V> backing = new HashMap<K, V>();

    /**
     * The provider used when a value of type V is needed for a key k of type K
     * (this happens if get(k) is called but containsKey(k) returns false)
     */
    protected Function<K, V> provider;

    public AutoHashMap(Function<K, V> provider) {
        this.provider = provider;
    }

    /**
     * Returns an AutoHashMap which automatically will initialize an
     * ArrayList<V> when queried for a key for which there is no value.
     */
    public static <K, V> AutoHashMap<K, List<V>> getListHashMap() {
        return new AutoHashMap<K, List<V>>(new Function<K, List<V>>() {
            @Override
            public List<V> apply(K u) {
                return new ArrayList<V>();
            }
        });
    }

    /**
     * Returns an AutoHashMap which automatically will initialize a
     * LinkedBlockingQueue<V> when queried for a key for which there is no
     * value.
     */
    public static <K, V> AutoHashMap<K, BlockingQueue<V>> getBlockingQueueHashMap() {
        return new AutoHashMap<K, BlockingQueue<V>>(
            new Function<K, BlockingQueue<V>>() {
                @Override
                public BlockingQueue<V> apply(K u) {
                    return new LinkedBlockingQueue<V>();
                }
            }
        );
    }

    /**
     * Returns an AutoHashMap which automatically will initialize a HashSet<V>
     * when queried for a key for which there is no value.
     */
    public static <K, V> AutoHashMap<K, Set<V>> getSetHashMap() {
        return new AutoHashMap<K, Set<V>>(new Function<K, Set<V>>() {
            @Override
            public Set<V> apply(K u) {
                return new HashSet<V>();
            }
        });
    }

    @Override @SuppressWarnings("unchecked")
    public V get(Object k) {
        if (!containsKey(k)) {
            put((K) k, provider.apply((K) k));
        }
        return backing.get(k);
    }

    @Override
    public void clear() {
        backing.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return backing.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backing.containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return backing.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return backing.equals(o);
    }

    @Override
    public int hashCode() {
        return backing.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return backing.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return backing.keySet();
    }

    @Override
    public V put(K key, V value) {
        return backing.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> t) {
        backing.putAll(t);
    }

    @Override
    public V remove(Object key) {
        return backing.remove(key);
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public Collection<V> values() {
        return backing.values();
    }

}
