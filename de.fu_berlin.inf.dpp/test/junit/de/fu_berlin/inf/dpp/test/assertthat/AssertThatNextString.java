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
public class AssertThatNextString extends AssertThatNextGeneric<String> {

    public AssertThatNextString(ValueProducer<String> stringValueProducer) {
        super(stringValueProducer);
    }

    public void containsString(final String str) {
        waitFor(new TCondition() {
            public boolean isFullfilled() throws Exception {
                return (producer.value()).contains(str);
            }

            public String getFailureMessage() {
                String result = "";
                try {
                    String val = producer.value();
                    result = "expected element containing string <" + str + "> but was:<" + val + ">";
                } catch (Exception e) {
                    result = "Generating value produced an exception: " + e.getMessage();
                    e.printStackTrace();
                }

                return result;
            }
        });
    }

}
