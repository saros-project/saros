package de.fu_berlin.inf.dpp.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class MappedList<T> extends LinkedHashMap<String, List<T>> {
    private static final long serialVersionUID = 1L;

    public List<T> put(String key, T value) {
        List<T> l = get(key);
        if (l == null) {
            l = new LinkedList<T>();
            super.put(key, l);
        }
        l.add(value);
        return l;
    }

    @Override
    public List<T> put(String key, List<T> value) {
        List<T> l = get(key);
        if (l == null)
            return super.put(key, value);
        else
            l.addAll(value);
        return l;
    }

}
