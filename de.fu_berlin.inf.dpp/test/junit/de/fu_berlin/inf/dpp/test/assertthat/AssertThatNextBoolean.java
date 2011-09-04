package de.fu_berlin.inf.dpp.test.assertthat;

import static de.fu_berlin.inf.dpp.test.assertthat.AssertThat.assertThat;


/**
 * Fluent-builder for creating an assertion which periodicly
 * checks if its proposition is fullfilled.
 * <p/>
 * Waits max 5 seconds.
 *
 * @author cordes
 */
public class AssertThatNextBoolean extends AssertThatNextGeneric<Boolean> {

    public AssertThatNextBoolean(ValueProducer<Boolean> booleanValueProducer) {
        super(booleanValueProducer);
    }

    public void isTrue() {
        assertThat(producer)
                .isEquals(true);
    }

    public void isFalse() {
        assertThat(producer)
                .isEquals(false);
    }
}
