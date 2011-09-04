package de.fu_berlin.inf.dpp.test.assertthat;

/**
 * This class provides the beginning of the fluent interface for async-testing.
 * 
 * If you want to test async behaviour you must wait sometimes for the
 * fullfillment of a condition. With that fluent you can simple express
 * assertions which wait for some seconds before they prompt you with an
 * error-message.
 * 
 * Example:
 * 
 * <pre>
 * {@code
 *    assertThat(newValueProducer<Integer>() {
 *         public Integer value() throws Exception {
 *                 return 2;
 *         }
 *      }).isEquals(2); 
 * }
 * </pre>
 * 
 * @author cordes
 */
public final class AssertThat {
    private AssertThat() {
        //
    }

    public static <T> AssertThatNextGeneric<T> assertThat(
        ValueProducer<T> producer) {
        return new AssertThatNextGeneric<T>(producer);
    }

    public static AssertThatNextInteger assertThatInt(
        ValueProducer<Integer> producer) {
        return new AssertThatNextInteger(producer);
    }

    public static AssertThatNextString assertThatStr(
        ValueProducer<String> producer) {
        return new AssertThatNextString(producer);
    }

    public static AssertThatNextBoolean assertThatBoolean(
        ValueProducer<Boolean> producer) {
        return new AssertThatNextBoolean(producer);
    }

}
