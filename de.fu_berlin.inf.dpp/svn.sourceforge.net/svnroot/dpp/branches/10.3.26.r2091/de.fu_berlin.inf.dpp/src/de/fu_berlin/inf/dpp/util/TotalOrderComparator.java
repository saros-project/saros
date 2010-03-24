package de.fu_berlin.inf.dpp.util;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Based on TotalOrder from
 * http://gafter.blogspot.com/2007/03/compact-object-comparator.html by Neal
 * Gafter
 */
public class TotalOrderComparator<T> implements Comparator<T> {

    long nextNonce = 1;

    Map<T, Long> codes = new IdentityHashMap<T, Long>();

    protected synchronized Long getNonce(T o) {
        Long nonce = codes.get(o);
        if (nonce == null) {
            nonce = nextNonce++;
            codes.put(o, nonce);
        }
        return nonce;
    }

    public int compare(T o1, T o2) {
        if (o1 == o2)
            return 0;
        int i1 = System.identityHashCode(o1);
        int i2 = System.identityHashCode(o2);
        if (i1 != i2)
            return (i1 < i2) ? -1 : 1;
        Long l1 = getNonce(o1);
        Long l2 = getNonce(o2);
        return l1.compareTo(l2);
    }
}