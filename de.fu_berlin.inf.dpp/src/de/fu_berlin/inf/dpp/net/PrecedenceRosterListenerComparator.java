/**
 * 
 */
package de.fu_berlin.inf.dpp.net;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.fu_berlin.inf.dpp.project.SarosRosterListener;
import de.fu_berlin.inf.dpp.util.TotalOrderComparator;

/**
 * Comparator which sorts IRosterListener by the order they should be notified
 */
public class PrecedenceRosterListenerComparator implements
    Comparator<IRosterListener> {

    protected List<Class<? extends IRosterListener>> precedence = new ArrayList<Class<? extends IRosterListener>>();

    protected Comparator<IRosterListener> totalOrder = new TotalOrderComparator<IRosterListener>();

    public PrecedenceRosterListenerComparator() {
        precedence.add(SarosRosterListener.Listener.class);
        precedence.add(IRosterListener.class);
    }

    public int precedenceIndex(IRosterListener listener) {
        int i = 0;
        for (Class<? extends IRosterListener> c : precedence) {
            if (c.isAssignableFrom(listener.getClass())) {
                return i;
            }
            i++;
        }
        return Integer.MAX_VALUE - 1;
    }

    public int compare(IRosterListener o1, IRosterListener o2) {

        int p1 = precedenceIndex(o1);
        int p2 = precedenceIndex(o2);

        if (p1 == p2) {
            return totalOrder.compare(o1, o2);
        } else {
            return p1 - p2;
        }
    }
}