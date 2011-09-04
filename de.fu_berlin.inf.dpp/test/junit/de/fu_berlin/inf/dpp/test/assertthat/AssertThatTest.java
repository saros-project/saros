package de.fu_berlin.inf.dpp.test.assertthat;

import static de.fu_berlin.inf.dpp.test.assertthat.AssertThat.assertThat;
import static de.fu_berlin.inf.dpp.test.assertthat.AssertThat.assertThatBoolean;
import static de.fu_berlin.inf.dpp.test.assertthat.AssertThat.assertThatInt;
import static de.fu_berlin.inf.dpp.test.assertthat.AssertThat.assertThatStr;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @author cordes
 */
public class AssertThatTest {

    @Test
    public void testAssertion() {

        assertThat(new ValueProducer<Integer>() {
            @Override
            public Integer value() throws Exception {
                return 2;
            }
        }).isEquals(2);

        assertThatBoolean(new ValueProducer<Boolean>() {
            @Override
            public Boolean value() throws Exception {
                return true;
            }
        }).isTrue();

        assertThatBoolean(new ValueProducer<Boolean>() {
            @Override
            public Boolean value() throws Exception {
                return false;
            }
        }).isFalse();

        assertThatBoolean(new ValueProducer<Boolean>() {
            @Override
            public Boolean value() throws Exception {
                return false;
            }
        }).isEquals(false);

        assertThat(new ValueProducer<Object>() {
            @Override
            public Object value() throws Exception {
                return new Object();
            }
        }).isNotNull();

        assertThat(new ValueProducer<Object>() {
            @Override
            public Object value() throws Exception {
                return null;
            }
        }).isNull();

        final Object object = new Object();
        assertThat(new ValueProducer<Object>() {
            @Override
            public Object value() throws Exception {
                return object;
            }
        }).isSame(object);

        assertThatStr(new ValueProducer<String>() {
            @Override
            public String value() throws Exception {
                return "This is a test string.";
            }
        }).containsString("test");

        assertThatInt(new ValueProducer<Integer>() {
            @Override
            public Integer value() throws Exception {
                return 1;
            }
        }).isGreaterThan(0);

        assertThatInt(new ValueProducer<Integer>() {
            @Override
            public Integer value() throws Exception {
                return 1;
            }
        }).isLessThan(2);

    }

    @Test
    public void testWithWait() {
        final long start = System.currentTimeMillis();

        assertThatBoolean(new ValueProducer<Boolean>() {
            @Override
            public Boolean value() throws Exception {
                Boolean result = false;
                long now = System.currentTimeMillis();
                if (now - TimeUnit.SECONDS.toMillis(3) > start) {
                    result = true;
                }
                return result;
            }
        }).isTrue();
    }

}
