package de.fu_berlin.inf.dpp.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator which iterates over an array
 */
public class ArrayIterator<T> implements Iterator<T> {

    protected T[] array;
    protected int i;
    protected int length;

    /**
     * An iterator over the given array
     */
    public ArrayIterator(T[] elements) {
        this(elements, 0, elements.length);
    }

    /**
     * An iterator over the given Returns new array enumeration over the given
     * object array
     * 
     * @param until
     *            is excluding
     */
    public ArrayIterator(T[] array, int from, int until) {
        super();
        this.array = array;
        this.i = from;
        this.length = until;
    }

    public boolean hasNext() {
        return array != null && i < length;
    }

    public T next() throws NoSuchElementException {
        if (!hasNext())
            throw new NoSuchElementException();
        return array[i++];
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
