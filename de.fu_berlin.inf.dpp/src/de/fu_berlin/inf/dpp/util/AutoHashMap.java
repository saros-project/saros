package de.fu_berlin.inf.dpp.util;

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
 * 
 * This Map is backed by an HashMap and inherits all characteristics by it.
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

    /**
     * Returns a AutoHashMap which automatically will initialize an ArrayList<V>
     * when queried for a key for which there is no value.
     */
    public static <K, V> AutoHashMap<K, List<V>> getListHashMap() {
        return new AutoHashMap<K, List<V>>(new Function<K, List<V>>() {
            public List<V> apply(K u) {
                return new ArrayList<V>();
            }
        });
    }

    /**
     * Returns a AutoHashMap which automatically will initialize an
     * LinkedBlockingQueue<V> when queried for a key for which there is no
     * value.
     */
    public static <K, V> AutoHashMap<K, BlockingQueue<V>> getBlockingQueueHashMap() {
        return new AutoHashMap<K, BlockingQueue<V>>(
            new Function<K, BlockingQueue<V>>() {
                public BlockingQueue<V> apply(K u) {
                    return new LinkedBlockingQueue<V>();
                }
            });
    }

    /**
     * Returns a AutoHashMap which automatically will initialize an HashSet<V>
     * when queried for a key for which there is no value.
     */
    public static <K, V> AutoHashMap<K, Set<V>> getSetHashMap() {
        return new AutoHashMap<K, Set<V>>(new Function<K, Set<V>>() {
            public Set<V> apply(K u) {
                return new HashSet<V>();
            }
        });
    }

    public AutoHashMap(Function<K, V> provider) {
        this.provider = provider;
    }

    @SuppressWarnings("unchecked")
    public V get(Object k) {
        if (!containsKey(k)) {
            put((K) k, provider.apply((K) k));
        }
        return backing.get(k);
    }

    public void clear() {
        backing.clear();
    }

    public boolean containsKey(Object key) {
        return backing.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return backing.containsValue(value);
    }

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

    public boolean isEmpty() {
        return backing.isEmpty();
    }

    public Set<K> keySet() {
        return backing.keySet();
    }

    public V put(K key, V value) {
        return backing.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        backing.putAll(t);
    }

    public V remove(Object key) {
        return backing.remove(key);
    }

    public int size() {
        return backing.size();
    }

    public Collection<V> values() {
        return backing.values();
    }

}
