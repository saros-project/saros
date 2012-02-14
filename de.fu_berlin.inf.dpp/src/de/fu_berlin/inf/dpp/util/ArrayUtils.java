package de.fu_berlin.inf.dpp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;

/**
 * @author bkahlert
 */
public class ArrayUtils {

    /**
     * Returns a list of elements that are instances of class clazz.
     * 
     * @param <T>
     * @param array
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getInstances(Object[] array, Class<T> clazz) {
        if (array == null)
            return null;

        List<T> instanceObjects = new ArrayList<T>();
        for (Object object : array) {
            if (clazz.isInstance(object)) {
                instanceObjects.add((T) object);
            }
        }

        return instanceObjects;
    }

    /**
     * Tries to adapt each selection item to adapter and returns all adapted
     * items.
     * 
     * @param objects
     * @param adapter
     *            to adapt each object to
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getAdaptableObjects(Object[] objects,
        Class<? extends T> adapter) {
        Set<T> adaptableObjects = new HashSet<T>(objects.length);

        for (Object object : objects) {
            T adaptedObject = (T) Platform.getAdapterManager().getAdapter(
                object, adapter);

            if (adaptedObject != null
                && !adaptableObjects.contains(adaptedObject)) {
                adaptableObjects.add(adaptedObject);
            }
        }

        return (List<T>) Arrays.asList(adaptableObjects.toArray());
    }

}
