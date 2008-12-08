package org.limewire.rudp;

import junit.framework.TestCase;

public class DataWindowTest extends TestCase {

    public void testAddData() {
        DataWindow window = new DataWindow(20, 0);
        assertFalse(window.hasReadableData());
        
        StubDataMessage msg1 = new StubDataMessage(0);
        DataRecord rec = window.addData(msg1);
        assertTrue(window.hasReadableData());
        assertSame(msg1, rec.msg);
        
        StubDataMessage msg2 = new StubDataMessage(0);
        rec = window.addData(msg2);
        assertTrue(window.hasReadableData());
        assertSame(msg1, rec.msg);
    }

    public void testGetWindowSize() {
        DataWindow window = new DataWindow(2, 0);
        assertEquals(2, window.getWindowSize());

        try {
            window = new DataWindow(0, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            window = new DataWindow(-1, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }
    
    public void testClearEarlyReadBlocks() {
        DataWindow window = new DataWindow(2, 0);
        assertFalse(window.hasReadableData());
        assertEquals(2, window.getWindowSpace());
        
        DataRecord rec1 = window.addData(new StubDataMessage(0));
        assertTrue(window.hasReadableData());        
        assertEquals(1, window.getWindowSpace());
        
        window.clearEarlyReadBlocks();
        assertNotNull(window.getBlock(0));
        assertEquals(1, window.getWindowSpace());
        rec1.read = true;
        window.clearEarlyReadBlocks();
        assertEquals(2, window.getWindowSpace());
        assertFalse(window.hasReadableData());
        assertNull(window.getBlock(0));

        rec1 = window.addData(new StubDataMessage(1));
        DataRecord rec2 = window.addData(new StubDataMessage(2));
        assertTrue(window.hasReadableData());
        window.clearEarlyReadBlocks();
        assertNull(window.getBlock(0));
        assertNotNull(window.getBlock(1));
        assertTrue(window.hasReadableData());        
     
        rec2.read = true;
        window.clearEarlyReadBlocks();
        assertTrue(window.hasReadableData());        
        assertEquals(0, window.getWindowSpace());
        assertNotNull(window.getBlock(1));
        assertNotNull(window.getBlock(2));

        rec1.read = true;
        window.clearEarlyReadBlocks();
        assertFalse(window.hasReadableData());        
        assertEquals(2, window.getWindowSpace());
        assertNull(window.getBlock(1));
        assertNull(window.getBlock(2));
    }
    
    public void testAdvanceWindow() {
        DataWindow window = new DataWindow(2, 5);
        assertEquals(5, window.getWindowStart());

        DataRecord rec1;
        try {
             rec1 = window.addData(new StubDataMessage(4));
            fail("Expected IllegalStateException, got: " + rec1);
        } catch (IllegalStateException expected) {
        }
        assertEquals(5, window.getWindowStart());

        rec1 = window.addData(new StubDataMessage(5));
        rec1.read = true;
        assertEquals(5, window.getWindowStart());

        window.clearEarlyReadBlocks();
        assertEquals(6, window.getWindowStart());

        try {
            rec1 = window.addData(new StubDataMessage(5));
            fail("Expected IllegalStateException, got: " + rec1);
        } catch (IllegalStateException expected) {
        }        
    }
    
    public void testGetUsedSpots() {
        DataWindow window = new DataWindow(2, 0);
        assertEquals(0, window.getUsedSpots());
        
        DataRecord rec1 = window.addData(new StubDataMessage(0));
        assertEquals(1, window.getUsedSpots());

        rec1 = window.addData(new StubDataMessage(0));
        rec1.read = true;
        assertEquals(0, window.getUsedSpots());
        window.clearEarlyReadBlocks();
        assertEquals(0, window.getUsedSpots());

        DataRecord rec2 = window.addData(new StubDataMessage(2));
        assertEquals(1, window.getUsedSpots());        

        rec2 = window.addData(new StubDataMessage(2));
        assertEquals(1, window.getUsedSpots());        
        
        rec1 = window.addData(new StubDataMessage(1));
        assertEquals(2, window.getUsedSpots());
        rec1.read = true;
        rec2.read = true;
        // TODO remove following line
        window.clearEarlyReadBlocks();
        assertEquals(0, window.getUsedSpots());
    }    

    public void testHasReadable() {
        DataWindow window = new DataWindow(2, 0);
        assertFalse(window.hasReadableData());

        DataRecord rec2 = window.addData(new StubDataMessage(1));
        assertFalse(window.hasReadableData());
        rec2.read = true;
        assertFalse(window.hasReadableData());
        
        DataRecord rec1 = window.addData(new StubDataMessage(0));
        assertTrue(window.hasReadableData());
        rec1.read = true;
        // TODO remove following line
        window.clearEarlyReadBlocks();
        assertFalse(window.hasReadableData());
    }
}
