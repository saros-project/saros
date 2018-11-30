package de.fu_berlin.inf.dpp.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;

/**
 * @author Björn Kahlert
 * @author Stefan Rossbach
 * @author Maria Spiering
 */
public class ArrayUtils {

  /**
   * Returns a list of elements that are instances of this class.
   *
   * @param objects
   * @param clazz
   * @return all objects that are instances of this class or <code>null</code> if the object array
   *     is <code>null</code>
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> getInstances(Object[] objects, Class<T> clazz) {
    if (objects == null) return null;

    List<T> instanceObjects = new ArrayList<T>();
    for (Object object : objects) {
      if (clazz.isInstance(object)) {
        instanceObjects.add((T) object);
      }
    }

    return instanceObjects;
  }

  /**
   * Tries to adapt each object to the given class.
   *
   * @param objects objects that are tried to adapted
   * @param clazz class to adapt each object to
   * @return all objects that were adapted or <code>null</code> if the object array is <code>null
   *     </code>
   * @deprecated use {@link #getAdaptableObjects(Object[] objects, Class clazz, IAdapterManager
   *     adapterManager)}
   */
  @Deprecated
  public static <T> List<T> getAdaptableObjects(Object[] objects, Class<? extends T> clazz) {
    return getAdaptableObjects(objects, clazz, Platform.getAdapterManager());
  }

  /**
   * Tries to adapt each object to the given class.
   *
   * @param objects objects that are tried to adapted
   * @param clazz class to adapt each object to
   * @param adapterManager the adapterManager that is used to adapt the objects
   * @return all objects that were adapted or <code>null</code> if the object array is <code>null
   *     </code>
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> getAdaptableObjects(
      Object[] objects, Class<? extends T> clazz, IAdapterManager adapterManager) {

    if (objects == null) return null;

    Set<T> adaptableObjects = new HashSet<T>(objects.length);

    for (Object object : objects) {
      T adaptedObject = (T) adapterManager.getAdapter(object, clazz);

      if (adaptedObject != null && !adaptableObjects.contains(adaptedObject)) {
        adaptableObjects.add(adaptedObject);
      }
    }

    return new ArrayList<T>(adaptableObjects);
  }
}
