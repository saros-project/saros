/*
 * Created on 20.01.2005
 *
 */
package de.fu_berlin.inf.dpp.observables;

import java.util.HashSet;
import java.util.Set;

/**
 * An ObservableValue is like a normal variable (you can get and set its value), but also allows
 * listeners to register for changes.
 *
 * @author oezbek
 */
public class ObservableValue<T> {

  Set<ValueChangeListener<? super T>> listeners = new HashSet<ValueChangeListener<? super T>>();

  T variable;

  /** Create a new ObservableValue with the given initial value. */
  public ObservableValue(T initialValue) {
    variable = initialValue;
  }

  /**
   * Utility function which adds the given Listener and directly afterwards calls the listener with
   * the current value.
   */
  public void addAndNotify(ValueChangeListener<? super T> listener) {
    listeners.add(listener);
    listener.setValue(variable);
  }

  /**
   * Register as a listener with this Observable value.
   *
   * <p>The listener will be called each time the {@link ObservableValue#setValue(Object)}
   */
  public void add(ValueChangeListener<? super T> listener) {
    listeners.add(listener);
  }

  public void remove(ValueChangeListener<? super T> listener) {
    listeners.remove(listener);
  }

  /**
   * Sets a new value for this ObservableValue and notify all listeners.
   *
   * <p>It is safe to call {@link ObservableValue#getValue()} to get the current value of the
   * ObservableValue from the listener.
   *
   * <p>Note: The listeners are called, even if the newValue is not changed! Thus calling <code>
   * setValue(getValue());</code> would trigger a call to all listeners.
   */
  public void setValue(T newValue) {
    variable = newValue;
    for (ValueChangeListener<? super T> vpl : listeners) {
      vpl.setValue(newValue);
    }
  }

  public T getValue() {
    return variable;
  }
}
