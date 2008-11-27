/*
 * Created on 20.01.2005
 *
 */
package de.fu_berlin.inf.dpp.util;

import java.util.HashSet;
import java.util.Set;

public class VetoableVariableProxy<T> extends VariableProxy<T> {

    Set<VetoableVariableProxyListener<? super T>> vetoables = new HashSet<VetoableVariableProxyListener<? super T>>();

    public VetoableVariableProxy(T variable) {
	super(variable);
    }

    public void addAndNotify(VetoableVariableProxyListener<? super T> listener) {
	vetoables.add(listener);
	listener.setVariable(variable, null);
    }

    public void add(VetoableVariableProxyListener<? super T> listener) {
	vetoables.add(listener);
    }

    public void remove(VetoableVariableProxyListener<? super T> listener) {
	vetoables.remove(listener);
    }

    @Override
    public boolean setVariable(T variable) {
	T oldValue = this.variable;
	this.variable = variable;

	for (VetoableVariableProxyListener<? super T> vpl : vetoables) {
	    if (!vpl.setVariable(variable, oldValue)) {
		this.variable = oldValue;
		return false;
	    }
	}

	for (VariableProxyListener<? super T> vpl : nonVetoables) {
	    vpl.setVariable(variable);
	}
	return true;
    }
}
