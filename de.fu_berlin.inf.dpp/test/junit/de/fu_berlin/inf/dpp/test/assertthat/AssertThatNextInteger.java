package de.fu_berlin.inf.dpp.test.assertthat;

import de.fu_berlin.inf.dpp.test.util.TCondition;

import static de.fu_berlin.inf.dpp.test.util.SarosTestUtils.waitFor;

/**
 * Fluent-builder for creating an assertion which periodicly
 * checks if its proposition is fullfilled.
 * <p/>
 * Waits max 5 seconds.
 *
 * @author cordes
 */
public class AssertThatNextInteger extends AssertThatNextGeneric<Integer> {

    public AssertThatNextInteger(ValueProducer<Integer> integerValueProducer) {
        super(integerValueProducer);
    }

    public void isGreaterThan(final Integer integer) {
        waitFor(new TCondition() {
            public boolean isFullfilled() throws Exception {
                return producer.value() > integer;
            }

            public String getFailureMessage() {
                String result;
                try {
                    Integer val = producer.value();
                    result = "expected value greater than <" + integer + "> but was:<" + val + ">";
                } catch (Exception e) {
                    result = "Generating value produced an exception: " + e.getMessage();
                    e.printStackTrace();
                }
                return result;
            }
        });
    }

    public void isLessThan(final Integer integer) {
        waitFor(new TCondition() {
            public boolean isFullfilled() throws Exception {
                return producer.value() < integer;
            }

            public String getFailureMessage() {
                String result;
                try {
                    Integer val = producer.value();
                    result = "expected value greater than <" + integer + "> but was:<" + val + ">";
                } catch (Exception e) {
                    result = "Generating value produced an exception: " + e.getMessage();
                    e.printStackTrace();
                }
                return result;
            }
        });
    }
}
