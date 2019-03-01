/*
 * Created on 20.01.2005
 *
 */
package de.fu_berlin.inf.dpp.observables;

/**
 * Listener interface for listening to changes of a {@link ObservableValue}.
 *
 * @author oezbek
 */
public interface ValueChangeListener<T> {

  /**
   * This method is called by a {@link ObservableValue} when its value changes.
   *
   * @param newValue
   */
  public void setValue(T newValue);
}
