package de.fu_berlin.inf.dpp.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.limewire.collection.Function;

/**
 * An Iterator which is backed by another iterator but which transforms the
 * result using a given function.
 */
public class MappingIterator<U, T> implements Iterator<T> {

    protected Function<U, T> mapping;

    protected Iterator<U> backing;

    public MappingIterator(Iterator<U> u, Function<U, T> mapping) {
        this.backing = u;
        this.mapping = mapping;
    }

    public boolean hasNext() {
        return backing.hasNext();
    }

    public T next() throws NoSuchElementException {
        if (!hasNext())
            throw new NoSuchElementException();
        return mapping.apply(backing.next());
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
