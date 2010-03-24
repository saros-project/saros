/*
 * Created on 20.01.2005
 *
 */
package de.fu_berlin.inf.dpp.util;

import java.util.HashSet;
import java.util.Set;

/**
 * An Observable Value whose changes can be vetoed by its Listeners.
 * 
 * @author oezbek
 */
public class VetoableObservableValue<T> extends ObservableValue<T> {

    Set<VetoableValueChangeListener<? super T>> vetoables = new HashSet<VetoableValueChangeListener<? super T>>();

    public VetoableObservableValue(T variable) {
        super(variable);
    }

    public void addAndNotify(VetoableValueChangeListener<? super T> listener) {
        vetoables.add(listener);
        listener.setVariable(variable, null);
    }

    public void add(VetoableValueChangeListener<? super T> listener) {
        vetoables.add(listener);
    }

    public void remove(VetoableValueChangeListener<? super T> listener) {
        vetoables.remove(listener);
    }

    @Override
    public boolean setValue(T variable) {
        T oldValue = this.variable;
        this.variable = variable;

        for (VetoableValueChangeListener<? super T> vpl : vetoables) {
            if (!vpl.setVariable(variable, oldValue)) {
                this.variable = oldValue;
                return false;
            }
        }

        for (ValueChangeListener<? super T> vpl : nonVetoables) {
            vpl.setValue(variable);
        }
        return true;
    }
}
