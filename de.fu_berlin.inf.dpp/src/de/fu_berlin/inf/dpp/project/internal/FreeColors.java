package de.fu_berlin.inf.dpp.project.internal;

import java.util.Deque;
import java.util.LinkedList;

/**
 * A pool of free colors.
 */
public class FreeColors {
    protected final int maxColorID;
    protected final Deque<Integer> freeColors;

    /**
     * Creates the pool of free color IDs.
     * 
     * @param maxColorID
     *            The highest color ID.
     */
    public FreeColors(int maxColorID) {
        assert maxColorID > 1;
        this.maxColorID = maxColorID;
        freeColors = new LinkedList<Integer>();
        for (int i = 1; i < maxColorID; ++i) {
            freeColors.add(i);
        }
    }

    /**
     * Gets a color ID from the pool. If the pool is empty, the highest color ID
     * will be returned.
     * 
     * @return color ID.
     */
    public synchronized int get() {
        if (freeColors.isEmpty())
            return maxColorID;

        return freeColors.removeFirst();
    }

    /**
     * Return a color ID to the pool.
     * 
     * @param colorID
     *            Color ID to return.
     */
    public synchronized void add(int colorID) {
        if (!freeColors.contains(colorID))
            freeColors.addFirst(colorID);
    }
}
