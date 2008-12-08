/*
 * Created on 20.01.2005
 *
 */
package de.fu_berlin.inf.dpp.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author oezbek
 * 
 */
public class VariableProxy<T> {

    Set<VariableProxyListener<? super T>> nonVetoables = new HashSet<VariableProxyListener<? super T>>();

    T variable;

    public VariableProxy(T variable) {
        super();
        this.variable = variable;
    }

    public void addAndNotify(VariableProxyListener<? super T> listener) {
        nonVetoables.add(listener);
        listener.setVariable(variable);
    }

    public void add(VariableProxyListener<? super T> listener) {
        nonVetoables.add(listener);
    }

    public void remove(VariableProxyListener<? super T> listener) {
        nonVetoables.remove(listener);
    }

    /**
     * 
     * @return Returns true if the setting of the variable was successful.
     * 
     */
    public boolean setVariable(T variable) {
        return setVariable(variable, null);
    }

    public boolean setVariable(T variable,
            VariableProxyListener<? super T> exclude) {
        this.variable = variable;
        for (VariableProxyListener<? super T> vpl : nonVetoables) {
            if (vpl == exclude)
                continue;
            vpl.setVariable(variable);
        }
        return true;
    }

    public T getVariable() {
        return variable;
    }
}
