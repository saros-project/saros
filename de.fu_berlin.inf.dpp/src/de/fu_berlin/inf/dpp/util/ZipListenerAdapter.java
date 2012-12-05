package de.fu_berlin.inf.dpp.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This adapter provides a way to combine multiple {@linkplain ZipListener} into
 * one. Calls to {@linkplain #update(String)} or
 * {@linkplain #update(long, long)} are forwarded to the individual listeners.
 * 
 */
public class ZipListenerAdapter implements ZipListener {

    private List<ZipListener> zipListeners;

    public ZipListenerAdapter(Collection<ZipListener> zipListeners) {
        this.zipListeners = new ArrayList<ZipListener>(zipListeners);
    }

    @Override
    public boolean update(String filename) {
        boolean isCanceled = false;
        for (ZipListener listener : zipListeners) {
            isCanceled |= listener.update(filename);
        }

        return isCanceled;
    }

    @Override
    public boolean update(long totalRead, long totalSize) {
        boolean isCanceled = false;
        for (ZipListener listener : zipListeners) {
            isCanceled |= listener.update(totalRead, totalSize);
        }

        return isCanceled;
    }
}
