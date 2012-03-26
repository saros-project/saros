package de.fu_berlin.inf.dpp.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Each key of type K refers to a {@link List} of type V.
 * 
 * @param <K>
 *            type for the key elements
 * @param <V>
 *            type for the value list elements
 * 
 *            Example:<br />
 *            <code>MappedList<String, Integer> ml = new MappedList<String, Integer>();<br />
 *            Set&lt;String&gt; keys = ml.keySet();<br />
 *            Collection&lt;List&lt;Integer&gt;&gt; values = ml.values();
 */
public class MappedList<K, V> extends LinkedHashMap<K, List<V>> {
    private static final long serialVersionUID = 2L;

    public List<V> put(K key, V value) {
        List<V> l = get(key);
        if (l == null) {
            l = new LinkedList<V>();
            super.put(key, l);
        }
        l.add(value);
        return l;
    }

    @Override
    public List<V> put(K key, List<V> value) {
        List<V> l = get(key);
        if (l == null)
            return super.put(key, value);
        else
            l.addAll(value);
        return l;
    }

}
