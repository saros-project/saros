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
public class AssertThatNextGeneric<T> {

    protected ValueProducer<T> producer;

    public AssertThatNextGeneric(ValueProducer<T> producer) {
        this.producer = producer;
    }

    public void isEquals(final T expected) {
        waitFor(new TCondition() {
            public boolean isFullfilled() throws Exception {
                return expected.equals(producer.value());
            }

            public String getFailureMessage() {
                String result;
                try {
                    T val = producer.value();
                    result = "expected <" + expected.toString() + "> but was:<" + val + ">";
                } catch (Exception e) {
                    result = "Generating value produced an exception: " + e.getMessage();
                    e.printStackTrace();
                }

                return result;
            }
        });
    }

    public void isNotNull() {
        waitFor(new de.fu_berlin.inf.dpp.test.util.TCondition() {
            public boolean isFullfilled() throws Exception {
                return producer.value() != null;
            }

            public String getFailureMessage() {
                return "expected value not null";
            }
        });
    }

    public void isNull() {
        waitFor(new TCondition() {
            public boolean isFullfilled() throws Exception {
                return producer.value() == null;
            }

            public String getFailureMessage() {
                return "expected value null";
            }
        });
    }

    public void isSame(final T expected) {
        waitFor(new TCondition() {
            public boolean isFullfilled() throws Exception {
                return producer.value() == expected;
            }

            public String getFailureMessage() {
                String result = "";
                try {
                    T val = producer.value();
                    result = "expected same <" + expected.toString() + "> was not:<" + val + ">";
                } catch (Exception e) {
                    result = "Generating value produced an exception: " + e.getMessage();
                    e.printStackTrace();
                }

                return result;
            }
        });
    }


}
