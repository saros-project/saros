package org.limewire.rudp;

import junit.framework.Test;

import org.limewire.util.BaseTestCase;

/**
 * Tests the SequenceNumberExtender class.
 */
public final class SequenceNumberExtenderTest extends BaseTestCase {

    /*
     * Constructs the test.
     */
    public SequenceNumberExtenderTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(SequenceNumberExtenderTest.class);
    }

    /**
     * Runs this test individually.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Test that increments remain in sync with a full long being incremented
     * and with the values being mapped down to 2 byte sequenceNumbers.
     * 
     * @throws Exception if an error occurs
     */
    public void testSequenceNumberIncrements() throws Exception {

        // Init the extender
        SequenceNumberExtender extender = new SequenceNumberExtender();

        // Make sure that the sequence remains in sync with prior number
        // and its own extended value - test up to 100000000
        long finalValue = 10000000;
        long lastiand;
        long iand = -1;
        lastiand = 0;
        for (long i = 1; i <= finalValue; i++) {

            // Shrink the sequenceNumber down to 2 bytes
            iand = i & 0xffff;

            // Extend the sequenceNumber back to 8 bytes
            iand = extender.extendSequenceNumber((iand));

            assertEquals("Previous sequence number plus 1 isn't current one",
                    lastiand + 1, iand);
            assertEquals(i, iand);
            lastiand = iand;
        }

        assertEquals(finalValue, iand);
    }

    public void testOutOfOrderNumber() {
        SequenceNumberExtender extender = new SequenceNumberExtender();
        for (long i = 0; i < 0x3FFF; i++)
            assertEquals(i, extender.extendSequenceNumber(i));
        assertEquals(0x3FFF, extender.extendSequenceNumber(0x3FFF));
        for (long i = 0x3FFF + 1; i < 0x7FFF - 4; i++)
            assertEquals(i, extender.extendSequenceNumber(i));
        assertEquals(0x7FFF + 1, extender.extendSequenceNumber(0x7FFF + 1));
        assertEquals(0x7FFF - 3, extender.extendSequenceNumber(0x7FFF - 3));
    }

    public void testOutOfOrderNumbers() {
        SequenceNumberExtender extender = new SequenceNumberExtender();
        for (int i = 1; i < 0xFFFFFF; i++) {
            long extendedI = extender.extendSequenceNumber((i & 0xFFFF));
            long outOfOrder = Math.max(i - 3, 1);
            long extendedOutOfOrder = extender
                    .extendSequenceNumber((outOfOrder & 0xFFFF));
            assertEquals(i, extendedI);
            assertEquals(outOfOrder, extendedOutOfOrder);
            assertEquals(i, extender.extendSequenceNumber((i & 0xFFFF)));
        }
    }
    
    public void testLargeValues() {
        SequenceNumberExtender extender = new SequenceNumberExtender();
        assertEquals(0x3FFF, extender.extendSequenceNumber(0x3FFF));
        assertEquals(0x3FFF, extender.extendSequenceNumber(0x3FFF));
        assertEquals(0xFFFF, extender.extendSequenceNumber(0xFFFF));
        assertEquals(0x10000, extender.extendSequenceNumber(0x0000));
    }

}
