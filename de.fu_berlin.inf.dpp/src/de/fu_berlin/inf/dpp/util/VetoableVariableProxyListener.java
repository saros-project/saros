/*
 * Created on 20.01.2005
 *
 */
package de.fu_berlin.inf.dpp.util;

/**
 * @author oezbek
 * 
 */
public interface VetoableVariableProxyListener<T> {

    public boolean setVariable(T newValue, T oldValue);

}
