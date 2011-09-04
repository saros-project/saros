package de.fu_berlin.inf.dpp.test.assertthat;

/**
 * A ValueProducer for producing periodicly a value.
 *
 * @see de.fu_berlin.inf.dpp.test.assertthat.AssertThat
 *
 * @author cordes
 */
public abstract class ValueProducer<T> {

    public abstract T value() throws Exception;

}
