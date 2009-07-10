package de.fu_berlin.inf.dpp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * HashMap which will automatically insert a value for a given key if the Map
 * does not already contain one.
 */
public class AutoHashMap<K, V> extends HashMap<K, V> {

    Function<K, V> provider;

    public static <K, V> AutoHashMap<K, List<V>> getListHashMap() {
        return new AutoHashMap<K, List<V>>(new Function<K, List<V>>() {
            public List<V> apply(K u) {
                return new ArrayList<V>();
            }

        });
    }

    public AutoHashMap(Function<K, V> provider) {
        this.provider = provider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object k) {
        if (!containsKey(k)) {
            put((K) k, provider.apply((K) k));
        }
        return super.get(k);
    }
}
