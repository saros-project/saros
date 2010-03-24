package de.fu_berlin.inf.dpp.project.internal;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A pool of free colors.
 */
public class FreeColors {
    protected final int maxColorID;
    protected final Queue<Integer> freeColors;

    /**
     * Creates the pool of free color IDs.
     * 
     * @param maxColorID
     *            The highest color ID.
     */
    public FreeColors(int maxColorID) {
        assert maxColorID > 1;
        this.maxColorID = maxColorID;
        freeColors = new LinkedBlockingQueue<Integer>();
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
    public int get() {
        try {
            return freeColors.remove();
        } catch (NoSuchElementException e) {
            return maxColorID;
        }
    }

    /**
     * Return a color ID to the pool.
     * 
     * @param colorID
     *            Color ID to return.
     */
    public void add(int colorID) {
        if ((colorID > 0) && (colorID < maxColorID)
            && (!freeColors.contains(colorID))) {
            freeColors.add(colorID);
        }
    }
}
